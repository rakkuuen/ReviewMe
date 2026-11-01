package Model.Library;

import Model.Reviews.CompletionSource;

// What the user has said about a Steam game that has no review: shelved (with a reason), or completed by hand (for a game with no
// achievements, where completion can't be worked out). When a review is written for the game these move onto the review.
// Immutable: every change makes a new one
public class SteamGameStatus {
    private final int appId;
    private final boolean shelved;
    private final String shelvedReason;
    private final boolean completed;
    private final String completedOn;                    // yyyy-MM-dd, or null when no date was given
    private final CompletionSource completedOnSource;    // MANUAL when there is a date, otherwise null

    public SteamGameStatus(int appId, boolean shelved, String shelvedReason, boolean completed, String completedOn, CompletionSource completedOnSource){
        this.appId = appId;
        this.shelved = shelved;
        this.shelvedReason = shelvedReason;
        this.completed = completed;
        this.completedOn = completedOn;
        this.completedOnSource = completedOnSource;
    }

    public int GetAppId(){ return appId; }
    public boolean IsShelved(){ return shelved; }
    public String GetShelvedReason(){ return shelvedReason; }
    public boolean IsCompleted(){ return completed; }
    public String GetCompletedOn(){ return completedOn; }
    public CompletionSource GetCompletedOnSource(){ return completedOnSource; }

    // Nothing said, so there is nothing to keep
    public boolean IsEmpty(){
        return !shelved && !completed;
    }
}
