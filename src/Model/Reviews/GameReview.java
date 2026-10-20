package Model.Reviews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameReview {
    private String gameTitle;

    // id is null until the review is saved
    private Integer id, seriesPosition, steamAppId, achievementsAtCompletion, manualPlaytimeMinutes, topPickSlot;
    private String templateId, seriesId, sourcePath, completedOn, shelvedReason, addedToLibraryOn;
    private boolean inProgress, completed, reviewIncompleteFlagged, shelved;
    private CompletionSource completedOnSource;
    private List<String> genres = new ArrayList<>();
    private List<String> metadata = new ArrayList<>();
    private List<String> platforms = new ArrayList<>();
    private Map<String, FieldValue> fieldValues = new LinkedHashMap<>();

    // Getters / Setters
    public String GetTitle() {
        return gameTitle;
    }

    public void SetTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public Integer GetId(){ return id; }
    public void SetId(Integer id){ this.id = id; }

    public String GetTemplateId(){ return templateId; }
    public void SetTemplateId(String templateId){ this.templateId = templateId; }

    public String GetSeriesId(){ return seriesId; }
    public void SetSeriesId(String seriesId){ this.seriesId = seriesId; }

    public Integer GetSeriesPosition(){ return seriesPosition; }
    public void SetSeriesPosition(Integer seriesPosition){ this.seriesPosition = seriesPosition; }

    public String GetSourcePath(){ return sourcePath; }
    public void SetSourcePath(String sourcePath){ this.sourcePath = sourcePath; }

    public Integer GetSteamAppId(){ return steamAppId; }
    public void SetSteamAppId(Integer steamAppId){ this.steamAppId = steamAppId; }

    public boolean IsInProgress(){ return inProgress; }
    public void SetInProgress(boolean inProgress){ this.inProgress = inProgress; }

    public boolean IsCompleted(){ return completed; }
    public void SetCompleted(boolean completed){ this.completed = completed; }

    // Set by hand when a game is dropped or parked. Overrides every other game state. The reason is optional
    public boolean IsShelved(){ return shelved; }
    public void SetShelved(boolean shelved){ this.shelved = shelved; }

    public String GetShelvedReason(){ return shelvedReason; }
    public void SetShelvedReason(String shelvedReason){ this.shelvedReason = shelvedReason; }

    // A date typed in by hand (like 2025-03-02), because Steam doesn't say when a game was added. Null means not set
    public String GetAddedToLibraryOn(){ return addedToLibraryOn; }
    public void SetAddedToLibraryOn(String addedToLibraryOn){ this.addedToLibraryOn = addedToLibraryOn; }

    // Set by hand when a review is unfinished even though nothing is blank (Dave the Diver)
    public boolean IsReviewIncompleteFlagged(){ return reviewIncompleteFlagged; }
    public void SetReviewIncompleteFlagged(boolean reviewIncompleteFlagged){ this.reviewIncompleteFlagged = reviewIncompleteFlagged; }

    public String GetCompletedOn(){ return completedOn; }
    public void SetCompletedOn(String completedOn){ this.completedOn = completedOn; }

    public CompletionSource GetCompletedOnSource(){ return completedOnSource; }
    public void SetCompletedOnSource(CompletionSource completedOnSource){ this.completedOnSource = completedOnSource; }

    public Integer GetAchievementsAtCompletion(){ return achievementsAtCompletion; }
    public void SetAchievementsAtCompletion(Integer achievementsAtCompletion){ this.achievementsAtCompletion = achievementsAtCompletion; }

    public Integer GetManualPlaytimeMinutes(){ return manualPlaytimeMinutes; }
    public void SetManualPlaytimeMinutes(Integer manualPlaytimeMinutes){ this.manualPlaytimeMinutes = manualPlaytimeMinutes; }

    public Integer GetTopPickSlot(){ return topPickSlot; }
    public void SetTopPickSlot(Integer topPickSlot){ this.topPickSlot = topPickSlot; }

    public List<String> GetGenres(){ return Collections.unmodifiableList(genres); }
    public void SetGenres(List<String> genres){ this.genres = new ArrayList<>(genres); }

    public List<String> GetMetadata(){ return Collections.unmodifiableList(metadata); }
    public void SetMetadata(List<String> metadata){ this.metadata = new ArrayList<>(metadata); }

    public List<String> GetPlatforms(){ return Collections.unmodifiableList(platforms); }
    public void SetPlatforms(List<String> platforms){ this.platforms = new ArrayList<>(platforms); }

    // The review's sections, keyed by the template's field key. Null means the field isn't on this review at all
    // (not on its template, or removed)
    public FieldValue GetFieldValue(String fieldKey){ return fieldValues.get(fieldKey); }
    public void SetFieldValue(String fieldKey, FieldValue value){ fieldValues.put(fieldKey, value); }
    public void RemoveFieldValue(String fieldKey){ fieldValues.remove(fieldKey); }
    public Map<String, FieldValue> GetFieldValues(){ return Collections.unmodifiableMap(fieldValues); }

}
