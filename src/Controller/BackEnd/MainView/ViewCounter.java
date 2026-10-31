package Controller.BackEnd.MainView;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Model.Library.LibraryGame;
import Model.Reviews.ReviewState;

// The numbers the State row, tabs and review chips show
public class ViewCounter {

    // Games in the picked states: the State row's counts, one call per chip (All for the first)
    public static int StateCount(List<LibraryGame> games, StateSelection selection){
        int count = 0;
        for(LibraryGame game : games){
            count += selection.Includes(game.GetStatus().GetGameState()) ? 1 : 0;
        }
        return count;
    }

    // A count for each tab the selection has. Series counts the series with a game in the selection; with All it is every series,
    // empty ones included (allSeriesIds)
    public static Map<MainTab, Integer> TabCounts(List<LibraryGame> games, StateSelection selection, Collection<String> allSeriesIds){
        Map<MainTab, Integer> counts = new EnumMap<>(MainTab.class);
        for(MainTab tab : ViewRules.TabsFor(selection)){
            counts.put(tab, tab == MainTab.SERIES ? SeriesCount(games, selection, allSeriesIds) : TabGames(games, tab, selection));
        }
        return counts;
    }

    // How many games in the tab (or in the picked states, for the Library: tab null) have each review state the chips offer
    public static Map<ReviewState, Integer> ReviewCounts(List<LibraryGame> games, MainTab tab, StateSelection selection){
        Map<ReviewState, Integer> counts = new EnumMap<>(ReviewState.class);
        for(ReviewState review : tab == null ? ViewRules.ReviewStatesFor(selection) : ViewRules.ReviewStatesForTab(tab, selection)){
            counts.put(review, 0);
        }
        for(LibraryGame game : games){
            boolean inScope = tab == null ? selection.Includes(game.GetStatus().GetGameState()) : TabMembership.Contains(tab, selection, game);
            ReviewState review = game.GetStatus().GetReviewState();
            if(inScope && counts.containsKey(review)){
                counts.merge(review, 1, Integer::sum);
            }
        }
        return counts;
    }

    // Games in the picked states carrying the More achievements tag
    public static int MoreAchievementsCount(List<LibraryGame> games, StateSelection selection){
        int count = 0;
        for(LibraryGame game : games){
            count += selection.Includes(game.GetStatus().GetGameState()) && game.GetStatus().HasMoreAchievements() ? 1 : 0;
        }
        return count;
    }

    private static int TabGames(List<LibraryGame> games, MainTab tab, StateSelection selection){
        int count = 0;
        for(LibraryGame game : games){
            count += TabMembership.Contains(tab, selection, game) ? 1 : 0;
        }
        return count;
    }

    private static int SeriesCount(List<LibraryGame> games, StateSelection selection, Collection<String> allSeriesIds){
        if(selection.IsAll()){
            return new HashSet<>(allSeriesIds).size();
        }
        Set<String> inScope = new HashSet<>();
        for(LibraryGame game : games){
            if(TabMembership.Contains(MainTab.SERIES, selection, game)){
                inScope.add(game.GetReview().GetSeriesId());
            }
        }
        return inScope.size();
    }
}
