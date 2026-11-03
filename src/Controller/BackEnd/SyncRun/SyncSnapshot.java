package Controller.BackEnd.SyncRun;

// What the screen shows about a sync at one moment: its state, how far it is, the words, and which buttons the banner offers. Immutable
public class SyncSnapshot {
    private final SyncState state;
    private final int done;
    private final int total;
    private final String headline;
    private final String detail;
    private final boolean canRetry;
    private final boolean opensSettings;

    public SyncSnapshot(SyncState state, int done, int total, String headline, String detail, boolean canRetry, boolean opensSettings){
        this.state = state;
        this.done = done;
        this.total = total;
        this.headline = headline;
        this.detail = detail;
        this.canRetry = canRetry;
        this.opensSettings = opensSettings;
    }

    public SyncState GetState(){ return state; }
    public int GetDone(){ return done; }
    public int GetTotal(){ return total; }

    // "Syncing 118 of 372 games", "Steam API key rejected" ... empty when idle
    public String GetHeadline(){ return headline; }

    // The sentence under a failure's headline (empty otherwise)
    public String GetDetail(){ return detail; }

    // Retry is offered on a failure banner
    public boolean CanRetry(){ return canRetry; }

    // "Open settings" is offered (a key problem)
    public boolean OpensSettings(){ return opensSettings; }

    // How much of a running sync is done, 0 to 100 (0 when the total isn't known yet)
    public int GetPercent(){
        return total <= 0 ? 0 : Math.min(percentOfWhole, (int) Math.round(done * (double) percentOfWhole / total));
    }

    private static final int percentOfWhole = 100;
}
