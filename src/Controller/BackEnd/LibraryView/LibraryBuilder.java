package Controller.BackEnd.LibraryView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

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
import Model.Reviews.Series;

// Works out the Library: which games match the search and filters, their order, the first pages of them as rows, and the chips with
// their counts. No drawing and no database in here
public class LibraryBuilder {
    public static final String stateKeyPrefix = "state:";
    public static final String reviewKeyPrefix = "review:";
    public static final String moreAchievementsKey = "tag:more";
    public static final String searchKey = "search";
    public static final String seriesKey = "series:";
    public static final String genreKey = "genre:";
    public static final String platformKey = "platform:";
    public static final String completedKey = "completed:";
    public static final String addedKey = "added:";

    public static LibraryResult Build(List<LibraryGame> games, LibraryQuery query, int pageSize){
        return Build(games, new ArrayList<>(), query, pageSize);
    }

    // allSeries is where the Series filter's names come from
    public static LibraryResult Build(List<LibraryGame> games, List<Series> allSeries, LibraryQuery query, int pageSize){
        List<LibraryGame> matching = new ArrayList<>();
        for(LibraryGame game : games){
            if(query.GetFilter().Matches(game) && query.GetExtras().Matches(game) && Matches(game, query.GetSearch())){
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
                ActiveFilters(query, allSeries), SeriesOptions(games, allSeries, query), TagOptions(games, query, true), TagOptions(games, query, false));
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

    // Every series that has a game, with how many, in name order
    private static List<ListChip> SeriesOptions(List<LibraryGame> games, List<Series> allSeries, LibraryQuery query){
        Map<String, Integer> counts = new LinkedHashMap<>();
        for(LibraryGame game : games){
            if(game.HasReview() && game.GetReview().GetSeriesId() != null){
                counts.merge(game.GetReview().GetSeriesId(), 1, Integer::sum);
            }
        }
        List<ListChip> options = new ArrayList<>();
        for(Series series : allSeries){
            if(counts.containsKey(series.GetId())){
                options.add(new ListChip(series.GetId(), series.GetName(), counts.get(series.GetId()), query.GetExtras().GetSeriesIds().contains(series.GetId())));
            }
        }
        options.sort((a, b) -> a.GetLabel().compareToIgnoreCase(b.GetLabel()));
        return options;
    }

    // Every genre (or platform) any review has, with how many, in name order
    private static List<ListChip> TagOptions(List<LibraryGame> games, LibraryQuery query, boolean genres){
        Map<String, Integer> counts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for(LibraryGame game : games){
            if(!game.HasReview()){
                continue;
            }
            List<String> values = genres ? game.GetReview().GetGenres() : game.GetReview().GetPlatforms();
            if(values != null){
                for(String value : new java.util.LinkedHashSet<>(values)){
                    counts.merge(value, 1, Integer::sum);
                }
            }
        }
        List<ListChip> options = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : counts.entrySet()){
            boolean on = (genres ? query.GetExtras().GetGenres() : query.GetExtras().GetPlatforms()).contains(entry.getKey());
            options.add(new ListChip(entry.getKey(), entry.getKey(), entry.getValue(), on));
        }
        return options;
    }

    private static List<LibraryResult.ActiveFilter> ActiveFilters(LibraryQuery query, List<Series> allSeries){
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
        if(!query.GetExtras().GetSeriesIds().isEmpty()){
            List<String> names = new ArrayList<>();
            for(String id : query.GetExtras().GetSeriesIds()){
                names.add(SeriesName(id, allSeries));
            }
            filters.add(new LibraryResult.ActiveFilter(seriesKey, "Series: " + String.join(", ", names)));
        }
        if(!query.GetExtras().GetGenres().isEmpty()){
            filters.add(new LibraryResult.ActiveFilter(genreKey, "Genre: " + String.join(", ", query.GetExtras().GetGenres())));
        }
        if(!query.GetExtras().GetPlatforms().isEmpty()){
            filters.add(new LibraryResult.ActiveFilter(platformKey, "Platform: " + String.join(", ", query.GetExtras().GetPlatforms())));
        }
        if(query.GetExtras().GetCompleted().IsSet()){
            filters.add(new LibraryResult.ActiveFilter(completedKey, "Completed: " + query.GetExtras().GetCompleted().Describe()));
        }
        if(query.GetExtras().GetAdded().IsSet()){
            filters.add(new LibraryResult.ActiveFilter(addedKey, "Added: " + query.GetExtras().GetAdded().Describe()));
        }
        if(!query.GetSearch().isEmpty()){
            filters.add(new LibraryResult.ActiveFilter(searchKey, "Search: " + query.GetSearch()));
        }
        return filters;
    }

    private static String SeriesName(String id, List<Series> allSeries){
        for(Series series : allSeries){
            if(series.GetId().equals(id)){
                return series.GetName();
            }
        }
        return id;
    }

    private static String ReviewWords(ReviewState review){
        switch(review){
            case COMPLETE: return "Complete";
            case PARTIAL: return "Partial";
            default: return "No review yet";
        }
    }
}
