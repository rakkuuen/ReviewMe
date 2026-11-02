package Controller.BackEnd.GameDetail;

import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.MainView.MainViewLabels;
import Controller.BackEnd.Overview.StatsText;
import Controller.BackEnd.Reviews.GameNumbers;
import Controller.BackEnd.Reviews.LibraryDates;
import Controller.BackEnd.Reviews.ReviewLabels;
import Model.Library.SteamGame;
import Model.Reviews.AddedToLibrary;
import Model.Reviews.CompletionSource;
import Model.Reviews.FieldKind;
import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.RatingRules;
import Model.Reviews.ReviewGap;
import Model.Reviews.ReviewState;
import Model.Reviews.ReviewStatus;
import Model.Reviews.Series;
import Model.Reviews.Subsection;
import Model.Reviews.Template;
import Model.Reviews.TemplateField;

// Works out a game's page from its review, template, status, Steam record and series. The sections come from the template (fields it
// doesn't have stay saved but are not shown). No drawing and no database in here
public class GameDetailBuilder {
    private static final String dot = " " + (char) 0x00B7 + " ";   // A middle dot, by code point so the source stays plain ASCII

    // steam is null for a game that isn't linked to Steam; series is null (and seriesGameCount ignored) for a game in no series
    public static GameDetailData Build(GameReview review, Template template, ReviewStatus status, SteamGame steam, Series series, int seriesGameCount){
        String seriesLink = series == null ? null : series.GetName() + ", " + Position(review) + " of " + seriesGameCount;
        List<String> tags = new ArrayList<>(review.GetGenres());
        tags.addAll(review.GetPlatforms());
        return new GameDetailData(review.GetTitle(), series == null ? null : series.GetId(), seriesLink, Badges(status), Stats(review, steam), tags,
                new ArrayList<>(review.GetMetadata()), Sections(review, template, status));
    }

    private static String Position(GameReview review){
        return review.GetSeriesPosition() == null ? "?" : String.valueOf(review.GetSeriesPosition());
    }

    private static List<DetailBadge> Badges(ReviewStatus status){
        List<DetailBadge> badges = new ArrayList<>();
        badges.add(new DetailBadge(MainViewLabels.StateLabel(status.GetGameState()), DetailBadge.Look.STATE));
        DetailBadge.Look reviewLook = status.CountsAsIncomplete() ? DetailBadge.Look.WARNING : status.GetReviewState() == ReviewState.COMPLETE ? DetailBadge.Look.REVIEW_OK : DetailBadge.Look.REVIEW_PLAIN;
        badges.add(new DetailBadge(ReviewLabels.Label(status), reviewLook));
        if(status.HasMoreAchievements()){
            badges.add(new DetailBadge("More achievements added" + dot + "+" + status.GetNewAchievements(), DetailBadge.Look.MORE_ACHIEVEMENTS));
        }
        return badges;
    }

    private static List<DetailStat> Stats(GameReview review, SteamGame steam){
        List<DetailStat> stats = new ArrayList<>();
        stats.add(new DetailStat("Hours played", DetailText.Hours(GameNumbers.PlaytimeMinutes(review, steam)), null, null, null, false));
        stats.add(new DetailStat("Last played", DetailText.DateOfEpoch(steam == null ? null : steam.GetLastPlayed()), null, null, null, false));
        stats.add(new DetailStat("Completed on", DetailText.Date(review.GetCompletedOn()), null, CompletedNote(review), null, false));
        AddedToLibrary added = LibraryDates.Effective(review, steam == null ? null : steam.GetFirstSeenAt());
        stats.add(new DetailStat("Added to library", DetailText.Date(added.GetDate()), null, AddedNote(added), null, false));
        stats.add(Achievements(steam));
        return stats;
    }

    private static String CompletedNote(GameReview review){
        if(review.GetCompletedOn() == null || review.GetCompletedOn().trim().isEmpty() || review.GetCompletedOnSource() == null){
            return null;
        }
        return review.GetCompletedOnSource() == CompletionSource.STEAM ? "from last unlock" : "set manually";
    }

    private static String AddedNote(AddedToLibrary added){
        switch(added.GetSource()){
            case MANUAL: return "set manually";
            case FIRST_SEEN: return "first seen by ReviewMe";
            default: return null;
        }
    }

    private static DetailStat Achievements(SteamGame steam){
        if(steam == null || !steam.HasAchievements()){
            return new DetailStat("Achievements", "none", null, null, null, false);
        }
        int unlocked = steam.GetUnlockedAchievements() == null ? 0 : steam.GetUnlockedAchievements();
        int total = steam.GetTotalAchievements();
        return new DetailStat("Achievements", StatsText.Count(unlocked), "/ " + StatsText.Count(total), null, Math.min(1.0, unlocked / (double) total), true);
    }

    private static List<DetailSection> Sections(GameReview review, Template template, ReviewStatus status){
        List<DetailSection> sections = new ArrayList<>();
        for(TemplateField field : template.GetFields()){
            sections.add(Section(field, review.GetFieldValue(field.GetFieldKey()), status));
        }
        return sections;
    }

    private static DetailSection Section(TemplateField field, FieldValue value, ReviewStatus status){
        boolean rating = field.GetKind() == FieldKind.RATING_OR_UNKNOWN;
        if(value != null && value.IsNotApplicable()){
            return new DetailSection(field.GetFieldKey(), field.GetHeading(), DetailSection.State.NOT_APPLICABLE, rating, "", "", false, new ArrayList<>());
        }
        String text = value == null || value.GetText() == null ? "" : value.GetText().trim();
        String comment = value == null || value.GetComment() == null ? "" : value.GetComment().trim();
        List<DetailPart> parts = new ArrayList<>();
        if(value != null){
            for(Subsection subsection : value.GetSubsections()){
                parts.add(Part(subsection));
            }
        }
        DetailSection.State state = value == null || value.IsMissing() ? (IsGap(field, status) ? DetailSection.State.MISSING : DetailSection.State.EMPTY) : DetailSection.State.FILLED;
        return new DetailSection(field.GetFieldKey(), field.GetHeading(), state, rating, text, comment, rating && RatingRules.unknown.equals(text), parts);
    }

    // Only a section the review counts as missing is flagged; an optional one with nothing in it is simply empty
    private static boolean IsGap(TemplateField field, ReviewStatus status){
        for(ReviewGap gap : status.GetGaps()){
            if(gap.GetKind() == ReviewGap.Kind.MISSING_SECTION && field.GetFieldKey().equals(gap.GetFieldKey())){
                return true;
            }
        }
        return false;
    }

    private static DetailPart Part(Subsection subsection){
        List<DetailPart> children = new ArrayList<>();
        for(Subsection child : subsection.GetChildren()){
            children.add(Part(child));
        }
        String text = subsection.GetText() == null ? "" : subsection.GetText().trim();
        return new DetailPart(subsection.GetName(), text, !subsection.HasText(), children);
    }
}
