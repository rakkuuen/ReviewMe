package Controller.BackEnd.SyncRun;

// The words of each sync state, as the Sync boards have them
public class SyncWords {
    private static final String dot = " " + (char) 0x00B7 + " ";   // A middle dot, by code point so the source stays plain ASCII

    public static final String syncButton = "Sync";
    public static final String retryButton = "Retry";
    public static final String settingsButton = "Open settings";

    public static SyncSnapshot Idle(){
        return new SyncSnapshot(SyncState.IDLE, 0, 0, "", "", false, false);
    }

    // The library is being fetched (total not known yet) or the achievements are being synced game by game
    public static SyncSnapshot Running(int done, int total){
        String headline = total <= 0 ? "Syncing" + (char) 0x2026 : "Syncing " + done + " of " + total + " games";
        return new SyncSnapshot(SyncState.RUNNING, done, total, headline, "", false, false);
    }

    // "Synced 372 games . 6 achievement lists updated"
    public static SyncSnapshot Success(int games, int achievementListsUpdated){
        String headline = "Synced " + games + (games == 1 ? " game" : " games");
        if(achievementListsUpdated > 0){
            headline += dot + achievementListsUpdated + (achievementListsUpdated == 1 ? " achievement list updated" : " achievement lists updated");
        }
        return new SyncSnapshot(SyncState.SUCCESS, 0, 0, headline, "", false, false);
    }

    public static SyncSnapshot Failure(SyncState state, String extra){
        switch(state){
            case INVALID_KEY:
                return new SyncSnapshot(state, 0, 0, "Steam API key rejected", "Steam did not accept your API key (or none is set up). Check it in Settings, then retry.", true, true);
            case PRIVATE_PROFILE:
                return new SyncSnapshot(state, 0, 0, "Steam profile is private", "ReviewMe cannot read your games. In Steam, set Game details to Public, then retry.", true, false);
            case OFFLINE:
                return new SyncSnapshot(state, 0, 0, "You are offline", "Could not reach Steam. Showing data from the last sync.", true, false);
            case RATE_LIMITED:
                return new SyncSnapshot(state, 0, 0, "Steam is limiting requests", "Too many requests in a short time. Wait about 5 minutes, then retry.", true, false);
            default:
                return new SyncSnapshot(SyncState.FAILED, 0, 0, "Sync stopped", extra == null || extra.isEmpty() ? "Something went wrong. Try again." : extra, true, false);
        }
    }
}
