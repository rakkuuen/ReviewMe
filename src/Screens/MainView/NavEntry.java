package Screens.MainView;

// One button in a NavStrip: its key (see HitMap), its words, an optional count, whether it is the picked one, and what it does.
// A button that is not enabled is drawn muted and does nothing
public class NavEntry {
    private final String key;
    private final String label;
    private final Integer count;
    private final boolean selected;
    private final boolean enabled;
    private final Runnable action;

    public NavEntry(String key, String label, Integer count, boolean selected, boolean enabled, Runnable action){
        this.key = key;
        this.label = label;
        this.count = count;
        this.selected = selected;
        this.enabled = enabled;
        this.action = action;
    }

    public String GetKey(){ return key; }
    public String GetLabel(){ return label; }

    // Null for a button with no count
    public Integer GetCount(){ return count; }

    public boolean IsSelected(){ return selected; }
    public boolean IsEnabled(){ return enabled; }
    public Runnable GetAction(){ return action; }
}
