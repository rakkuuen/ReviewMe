package Controller.BackEnd.Achievements;

import java.util.List;

import Model.Library.SteamGame;
import Model.Reviews.ManualAchievement;

// A game's achievement counts, Steam's and the ones kept by hand side by side. Only the Steam counts are compared with Steam, and
// only they drive a game's state (Completed, In progress). The manual ones add to the overall figure and, once all done, earn a badge
public class AchievementTotals {
    private int steamTotal, steamUnlocked, manualTotal, manualUnlocked;
    private boolean steamKnown;

    private AchievementTotals(){}

    // steam is the game's Steam record or null. manual is the review's own list
    public static AchievementTotals Of(SteamGame steam, List<ManualAchievement> manual){
        AchievementTotals totals = new AchievementTotals();
        if(steam != null && steam.GetTotalAchievements() != null){
            totals.steamKnown = true;
            totals.steamTotal = steam.GetTotalAchievements();
            totals.steamUnlocked = steam.GetUnlockedAchievements() == null ? 0 : steam.GetUnlockedAchievements();
        }
        for(ManualAchievement achievement : manual){
            totals.manualTotal++;
            totals.manualUnlocked += achievement.IsUnlocked() ? 1 : 0;
        }
        return totals;
    }

    // Whether Steam's counts are known: the game has a synced Steam record (a total of 0 is known: it has none)
    public boolean IsSteamKnown(){ return steamKnown; }
    public int GetSteamTotal(){ return steamTotal; }
    public int GetSteamUnlocked(){ return steamUnlocked; }

    public int GetManualTotal(){ return manualTotal; }
    public int GetManualUnlocked(){ return manualUnlocked; }

    public int GetOverallTotal(){ return steamTotal + manualTotal; }
    public int GetOverallUnlocked(){ return steamUnlocked + manualUnlocked; }

    public boolean HasManualAchievements(){ return manualTotal > 0; }

    // Every achievement kept by hand is done (false when there are none): the extra badge
    public boolean AllManualDone(){ return manualTotal > 0 && manualUnlocked == manualTotal; }
}
