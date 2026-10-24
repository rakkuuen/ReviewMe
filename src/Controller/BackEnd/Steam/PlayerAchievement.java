package Controller.BackEnd.Steam;

// One achievement as the player's own stats report it: whether it's unlocked and when (Steam's epoch seconds, null if never)
public class PlayerAchievement {
    private String apiName;
    private boolean unlocked;
    private Long unlockedAt;

    public PlayerAchievement(String apiName, boolean unlocked, Long unlockedAt){
        this.apiName = apiName;
        this.unlocked = unlocked;
        this.unlockedAt = unlockedAt;
    }

    public String GetApiName(){ return apiName; }
    public boolean IsUnlocked(){ return unlocked; }
    public Long GetUnlockedAt(){ return unlockedAt; }
}
