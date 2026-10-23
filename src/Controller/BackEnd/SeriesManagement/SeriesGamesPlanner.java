package Controller.BackEnd.SeriesManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Controller.BackEnd.Reviews.LoadedReview;
import Controller.BackEnd.Reviews.ReviewLoader;
import Database.Dao.GameReviewDao;
import Database.Dao.SeriesDao;
import Database.Dao.TemplateDao;
import Model.Reviews.GameReview;
import Model.Reviews.Series;
import Model.Reviews.Template;

// Plans the operations on a series' games: which games belong, in what order, and which template they use. Reads the database,
// writes nothing. Every game a plan moves is renumbered so each series ends up with positions 1 to n. Hidden fields are never
// deleted by a template switch: the game just points at another template
public class SeriesGamesPlanner {
    public static final String standardTemplateId = "Standard";

    public static String NoSeries(String seriesId){
        return "There is no series with the tag " + seriesId;
    }

    // Every game in the series switches to the template (and the series remembers it)
    public static SeriesPlan SetTemplate(String seriesId, String templateId){
        Series series = SeriesDao.GetSeries(seriesId);
        if(series == null){
            return SeriesPlan.Refused(NoSeries(seriesId));
        }
        if(TemplateDao.GetTemplate(templateId) == null){
            return SeriesPlan.Refused("There is no template called " + templateId);
        }
        SeriesPlan plan = new SeriesPlan();
        MembershipTracker tracker = new MembershipTracker();
        for(GameReview game : MembersOf(GameReviewDao.GetAllGameReviews(), seriesId)){
            tracker.Track(game);
            SwitchTemplate(game, templateId, plan);
        }
        if(!templateId.equals(series.GetTemplateId())){
            Series updated = series.Copy();
            updated.SetTemplateId(templateId);
            plan.GetChange().SetToUpdate(updated);
            plan.GetUndo().SetToUpdate(series);
        }
        return Finish(plan, tracker);
    }

    // The games (by title) join the end of the series in the order given, take its template, and leave any series they were in.
    // Games already in the series are left where they are
    public static SeriesPlan Assign(String seriesId, List<String> titles){
        Series series = SeriesDao.GetSeries(seriesId);
        if(series == null){
            return SeriesPlan.Refused(NoSeries(seriesId));
        }
        List<GameReview> all = GameReviewDao.GetAllGameReviews();
        SeriesPlan plan = new SeriesPlan();
        List<GameReview> joining = FindJoining(all, seriesId, titles, plan);
        if(!plan.Succeeded() || joining.isEmpty()){
            return plan;
        }

        MembershipTracker tracker = new MembershipTracker();
        List<GameReview> target = MembersOf(all, seriesId);   // as it is now, before the newcomers move in
        tracker.TrackAll(target);
        Set<String> leaving = new LinkedHashSet<>();
        for(GameReview game : joining){
            tracker.Track(game);
            if(game.GetSeriesId() != null){
                leaving.add(game.GetSeriesId());
            }
            game.SetSeriesId(seriesId);
            SwitchTemplate(game, series.GetTemplateId() == null ? TemplateOf(game) : series.GetTemplateId(), plan);
        }
        target.addAll(joining);
        SeriesOrder.Renumber(target);
        for(String oldSeriesId : leaving){
            CloseGap(all, oldSeriesId, tracker);
        }
        return Finish(plan, tracker);
    }

    // The game leaves its series, goes back to the Standard template, and the others close the gap
    public static SeriesPlan Unassign(String title){
        List<GameReview> all = GameReviewDao.GetAllGameReviews();
        GameReview game = ByTitle(all).get(title);
        if(game == null){
            return SeriesPlan.Refused("There is no review called " + title);
        }
        if(game.GetSeriesId() == null){
            return SeriesPlan.Refused(title + " isn't in a series");
        }
        SeriesPlan plan = new SeriesPlan();
        MembershipTracker tracker = new MembershipTracker();
        tracker.Track(game);
        String oldSeriesId = game.GetSeriesId();
        game.SetSeriesId(null);
        game.SetSeriesPosition(null);
        SwitchTemplate(game, standardTemplateId, plan);
        CloseGap(all, oldSeriesId, tracker);
        return Finish(plan, tracker);
    }

    // The series is removed. Its games stay, with no series and the Standard template
    public static SeriesPlan Delete(String seriesId){
        Series series = SeriesDao.GetSeries(seriesId);
        if(series == null){
            return SeriesPlan.Refused(NoSeries(seriesId));
        }
        SeriesPlan plan = new SeriesPlan();
        MembershipTracker tracker = new MembershipTracker();
        for(GameReview game : MembersOf(GameReviewDao.GetAllGameReviews(), seriesId)){
            tracker.Track(game);
            game.SetSeriesId(null);
            game.SetSeriesPosition(null);
            SwitchTemplate(game, standardTemplateId, plan);
        }
        plan.GetChange().SetIdToDelete(seriesId);
        plan.GetUndo().SetToInsert(series);
        return Finish(plan, tracker);
    }

