package Controller.BackEnd.MainView;

// A place in the main view: a State row choice and a tab
public class ViewTarget {
    private final StateSelection selection;
    private final MainTab tab;

    public ViewTarget(StateSelection selection, MainTab tab){
        this.selection = selection;
        this.tab = tab;
    }

    public StateSelection GetSelection(){ return selection; }
    public MainTab GetTab(){ return tab; }
}
