package Controller.BackEnd.MainView;

// One tab of the tab bar: what it says, which tab it opens, how many it holds, and whether it is the open one
public class TabItem {
    private final String label;
    private final MainTab tab;
    private final int count;
    private final boolean selected;

    public TabItem(String label, MainTab tab, int count, boolean selected){
        this.label = label;
        this.tab = tab;
        this.count = count;
        this.selected = selected;
    }

    public String GetLabel(){ return label; }
    public MainTab GetTab(){ return tab; }
    public int GetCount(){ return count; }
    public boolean IsSelected(){ return selected; }
}
