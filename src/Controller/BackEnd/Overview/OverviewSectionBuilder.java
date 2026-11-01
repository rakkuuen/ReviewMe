package Controller.BackEnd.Overview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import Controller.BackEnd.MainView.MainTab;
import Controller.BackEnd.MainView.StateSelection;
import Controller.BackEnd.MainView.TabMembership;
import Controller.BackEnd.Reviews.GameNumbers;
import Model.Library.LibraryGame;
import Model.Reviews.GameState;
import Model.Reviews.RatingRules;
import Model.Reviews.ReviewState;

// Picks and orders the rows of one Overview list. Incomplete shows the reviews closest to finished first, More achievements the
// most new achievements, In progress the most recently played, Completed the highest rated; Shelved and Not started go by title
public class OverviewSectionBuilder {
    private static final StateSelection hundredPercent = StateSelection.Of(GameState.COMPLETED);

    private static final Comparator<LibraryGame> byTitle = Comparator
            .comparing((LibraryGame game) -> game.GetTitle().toLowerCase())
            .thenComparing(LibraryGame::GetTitle);

    public static OverviewSection Build(List<LibraryGame> games, OverviewSectionKind kind, int rowLimit){
        List<LibraryGame> members = new ArrayList<>();
        for(LibraryGame game : games){
            if(IsMember(kind, game)){
                members.add(game);
            }
        }
        members.sort(OrderFor(kind));
        int shown = Math.min(Math.max(rowLimit, 0), members.size());
        return new OverviewSection(new ArrayList<>(members.subList(0, shown)), members.size());
    }

    private static boolean IsMember(OverviewSectionKind kind, LibraryGame game){
        switch(kind){
            case INCOMPLETE: return TabMembership.Contains(MainTab.INCOMPLETE, hundredPercent, game);
            case MORE_ACHIEVEMENTS: return TabMembership.Contains(MainTab.MORE_ACHIEVEMENTS, hundredPercent, game);
            case COMPLETED: return TabMembership.Contains(MainTab.COMPLETED, hundredPercent, game);
            case IN_PROGRESS: return game.GetStatus().GetGameState() == GameState.IN_PROGRESS;
            case SHELVED: return game.GetStatus().GetGameState() == GameState.SHELVED;
            default: return game.GetStatus().GetGameState() == GameState.NOT_STARTED;
        }
    }

    private static Comparator<LibraryGame> OrderFor(OverviewSectionKind kind){
        switch(kind){
            case INCOMPLETE:
                // Partial reviews before blank ones, then the fewest gaps first
                return Comparator
                        .comparingInt((LibraryGame game) -> game.GetStatus().GetReviewState() == ReviewState.NONE ? 1 : 0)
                        .thenComparingInt(game -> game.GetStatus().GetGaps().size())
                        .thenComparing(byTitle);
            case MORE_ACHIEVEMENTS:
                return Comparator.comparingInt((LibraryGame game) -> -game.GetStatus().GetNewAchievements()).thenComparing(byTitle);
            case IN_PROGRESS:
                return Comparator.comparingLong((LibraryGame game) -> -LastPlayed(game)).thenComparing(byTitle);
            case COMPLETED:
                return ((Comparator<LibraryGame>) (a, b) -> RatingRules.Compare(GameNumbers.RatingText(a.GetReview()), GameNumbers.RatingText(b.GetReview()), true))
                        .thenComparing(byTitle);
            default:
                return byTitle;
        }
    }

    // Zero when never played or not on Steam
    private static long LastPlayed(LibraryGame game){
        return game.IsOnSteam() && game.GetSteam().GetLastPlayed() != null ? game.GetSteam().GetLastPlayed() : 0L;
    }
}
