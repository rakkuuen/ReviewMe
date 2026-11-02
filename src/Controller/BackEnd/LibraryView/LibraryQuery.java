package Controller.BackEnd.LibraryView;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import Controller.BackEnd.ListTab.ListRules;
import Controller.BackEnd.ListTab.ListSort;
import Controller.BackEnd.MainView.StateSelection;
import Controller.BackEnd.MainView.ViewFilter;
import Model.Reviews.GameState;
import Model.Reviews.ReviewState;

// What is picked in the Library: the search words, the order, the states, review chips and More achievements tag (a ViewFilter, which
// drops anything the picked states can't have), and how many more pages have been asked for. Immutable: every change gives a new
// query, and any change to what is listed starts again from the first page
public class LibraryQuery {
    // The orders the Library offers, in the order the sort button goes through them
    public static final List<ListSort> sorts = Collections.unmodifiableList(Arrays.asList(ListSort.TITLE, ListSort.PLAYTIME, ListSort.RATING, ListSort.LAST_PLAYED));

    private final String search;
    private final ListSort sort;
    private final boolean descending;
    private final ViewFilter filter;
    private final int morePages;

    private LibraryQuery(String search, ListSort sort, boolean descending, ViewFilter filter, int morePages){
        this.search = search;
        this.sort = sort;
        this.descending = descending;
        this.filter = filter;
        this.morePages = morePages;
    }

    // How the Library opens: 100%ed games, titles A to Z, nothing else picked
    public static LibraryQuery Initial(){
        return new LibraryQuery("", ListSort.TITLE, false, ViewFilter.Default(), 0);
    }

    public String GetSearch(){ return search; }
    public ListSort GetSort(){ return sort; }
    public boolean IsDescending(){ return descending; }
    public ViewFilter GetFilter(){ return filter; }
    public StateSelection GetStates(){ return filter.GetStates(); }
    public int GetMorePages(){ return morePages; }

    public LibraryQuery WithSearch(String text){
        return new LibraryQuery(text == null ? "" : text.trim(), sort, descending, filter, 0);
    }

    // A new order takes its usual direction (titles A to Z, the rest high to low); the same one again flips it
    public LibraryQuery WithSort(ListSort next){
        boolean flipped = next == sort ? !descending : ListRules.DefaultDescending(next);
        return new LibraryQuery(search, next, flipped, filter, 0);
    }

    // The sort button's choice: the next order in the list
    public LibraryQuery WithNextSort(){
        return WithSort(sorts.get((sorts.indexOf(sort) + 1) % sorts.size()));
    }

    // One state turned on or off. Turning every state off is the same as All
    public LibraryQuery WithState(GameState state, boolean on){
        return new LibraryQuery(search, sort, descending, filter.WithStates(filter.GetStates().With(state, on)), 0);
    }

    public LibraryQuery WithAllStates(){
        return new LibraryQuery(search, sort, descending, filter.WithStates(StateSelection.All()), 0);
    }

    public LibraryQuery WithReviewState(ReviewState review, boolean on){
        return new LibraryQuery(search, sort, descending, filter.WithReviewState(review, on), 0);
    }

    public LibraryQuery WithMoreAchievements(boolean on){
        return new LibraryQuery(search, sort, descending, filter.WithMoreAchievements(on), 0);
    }

    // Everything off: no search, every state, no chips. The order stays ("Clear all")
    public LibraryQuery Cleared(){
        return new LibraryQuery("", sort, descending, new ViewFilter(StateSelection.All(), EnumSet.noneOf(ReviewState.class), false), 0);
    }

    public LibraryQuery WithMorePages(){
        return new LibraryQuery(search, sort, descending, filter, morePages + 1);
    }

    public Set<ReviewState> GetReviewStates(){ return filter.GetReviewStates(); }
    public boolean IsMoreAchievementsOnly(){ return filter.IsMoreAchievementsOnly(); }
}
