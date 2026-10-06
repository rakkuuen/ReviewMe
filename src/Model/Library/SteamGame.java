package Model.Library;

// Mirror of one owned game from the Steam API. Times are Steam's raw epoch seconds; null means not synced yet
public class SteamGame {
    private int appId;
    private String name, iconHash;
    private Integer playtimeMinutes, totalAchievements, unlockedAchievements;
    private Long lastPlayed, syncedAt;

    public SteamGame(int appId, String name){
        this.appId = appId;
        this.name = name;
    }

    public int GetAppId(){ return appId; }

    public String GetName(){ return name; }
    public void SetName(String name){ this.name = name; }

    public Integer GetPlaytimeMinutes(){ return playtimeMinutes; }
    public void SetPlaytimeMinutes(Integer playtimeMinutes){ this.playtimeMinutes = playtimeMinutes; }

    public Long GetLastPlayed(){ return lastPlayed; }
    public void SetLastPlayed(Long lastPlayed){ this.lastPlayed = lastPlayed; }

    public String GetIconHash(){ return iconHash; }
    public void SetIconHash(String iconHash){ this.iconHash = iconHash; }

    public Integer GetTotalAchievements(){ return totalAchievements; }
    public void SetTotalAchievements(Integer totalAchievements){ this.totalAchievements = totalAchievements; }

    public Integer GetUnlockedAchievements(){ return unlockedAchievements; }
    public void SetUnlockedAchievements(Integer unlockedAchievements){ this.unlockedAchievements = unlockedAchievements; }

    public Long GetSyncedAt(){ return syncedAt; }
    public void SetSyncedAt(Long syncedAt){ this.syncedAt = syncedAt; }

    public boolean HasAchievements(){
        return totalAchievements != null && totalAchievements > 0;
    }

    public boolean IsHundredPercent(){
        return HasAchievements() && unlockedAchievements != null && unlockedAchievements.equals(totalAchievements);
    }

    // Synced, zero hours and never launched. Unsynced games are not claimed as unplayed
    public boolean IsNotStarted(){
        return playtimeMinutes != null && playtimeMinutes == 0 && (lastPlayed == null || lastPlayed == 0);
    }
}
