package Controller.BackEnd.AchievementView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Everything the achievements page shows: the title, the summary figures and every achievement. Which ones are listed (the filter) and in
// what order (the sort) is worked out from this by Visible, so the screen only keeps what is picked
public class AchievementViewData {
    private final String title;
    private final int unlocked;
    private final int total;
    private final String percentText;
    private final String lockedText;
    private final double fraction;
    private final String lastUnlockedText;
    private final String rarestText;
    private final List<AchievementItem> items;

    public AchievementViewData(String title, int unlocked, int total, String percentText, String lockedText, double fraction, String lastUnlockedText, String rarestText,
            List<AchievementItem> items){
        this.title = title;
        this.unlocked = unlocked;
        this.total = total;
        this.percentText = percentText;
        this.lockedText = lockedText;
        this.fraction = fraction;
        this.lastUnlockedText = lastUnlockedText;
        this.rarestText = rarestText;
        this.items = Collections.unmodifiableList(items);
    }

    public String GetTitle(){ return title; }
    public int GetUnlocked(){ return unlocked; }
    public int GetTotal(){ return total; }

    // "94% complete"
    public String GetPercentText(){ return percentText; }

    // "3 locked . 1 hidden"
    public String GetLockedText(){ return lockedText; }

    // How full the bar is, 0 to 1
    public double GetFraction(){ return fraction; }

    // "28 Sep 2026", or a dash when nothing is unlocked
    public String GetLastUnlockedText(){ return lastUnlockedText; }

    // The smallest share of players among the unlocked ones ("2.1%"), or a dash
    public String GetRarestText(){ return rarestText; }

    public List<AchievementItem> GetItems(){ return items; }

    // How many achievements each filter would list
    public int Count(AchievementFilter filter){
        int count = 0;
        for(AchievementItem item : items){
            count += filter.Includes(item) ? 1 : 0;
        }
        return count;
    }

    // The achievements the filter lets through, in the sort's order
    public List<AchievementItem> Visible(AchievementFilter filter, AchievementSort sort){
        List<AchievementItem> shown = new ArrayList<>();
        for(AchievementItem item : items){
            if(filter.Includes(item)){
                shown.add(item);
            }
        }
        shown.sort(sort.Order());
        return shown;
    }
}
