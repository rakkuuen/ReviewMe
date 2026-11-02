package Controller.BackEnd.NewReview;

// What the New review dialog asks for. For a Steam game the app id is set and the title is Steam's name; for a game that isn't on Steam
// the app id is null and the title and state are typed. seriesId is null for no series. shelved and completed are the user's own marks
// (completedOn is optional, written like 2025-03-02); inProgress is only for a game that isn't on Steam, where nothing can be worked out
public class NewReviewRequest {
    private final String title;
    private final Integer steamAppId;
    private final String seriesId;
    private final boolean shelved;
    private final String shelvedReason;
    private final boolean completed;
    private final String completedOn;
    private final boolean inProgress;

    public NewReviewRequest(String title, Integer steamAppId, String seriesId, boolean shelved, String shelvedReason, boolean completed, String completedOn, boolean inProgress){
        this.title = title;
        this.steamAppId = steamAppId;
        this.seriesId = seriesId;
        this.shelved = shelved;
        this.shelvedReason = shelvedReason;
        this.completed = completed;
        this.completedOn = completedOn;
        this.inProgress = inProgress;
    }

    public String GetTitle(){ return title; }
    public Integer GetSteamAppId(){ return steamAppId; }
    public String GetSeriesId(){ return seriesId; }
    public boolean IsShelved(){ return shelved; }
    public String GetShelvedReason(){ return shelvedReason; }
    public boolean IsCompleted(){ return completed; }
    public String GetCompletedOn(){ return completedOn; }
    public boolean IsInProgress(){ return inProgress; }
}
