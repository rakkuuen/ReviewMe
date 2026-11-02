package Controller.BackEnd.AchievementView;

import java.util.Comparator;

// The orders the achievements can be listed in. Locked achievements and ones with no figure go last whichever order it is
public enum AchievementSort {
    UNLOCK_DATE("Unlock date"),
    RARITY("Rarity"),
    NAME("Name");

    private final String label;

    AchievementSort(String label){
        this.label = label;
    }

    public String GetLabel(){ return label; }

    // The order after this one, going round
    public AchievementSort Next(){
        AchievementSort[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    public Comparator<AchievementItem> Order(){
        Comparator<AchievementItem> byName = (a, b) -> a.GetTitle().compareToIgnoreCase(b.GetTitle());
        switch(this){
            case UNLOCK_DATE:
                // Newest first; unlocked ones with a time, then unlocked with none, then the locked
                return Comparator.comparingInt((AchievementItem item) -> item.IsUnlocked() ? (item.GetSortTime() > 0 ? 0 : 1) : 2)
                        .thenComparing((a, b) -> Long.compare(b.GetSortTime(), a.GetSortTime())).thenComparing(byName);
            case RARITY:
                // Rarest first; achievements with no figure last
                return Comparator.comparingInt((AchievementItem item) -> item.GetRarity() == null ? 1 : 0)
                        .thenComparing((a, b) -> a.GetRarity() == null || b.GetRarity() == null ? 0 : Double.compare(a.GetRarity(), b.GetRarity())).thenComparing(byName);
            default:
                return byName;
        }
    }
}
