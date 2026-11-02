package Controller.BackEnd.ListTab;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import Controller.BackEnd.MainView.MainTab;
import Model.Reviews.ReviewState;

// What is picked on a list tab's toolbar: the search words, the order, which review-state and reason chips are on (none on means
// any), and how many more pages have been asked for. Immutable: every change gives a new query, and any change to what is listed
// starts again from the first page
public class ListQuery {
    private final String search;
    private final ListSort sort;
    private final boolean descending;
    private final Set<ReviewState> reviewStates;
    private final Set<String> reasons;
    private final int morePages;

    private ListQuery(String search, ListSort sort, boolean descending, Set<ReviewState> reviewStates, Set<String> reasons, int morePages){
        this.search = search;
        this.sort = sort;
        this.descending = descending;
        Set<ReviewState> review = EnumSet.noneOf(ReviewState.class);
        review.addAll(reviewStates);
        this.reviewStates = Collections.unmodifiableSet(review);
        this.reasons = Collections.unmodifiableSet(new HashSet<>(reasons));
        this.morePages = morePages;
    }

    // How a tab starts: no search, its usual order, no chips
    public static ListQuery Initial(MainTab tab){
        ListSort sort = ListRules.DefaultSort(tab);
        return new ListQuery("", sort, ListRules.DefaultDescending(sort), EnumSet.noneOf(ReviewState.class), new HashSet<>(), 0);
    }

    public String GetSearch(){ return search; }
    public ListSort GetSort(){ return sort; }
    public boolean IsDescending(){ return descending; }
    public Set<ReviewState> GetReviewStates(){ return reviewStates; }
    public Set<String> GetReasons(){ return reasons; }
    public int GetMorePages(){ return morePages; }

    public ListQuery WithSearch(String text){
        return new ListQuery(text == null ? "" : text.trim(), sort, descending, reviewStates, reasons, 0);
    }

    // Picking a new order uses its usual direction; picking the same one again flips it
    public ListQuery WithSort(ListSort next){
        boolean flipped = next == sort ? !descending : ListRules.DefaultDescending(next);
        return new ListQuery(search, next, flipped, reviewStates, reasons, 0);
    }

    public ListQuery WithReviewState(ReviewState review, boolean on){
        Set<ReviewState> next = EnumSet.noneOf(ReviewState.class);
        next.addAll(reviewStates);
        if(on){
            next.add(review);
        } else {
            next.remove(review);
        }
        return new ListQuery(search, sort, descending, next, reasons, 0);
    }

    public ListQuery WithReason(String key, boolean on){
        Set<String> next = new HashSet<>(reasons);
        if(on){
            next.add(key);
        } else {
            next.remove(key);
        }
        return new ListQuery(search, sort, descending, reviewStates, next, 0);
    }

    // Search words and chips off, the order kept ("Clear filters")
    public ListQuery Cleared(){
        return new ListQuery("", sort, descending, EnumSet.noneOf(ReviewState.class), new HashSet<>(), 0);
    }

    public ListQuery WithMorePages(){
        return new ListQuery(search, sort, descending, reviewStates, reasons, morePages + 1);
    }

    public boolean IsFiltered(){
        return !search.isEmpty() || !reviewStates.isEmpty() || !reasons.isEmpty();
    }
}
