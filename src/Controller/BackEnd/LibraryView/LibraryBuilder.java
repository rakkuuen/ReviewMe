package Controller.BackEnd.LibraryView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Controller.BackEnd.ListTab.ListChip;
import Controller.BackEnd.ListTab.ListOrder;
import Controller.BackEnd.MainView.MainViewLabels;
import Controller.BackEnd.MainView.StateSelection;
import Controller.BackEnd.MainView.ViewCounter;
import Controller.BackEnd.MainView.ViewRules;
import Controller.BackEnd.Overview.GameRow;
import Controller.BackEnd.Overview.OverviewRows;
import Model.Library.LibraryGame;
import Model.Reviews.GameState;
import Model.Reviews.ReviewState;

// Works out the Library: which games match the search and filters, their order, the first pages of them as rows, and the chips with
// their counts. No drawing and no database in here
public class LibraryBuilder {
    public static final String stateKeyPrefix = "state:";
    public static final String reviewKeyPrefix = "review:";
    public static final String moreAchievementsKey = "tag:more";
    public static final String searchKey = "search";

    public static LibraryResult Build(List<LibraryGame> games, LibraryQuery query, int pageSize){
        List<LibraryGame> matching = new ArrayList<>();
        for(LibraryGame game : games){
            if(query.GetFilter().Matches(game) && Matches(game, query.GetSearch())){
                matching.add(game);
            }
        }
        matching.sort(ListOrder.Of(query.GetSort(), query.IsDescending()));

        int shown = Math.min(matching.size(), pageSize * (query.GetMorePages() + 1));
        List<GameRow> rows = new ArrayList<>();
        for(LibraryGame game : matching.subList(0, shown)){
            rows.add(OverviewRows.BuildForSeries(game));
        }
        StateSelection states = query.GetStates();
        return new LibraryResult(rows, matching.size(), games.size(), StateChips(games, states), ReviewChips(games, query), MoreAchievementsChip(games, query),
                ActiveFilters(query));
    }

    private static boolean Matches(LibraryGame game, String search){
        return search.isEmpty() || game.GetTitle().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }

    // One chip per game state with the number of games in it; a chip is on when its state is picked (none on means all)
    private static List<ListChip> StateChips(List<LibraryGame> games, StateSelection states){
        List<ListChip> chips = new ArrayList<>();
        for(GameState state : GameState.values()){
            chips.add(new ListChip(stateKeyPrefix + state.name(), MainViewLabels.StateLabel(state), ViewCounter.StateCount(games, StateSelection.Of(state)), !states.IsAll() && states.Includes(state)));
        }
        return chips;
    }

    // The review chips the picked states can have, counted among the games in those states. None when there is nothing to choose between
    private static List<ListChip> ReviewChips(List<LibraryGame> games, LibraryQuery query){
        List<ListChip> chips = new ArrayList<>();
        if(!ViewRules.ShowsReviewRow(query.GetStates())){
            return chips;
        }
        Map<ReviewState, Integer> counts = ViewCounter.ReviewCounts(games, null, query.GetStates());
        for(ReviewState review : ViewRules.ReviewStatesFor(query.GetStates())){
            chips.add(new ListChip(reviewKeyPrefix + review.name(), ReviewWords(review), counts.getOrDefault(review, 0), query.GetReviewStates().contains(review)));
        }
        return chips;
    }

    private static ListChip MoreAchievementsChip(List<LibraryGame> games, LibraryQuery query){
        if(!ViewRules.AppliesMoreAchievementsFilter(query.GetStates())){
            return null;
        }
        return new ListChip(moreAchievementsKey, "Has More achievements tag", ViewCounter.MoreAchievementsCount(games, query.GetStates()), query.IsMoreAchievementsOnly());
    }

    private static List<LibraryResult.ActiveFilter> ActiveFilters(LibraryQuery query){
        List<LibraryResult.ActiveFilter> filters = new ArrayList<>();
        if(!query.GetStates().IsAll()){
            List<String> names = new ArrayList<>();
            for(GameState state : GameState.values()){
                if(query.GetStates().Includes(state)){
                    names.add(MainViewLabels.StateLabel(state));
                }
            }
            filters.add(new LibraryResult.ActiveFilter(stateKeyPrefix, "State: " + String.join(", ", names)));
        }
        for(ReviewState review : ReviewState.values()){
            if(query.GetReviewStates().contains(review)){
                filters.add(new LibraryResult.ActiveFilter(reviewKeyPrefix + review.name(), "Review: " + ReviewWords(review)));
            }
        }
        if(query.IsMoreAchievementsOnly()){
            filters.add(new LibraryResult.ActiveFilter(moreAchievementsKey, "Tag: Has More achievements"));
        }
        if(!query.GetSearch().isEmpty()){
            filters.add(new LibraryResult.ActiveFilter(searchKey, "Search: " + query.GetSearch()));
        }
        return filters;
    }

    private static String ReviewWords(ReviewState review){
        switch(review){
            case COMPLETE: return "Complete";
            case PARTIAL: return "Partial";
            default: return "No review yet";
        }
    }
}
