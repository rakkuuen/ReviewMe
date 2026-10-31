package Controller.BackEnd.MainView;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import Model.Reviews.GameState;

// Which game states are picked in the State row: none picked means all games. The main view picks one state or All; the Library
// may pick several. Picking every state is the same as picking none, so there is only one way to say All
public class StateSelection {
    private final Set<GameState> states;

    private StateSelection(Set<GameState> states){
        boolean everyState = states.size() == GameState.values().length;
        this.states = Collections.unmodifiableSet(everyState ? EnumSet.noneOf(GameState.class) : states);
    }

    public static StateSelection All(){
        return new StateSelection(EnumSet.noneOf(GameState.class));
    }

    public static StateSelection Of(Collection<GameState> picked){
        Set<GameState> copy = EnumSet.noneOf(GameState.class);
        copy.addAll(picked);
        return new StateSelection(copy);
    }

    public static StateSelection Of(GameState... picked){
        return Of(Arrays.asList(picked));
    }

    public boolean IsAll(){ return states.isEmpty(); }

    public boolean IsSingle(){ return states.size() == 1; }

    public Set<GameState> GetStates(){ return states; }

    // The one state picked. Only valid when IsSingle()
    public GameState GetOnly(){
        if(!IsSingle()){
            throw new IllegalStateException("Not exactly one state picked");
        }
        return states.iterator().next();
    }

    public boolean Includes(GameState state){
        return states.isEmpty() || states.contains(state);
    }

    // This selection with one state switched on or off. Switching the last one off leaves All
    public StateSelection With(GameState state, boolean on){
        Set<GameState> next = EnumSet.noneOf(GameState.class);
        next.addAll(states);
        if(on){
            next.add(state);
        } else {
            next.remove(state);
        }
        return new StateSelection(next);
    }

    @Override
    public boolean equals(Object other){
        return other instanceof StateSelection && states.equals(((StateSelection) other).states);
    }

    @Override
    public int hashCode(){
        return states.hashCode();
    }
}
