package Controller.BackEnd.MainView;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import Model.Library.LibraryGame;
import Model.Reviews.ReviewState;

// The filters picked in the Library (and on the tab toolbars). It can never hold an option that its states make impossible:
// chips the picked states can't have, and the More achievements tag outside 100%ed, are dropped as it is built.
// Immutable: every change gives a new filter
public class ViewFilter {
    private final StateSelection states;
    private final Set<ReviewState> reviewStates;   // Empty means any review state
    private final boolean moreAchievements;

    public ViewFilter(StateSelection states, Set<ReviewState> reviewStates, boolean moreAchievements){
        Set<ReviewState> kept = EnumSet.noneOf(ReviewState.class);
        for(ReviewState review : reviewStates){
            if(ViewRules.ReviewStatesFor(states).contains(review)){
                kept.add(review);
            }
        }
        this.states = states;
        this.reviewStates = Collections.unmodifiableSet(kept);
        this.moreAchievements = moreAchievements && ViewRules.AppliesMoreAchievementsFilter(states);
    }

    public static ViewFilter Default(){
        return new ViewFilter(ViewRules.DefaultListSelection(), EnumSet.noneOf(ReviewState.class), false);
    }

    public StateSelection GetStates(){ return states; }
    public Set<ReviewState> GetReviewStates(){ return reviewStates; }
    public boolean IsMoreAchievementsOnly(){ return moreAchievements; }

    public ViewFilter WithStates(StateSelection next){
        return new ViewFilter(next, reviewStates, moreAchievements);
    }

    public ViewFilter WithReviewState(ReviewState review, boolean on){
        Set<ReviewState> next = EnumSet.noneOf(ReviewState.class);
        next.addAll(reviewStates);
        if(on){
            next.add(review);
        } else {
            next.remove(review);
        }
        return new ViewFilter(states, next, moreAchievements);
    }

    public ViewFilter WithMoreAchievements(boolean on){
        return new ViewFilter(states, reviewStates, on);
    }

    public boolean Matches(LibraryGame game){
        if(!states.Includes(game.GetStatus().GetGameState())){
            return false;
        }
        if(!reviewStates.isEmpty() && !reviewStates.contains(game.GetStatus().GetReviewState())){
            return false;
        }
        return !moreAchievements || game.GetStatus().HasMoreAchievements();
    }
}
