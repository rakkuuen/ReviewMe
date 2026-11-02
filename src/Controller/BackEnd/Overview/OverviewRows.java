package Controller.BackEnd.Overview;

import java.util.List;

import Controller.BackEnd.MainView.MainViewLabels;
import Controller.BackEnd.Reviews.GameNumbers;
import Controller.BackEnd.Reviews.ReviewLabels;
import Model.Library.LibraryGame;
import Model.Library.SteamGame;
import Model.Reviews.ReviewGap;
import Model.Reviews.ReviewState;
import Model.Reviews.ReviewStatus;

// The words on each row of the Overview's short lists: the title, a line of detail suited to the list, and the rating
public class OverviewRows {
    private static final String dot = " " + (char) 0x00B7 + " ";   // A middle dot, by code point so the source stays plain ASCII
    private static final int gapsNamed = 2;
    private static final int minutesPerHour = 60;

    public static GameRow Build(OverviewSectionKind kind, LibraryGame game){
        return new GameRow(game, game.GetTitle(), Detail(kind, game), GameNumbers.RatingText(game.GetReview()));
    }

    // A game in a series: its state, how complete its review is, and its achievements
    public static GameRow BuildForSeries(LibraryGame game){
        ReviewStatus status = game.GetStatus();
        return new GameRow(game, game.GetTitle(), MainViewLabels.StateLabel(status.GetGameState()) + dot + ReviewLabels.Label(status) + dot + ProgressNumbers(game.GetSteam()),
                GameNumbers.RatingText(game.GetReview()));
    }

    private static String Detail(OverviewSectionKind kind, LibraryGame game){
        ReviewStatus status = game.GetStatus();
        switch(kind){
            case INCOMPLETE: return MissingText(status);
            case MORE_ACHIEVEMENTS: return ProgressWords(game.GetSteam()) + dot + "+" + status.GetNewAchievements() + " new";
            case IN_PROGRESS: return ReviewLabels.Label(status) + dot + ProgressNumbers(game.GetSteam());
            case COMPLETED: return Hours(game) + dot + ProgressNumbers(game.GetSteam());
            case SHELVED: return ReasonText(status) + dot + ReviewLabels.Label(status);
            default: return ReviewLabels.Label(status);
        }
    }

    // What is missing from a review of a completed game: the first couple of gaps and how many more there are, or that there is no
    // review at all
    static String MissingText(ReviewStatus status){
        if(status.GetReviewState() == ReviewState.NONE){
            return ReviewLabels.Label(status);
        }
        List<ReviewGap> gaps = status.GetGaps();
        if(gaps.isEmpty()){
            return ReviewLabels.Label(status);
        }
        StringBuilder text = new StringBuilder("Missing: ");
        for(int i = 0; i < Math.min(gaps.size(), gapsNamed); i++){
            text.append(i > 0 ? ", " : "").append(Capitalised(gaps.get(i).GetPath()));
        }
        if(gaps.size() > gapsNamed){
            text.append(" +").append(gaps.size() - gapsNamed).append(" more");
        }
        return text.toString();
    }

    // "47 of 50 unlocked"
    private static String ProgressWords(SteamGame steam){
        if(steam == null || !steam.HasAchievements()){
            return "no achievements";
        }
        return Count(steam.GetUnlockedAchievements()) + " of " + steam.GetTotalAchievements() + " unlocked";
    }

    // "41/47", or "no achievements" for a game with none (or not on Steam)
    private static String ProgressNumbers(SteamGame steam){
        if(steam == null || !steam.HasAchievements()){
            return "no achievements";
        }
        return Count(steam.GetUnlockedAchievements()) + "/" + steam.GetTotalAchievements();
    }

    private static int Count(Integer number){
        return number == null ? 0 : number;
    }

    // "12 h", rounded
    private static String Hours(LibraryGame game){
        int minutes = GameNumbers.PlaytimeMinutes(game.GetReview(), game.GetSteam());
        return Math.round(minutes / (double) minutesPerHour) + " h";
    }

    // Why a game was shelved, on one line, or a note that no reason was given
    private static String ReasonText(ReviewStatus status){
        String reason = status.GetShelvedReason();
        if(reason == null || reason.trim().isEmpty()){
            return "No reason given";
        }
        return reason.trim().replaceAll("\\s+", " ");
    }

    private static String Capitalised(String text){
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