    // titles must be exactly the series' games, each once, in the new order
    public static SeriesPlan Reorder(String seriesId, List<String> titles){
        if(SeriesDao.GetSeries(seriesId) == null){
            return SeriesPlan.Refused(NoSeries(seriesId));
        }
        List<GameReview> members = MembersOf(GameReviewDao.GetAllGameReviews(), seriesId);
        Map<String, GameReview> byTitle = ByTitle(members);
        Set<String> given = new HashSet<>(titles);
        if(given.size() != titles.size() || !given.equals(byTitle.keySet())){
            return SeriesPlan.Refused("The new order has to list every game in the series exactly once");
        }
        List<GameReview> ordered = new ArrayList<>();
        for(String title : titles){
            ordered.add(byTitle.get(title));
        }
        return Renumbered(members, ordered);
    }

    // One place up (-1) or down (+1)
    public static SeriesPlan MoveBy(String seriesId, String title, int delta){
        if(SeriesDao.GetSeries(seriesId) == null){
            return SeriesPlan.Refused(NoSeries(seriesId));
        }
        List<GameReview> members = MembersOf(GameReviewDao.GetAllGameReviews(), seriesId);
        GameReview game = ByTitle(members).get(title);
        if(game == null){
            return SeriesPlan.Refused(title + " isn't in this series");
        }
        if(!SeriesOrder.CanMoveBy(members, game, delta)){
            return SeriesPlan.Refused(title + (delta < 0 ? " is already first" : " is already last"));
        }
        return Renumbered(members, SeriesOrder.MovedBy(members, game, delta));
    }

    private static SeriesPlan Renumbered(List<GameReview> before, List<GameReview> ordered){
        SeriesPlan plan = new SeriesPlan();
        MembershipTracker tracker = new MembershipTracker();
        tracker.TrackAll(before);
        SeriesOrder.Renumber(ordered);
        return Finish(plan, tracker);
    }

    // The games asked for, once each, that aren't in the series yet. Titles that don't exist are errors on the plan
    private static List<GameReview> FindJoining(List<GameReview> all, String seriesId, List<String> titles, SeriesPlan plan){
        Map<String, GameReview> byTitle = ByTitle(all);
        List<GameReview> joining = new ArrayList<>();
        for(String title : titles){
            GameReview game = byTitle.get(title);
            if(game == null){
                plan.AddError("There is no review called " + title);
            } else if(!seriesId.equals(game.GetSeriesId()) && !joining.contains(game)){
                joining.add(game);
            }
        }
        return joining;
    }

    // Moves the game to the template, recording what that hides. A game already on it is left alone
    private static void SwitchTemplate(GameReview game, String toTemplateId, SeriesPlan plan){
        String from = TemplateOf(game);
        if(from.equals(toTemplateId)){
            return;
        }
        LoadedReview loaded = ReviewLoader.Load(game.GetTitle());
        Template to = TemplateDao.GetTemplate(toTemplateId);
        if(loaded == null || to == null){
            plan.AddError("Couldn't read " + game.GetTitle() + " to switch its template");
            return;
        }
        plan.AddSwitch(new TemplateSwitch(game.GetTitle(), from, toTemplateId,
                TemplateImpact.Compute(loaded.GetTemplate(), to, loaded.GetReview().GetFieldValues())));
        game.SetTemplateId(toTemplateId);
    }

    // A review with no template stored uses Standard
    private static String TemplateOf(GameReview game){
        return game.GetTemplateId() == null ? standardTemplateId : game.GetTemplateId();
    }

    // Renumbers what's left of a series after games left it
    private static void CloseGap(List<GameReview> all, String seriesId, MembershipTracker tracker){
        List<GameReview> remaining = MembersOf(all, seriesId);
        tracker.TrackAll(remaining);
        SeriesOrder.Renumber(remaining);
    }

    // A plan whose games moved: what changed goes on the change, what they were goes on the undo
    private static SeriesPlan Finish(SeriesPlan plan, MembershipTracker tracker){
        if(plan.Succeeded()){
            plan.GetChange().AddMemberships(tracker.Forward());
            plan.GetUndo().AddMemberships(tracker.Backward());
        }
        return plan;
    }

    private static List<GameReview> MembersOf(List<GameReview> all, String seriesId){
        List<GameReview> members = new ArrayList<>();
        for(GameReview game : all){
            if(seriesId.equals(game.GetSeriesId())){
                members.add(game);
            }
        }
        return SeriesOrder.Sorted(members);
    }

    private static Map<String, GameReview> ByTitle(List<GameReview> games){
        Map<String, GameReview> byTitle = new HashMap<>();
        for(GameReview game : games){
            byTitle.put(game.GetTitle(), game);
        }
        return byTitle;
    }
}
