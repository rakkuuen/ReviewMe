package Controller.BackEnd.ListTab;

// One toggle on a list's toolbar: what it says, how many games are behind it, and whether it is on. The key is what the screen
// hands back when it is clicked
public class ListChip {
    private final String key;
    private final String label;
    private final int count;
    private final boolean on;

    public ListChip(String key, String label, int count, boolean on){
        this.key = key;
        this.label = label;
        this.count = count;
        this.on = on;
    }

    public String GetKey(){ return key; }
    public String GetLabel(){ return label; }
    public int GetCount(){ return count; }
    public boolean IsOn(){ return on; }
}
