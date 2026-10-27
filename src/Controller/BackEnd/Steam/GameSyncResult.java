package Controller.BackEnd.Steam;

// How one game's achievement sync went. SYNCED and NO_ACHIEVEMENTS both saved something (the second is a game that has none, which
// is stored as 0 of 0 so it isn't asked about again). SKIPPED and NOT_STORED made no call. NOT_PUBLIC and FAILED saved nothing.
// previousTotal/previousUnlocked are what the game held before this sync (null if it had never been synced)
public class GameSyncResult {
    public enum Outcome { SYNCED, NO_ACHIEVEMENTS, SKIPPED, NOT_STORED, NOT_PUBLIC, FAILED }

    private int appId;
    private Outcome outcome;
    private String message;
    private MergedAchievements merged;
    private Integer previousTotal, previousUnlocked;
    private boolean fetchedSchema;

    private GameSyncResult(int appId, Outcome outcome, String message){
        this.appId = appId;
        this.outcome = outcome;
        this.message = message;
    }

    public static GameSyncResult Saved(int appId, MergedAchievements merged, Integer previousTotal, Integer previousUnlocked, boolean fetchedSchema){
        GameSyncResult result = new GameSyncResult(appId, merged.GetTotal() == 0 ? Outcome.NO_ACHIEVEMENTS : Outcome.SYNCED, null);
        result.merged = merged;
        result.previousTotal = previousTotal;
        result.previousUnlocked = previousUnlocked;
        result.fetchedSchema = fetchedSchema;
        return result;
    }

    public static GameSyncResult Skipped(int appId){ return new GameSyncResult(appId, Outcome.SKIPPED, null); }
    public static GameSyncResult NotStored(int appId){ return new GameSyncResult(appId, Outcome.NOT_STORED, "The game isn't in the library yet (sync the library first)"); }
    public static GameSyncResult NotPublic(int appId, String message){ return new GameSyncResult(appId, Outcome.NOT_PUBLIC, message); }
    public static GameSyncResult Failed(int appId, String message){ return new GameSyncResult(appId, Outcome.FAILED, message); }

    public int GetAppId(){ return appId; }
    public Outcome GetOutcome(){ return outcome; }

    // Why it wasn't saved (null when it was, or when it was skipped)
    public String GetMessage(){ return message; }

    // What was saved; null unless SYNCED or NO_ACHIEVEMENTS
    public MergedAchievements GetMerged(){ return merged; }

    public Integer GetPreviousTotal(){ return previousTotal; }
    public Integer GetPreviousUnlocked(){ return previousUnlocked; }

    // Whether the schema and rarity were fetched this time (not just the player's unlocks)
    public boolean DidFetchSchema(){ return fetchedSchema; }

    public boolean WasSaved(){ return outcome == Outcome.SYNCED || outcome == Outcome.NO_ACHIEVEMENTS; }
}
