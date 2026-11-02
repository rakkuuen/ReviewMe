package Controller.BackEnd.AchievementView;

// Which achievements the list shows
public enum AchievementFilter {
    ALL("All"),
    UNLOCKED("Unlocked"),
    LOCKED("Locked");

    private final String label;

    AchievementFilter(String label){
        this.label = label;
    }

    public String GetLabel(){ return label; }

    public boolean Includes(AchievementItem item){
        switch(this){
            case UNLOCKED: return item.IsUnlocked();
            case LOCKED: return !item.IsUnlocked();
            default: return true;
        }
    }
}
