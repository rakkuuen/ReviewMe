package Controller.BackEnd.MainView;

// One button of the State row: what it says, which selection it picks, how many games that is, and whether it is the one picked
public class StateItem {
    private final String label;
    private final StateSelection selection;
    private final int count;
    private final boolean selected;

    public StateItem(String label, StateSelection selection, int count, boolean selected){
        this.label = label;
        this.selection = selection;
        this.count = count;
        this.selected = selected;
    }

    public String GetLabel(){ return label; }
    public StateSelection GetSelection(){ return selection; }
    public int GetCount(){ return count; }
    public boolean IsSelected(){ return selected; }
}
