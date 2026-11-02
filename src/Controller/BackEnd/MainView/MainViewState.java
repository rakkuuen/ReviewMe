package Controller.BackEnd.MainView;

// What is open in the main view: the State row's choice and the tab. The app opens on All, on its first tab (Overview).
// Changing the state keeps the open tab if the new state has it, otherwise opens the first one (see ViewRules)
public class MainViewState {
    private StateSelection selection = ViewRules.LaunchSelection();
    private MainTab tab = ViewRules.TabsFor(selection).get(0);

    public StateSelection GetSelection(){ return selection; }
    public MainTab GetTab(){ return tab; }

    // One state or All
    public void SelectState(StateSelection next){
        tab = ViewRules.TabAfterStateChange(tab, next);
        selection = next;
    }

    // Goes to a state and a tab together (a tab the state doesn't have opens the state's first one)
    public void Open(ViewTarget target){
        SelectState(target.GetSelection());
        SelectTab(target.GetTab());
    }

    // A tab the current state doesn't have is ignored
    public void SelectTab(MainTab next){
        if(ViewRules.TabsFor(selection).contains(next)){
            tab = next;
        }
    }
}
