package Controller.BackEnd.AchievementView;

// One achievement as the page lists it. A hidden achievement that isn't unlocked yet doesn't give its name away. rarity is the share of
// players who have it (null when Steam hasn't said). unlockedAt is epoch seconds for Steam's and null for others; unlockedText is the
// date as shown (empty while locked)
public class AchievementItem {
    private final String title;
    private final String description;
    private final boolean unlocked;
    private final boolean hiddenLocked;
    private final Double rarity;
    private final long sortTime;
    private final String unlockedText;

    public AchievementItem(String title, String description, boolean unlocked, boolean hiddenLocked, Double rarity, long sortTime, String unlockedText){
        this.title = title;
        this.description = description;
        this.unlocked = unlocked;
        this.hiddenLocked = hiddenLocked;
        this.rarity = rarity;
        this.sortTime = sortTime;
        this.unlockedText = unlockedText;
    }

    public String GetTitle(){ return title; }
    public String GetDescription(){ return description; }
    public boolean IsUnlocked(){ return unlocked; }
    public boolean IsHiddenLocked(){ return hiddenLocked; }
    public Double GetRarity(){ return rarity; }

    // When it was unlocked as something to sort by (zero when locked or the time isn't known)
    public long GetSortTime(){ return sortTime; }
    public String GetUnlockedText(){ return unlockedText; }
}
