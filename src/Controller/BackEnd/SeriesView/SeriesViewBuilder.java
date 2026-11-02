package Controller.BackEnd.SeriesView;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import Controller.BackEnd.Overview.GameRow;
import Controller.BackEnd.Overview.OverviewRows;
import Controller.BackEnd.Overview.StatCard;
import Controller.BackEnd.Overview.StatsText;
import Controller.BackEnd.SeriesManagement.SeriesGameFacts;
import Controller.BackEnd.SeriesManagement.SeriesOrder;
import Controller.BackEnd.SeriesManagement.SeriesStats;
import Controller.BackEnd.SeriesManagement.SeriesStatsCalculator;
import Model.Library.LibraryGame;
import Model.Reviews.GameReview;
import Model.Reviews.Series;

// Works out one series' page from the series and every game in the library. templateNames turns a template's id into the name shown
// (an id with no name is shown as it is). No drawing and no database in here
public class SeriesViewBuilder {
    private static final String noNotes = "No notes yet. Use Edit series to add some.";
    private static final int single = 1;

    public static SeriesViewData Build(Series series, List<LibraryGame> games, Map<String, String> templateNames){
        Map<GameReview, LibraryGame> gameByReview = new IdentityHashMap<>();
        List<GameReview> reviews = new ArrayList<>();
        for(LibraryGame game : games){
            if(game.HasReview() && series.GetId().equals(game.GetReview().GetSeriesId())){
                gameByReview.put(game.GetReview(), game);
                reviews.add(game.GetReview());
            }
        }
        List<SeriesGameItem> items = new ArrayList<>();
        List<SeriesGameFacts> facts = new ArrayList<>();
        int place = 1;
        for(GameReview review : SeriesOrder.Sorted(reviews)){
            LibraryGame game = gameByReview.get(review);
            facts.add(new SeriesGameFacts(review, game.GetStatus(), game.GetSteam()));
            GameRow row = OverviewRows.BuildForSeries(game);
            items.add(new SeriesGameItem(String.valueOf(place++), row, DifferentTemplate(series, review, templateNames)));
        }
        SeriesStats stats = SeriesStatsCalculator.Calculate(series, facts);
        String notes = series.GetNotes();
        boolean hasNotes = notes != null && !notes.trim().isEmpty();
        return new SeriesViewData(series.GetId(), series.GetName(), "Template: " + NameOf(series.GetTemplateId(), templateNames), Warning(stats.GetDifferentTemplateCount()),
                Cards(stats), hasNotes ? notes : noNotes, hasNotes, items);
    }

    // The four figures: average rating, total hours, achievements and how many are completed
    static List<StatCard> Cards(SeriesStats stats){
        List<StatCard> cards = new ArrayList<>();
        String rated = stats.GetRatedCount() == 0 ? "no rated games yet" : stats.GetRatedCount() + " of " + stats.GetGameCount() + " rated";
        cards.add(new StatCard(StatsText.Average(stats.GetAverageRating()), "Average rating", rated, false, single));
        cards.add(new StatCard(StatsText.Hours(stats.GetPlaytimeMinutes()), "Total hours", null, false, single));
        cards.add(new StatCard(StatsText.Count(stats.GetUnlockedAchievements()) + " / " + StatsText.Count(stats.GetTotalAchievements()), "Achievements", null, false, single));
        cards.add(new StatCard(stats.GetCompletedCount() + " of " + stats.GetGameCount(), "Completed", null, false, single));
        return cards;
    }

    private static String Warning(int different){
        if(different == 0){
            return null;
        }
        return different + (different == 1 ? " game uses" : " games use") + " a different template";
    }

    private static String DifferentTemplate(Series series, GameReview review, Map<String, String> templateNames){
        if(series.GetTemplateId() == null || series.GetTemplateId().equals(review.GetTemplateId())){
            return null;
        }
        return NameOf(review.GetTemplateId(), templateNames);
    }

    private static String NameOf(String templateId, Map<String, String> templateNames){
        if(templateId == null){
            return "none";
        }
        String name = templateNames.get(templateId);
        return name == null ? templateId : name;
    }
}
