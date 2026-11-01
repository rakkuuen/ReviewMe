package Controller.BackEnd.MainView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import Model.Library.LibraryGame;
import Model.Reviews.GameState;

// The State row and tab bar as they should be drawn right now: every item, its count, and which are picked. Built from the games and
// what is open, and nothing else, so drawing it needs no rules of its own
public class MainViewModel {
    private final List<StateItem> stateItems;
    private final List<TabItem> tabItems;

    private MainViewModel(List<StateItem> stateItems, List<TabItem> tabItems){
        this.stateItems = Collections.unmodifiableList(stateItems);
        this.tabItems = Collections.unmodifiableList(tabItems);
    }

    public List<StateItem> GetStateItems(){ return stateItems; }
    public List<TabItem> GetTabItems(){ return tabItems; }

    // allSeriesIds is every series that exists, so an empty one still counts under All
    public static MainViewModel Build(List<LibraryGame> games, Collection<String> allSeriesIds, MainViewState state){
        List<StateItem> states = new ArrayList<>();
        states.add(StateItemFor(games, state, null));
        for(GameState gameState : GameState.values()){
            states.add(StateItemFor(games, state, gameState));
        }

        Map<MainTab, Integer> counts = ViewCounter.TabCounts(games, state.GetSelection(), allSeriesIds);
        List<TabItem> tabs = new ArrayList<>();
        for(MainTab tab : ViewRules.TabsFor(state.GetSelection())){
            tabs.add(new TabItem(MainViewLabels.TabLabel(tab), tab, counts.get(tab), tab == state.GetTab()));
        }
        return new MainViewModel(states, tabs);
    }

    // The button for one state, or for All when gameState is null
    private static StateItem StateItemFor(List<LibraryGame> games, MainViewState state, GameState gameState){
        StateSelection selection = gameState == null ? StateSelection.All() : StateSelection.Of(gameState);
        return new StateItem(MainViewLabels.StateLabel(gameState), selection, ViewCounter.StateCount(games, selection), selection.equals(state.GetSelection()));
    }
}
