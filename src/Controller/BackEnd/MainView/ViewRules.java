package Controller.BackEnd.MainView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Model.Reviews.GameState;
import Model.Reviews.ReviewState;

// What the main view and Library offer for a State row selection. The screens only ask; every rule lives here
public class ViewRules {

    // The app opens on All, which is where the Overview lives
    public static StateSelection LaunchSelection(){
        return StateSelection.All();
    }

    // The list views and the Library start on 100%ed
    public static StateSelection DefaultListSelection(){
        return StateSelection.Of(GameState.COMPLETED);
    }

    // The tabs that exist for one state or All, in order. Several states at once (the Library) have no tabs
    public static List<MainTab> TabsFor(StateSelection selection){
        if(selection.IsAll()){
            return Arrays.asList(MainTab.OVERVIEW, MainTab.SERIES);
        }
        if(!selection.IsSingle()){
            throw new IllegalArgumentException("Tabs exist for one state or All, not several");
        }
        if(selection.GetOnly() == GameState.COMPLETED){
            return Arrays.asList(MainTab.COMPLETED, MainTab.INCOMPLETE, MainTab.MORE_ACHIEVEMENTS, MainTab.SERIES);
        }
        return Arrays.asList(MainTab.GAMES, MainTab.SERIES);
    }

    // After the State row changes the open tab stays if the new state has it, otherwise the first tab opens
    public static MainTab TabAfterStateChange(MainTab current, StateSelection selection){
        List<MainTab> tabs = TabsFor(selection);
        return tabs.contains(current) ? current : tabs.get(0);
    }

    // The review states a game in any picked state can have, in the order the chips show. Only a 100%ed game can have a complete
    // review, and a game that hasn't been started can only have no review
    public static List<ReviewState> ReviewStatesFor(StateSelection selection){
        List<ReviewState> possible = new ArrayList<>();
        for(ReviewState review : ReviewState.values()){
            for(GameState state : GameState.values()){
                if(selection.Includes(state) && CanHave(state, review) && !possible.contains(review)){
                    possible.add(review);
                }
            }
        }
        return possible;
    }

    // The Library's Review row is hidden when there is nothing to choose between (only Not started picked)
    public static boolean ShowsReviewRow(StateSelection selection){
        return ReviewStatesFor(selection).size() > 1;
    }

    // The More achievements tag only exists on 100%ed games
    public static boolean AppliesMoreAchievementsFilter(StateSelection selection){
        return selection.Includes(GameState.COMPLETED);
    }

    // The review chips a tab offers: Completed is complete reviews, Incomplete is partial or none, More achievements can be any
    public static List<ReviewState> ReviewStatesForTab(MainTab tab, StateSelection selection){
        switch(tab){
            case COMPLETED: return Arrays.asList(ReviewState.COMPLETE);
            case INCOMPLETE: return Arrays.asList(ReviewState.PARTIAL, ReviewState.NONE);
            case MORE_ACHIEVEMENTS: return Arrays.asList(ReviewState.values());
            case GAMES: return ReviewStatesFor(selection);
            default: return new ArrayList<>();
        }
    }

    private static boolean CanHave(GameState state, ReviewState review){
        switch(state){
            case COMPLETED: return true;
            case NOT_STARTED: return review == ReviewState.NONE;
            default: return review != ReviewState.COMPLETE;
        }
    }
}
