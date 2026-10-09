package Model.Reviews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameReview {
    private String gameTitle, gameplay, story, setting, achievements, replayability, music,
            conclusion, voiceActing;
    private String alternateTitles;
    private int finalRating;

    // Template-driven data, replaces the fixed sections above once the screens use templates
    private Integer id, seriesPosition, steamAppId, achievementsAtCompletion, dismissedAtTotal,
            manualPlaytimeMinutes, topPickSlot;
    private String templateId, seriesId, sourcePath, completedOn;
    private boolean inProgress, completed;
    private CompletionSource completedOnSource;
    private List<String> genres = new ArrayList<>();
    private List<String> metadata = new ArrayList<>();
    private List<String> platforms = new ArrayList<>();
    private Map<String, FieldValue> fieldValues = new LinkedHashMap<>();

    public GameReview(){
        // Set defualt values?
                    
    }

    // Getters / Setters
    public String GetTitle() {
        return gameTitle;
    }

    public void SetTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public String GetGameplay() {
        return gameplay;
    }

    public void SetGameplay(String gameplay) {
        this.gameplay = gameplay;
    }

    public String GetStory() {
        return story;
    }

    public void SetStory(String story) {
        this.story = story;
    }

    public String GetSetting() {
        return setting;
    }

    public void SetSetting(String setting) {
        this.setting = setting;
    }

    public String GetAchievements() {
        return achievements;
    }

    public void SetAchievements(String achievements) {
        this.achievements = achievements;
    }

    public String GetReplayability() {
        return replayability;
    }

    public void SetReplayability(String replayability) {
        this.replayability = replayability;
    }

    public String GetMusic() {
        return music;
    }

    public void SetMusic(String music) {
        this.music = music;
    }

    public String GetConclusion() {
        return conclusion;
    }

    public void SetConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public String GetVoiceActing() {
        return voiceActing;
    }

    public void SetVoiceActing(String voiceActing) {
        this.voiceActing = voiceActing;
    }

    public String GetAlternateTitles() {
        return alternateTitles;
    }

    public void SetAlternateTitles(String alternateTitles) {
        this.alternateTitles = alternateTitles;
    }

    public int GetFinalRating() {
        return finalRating;
    }

    public void SetFinalRating(int finalRating) {
        this.finalRating = finalRating;
    }

    // id is null until the review is saved
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

    public String GetCompletedOn(){ return completedOn; }
    public void SetCompletedOn(String completedOn){ this.completedOn = completedOn; }

    public CompletionSource GetCompletedOnSource(){ return completedOnSource; }
    public void SetCompletedOnSource(CompletionSource completedOnSource){ this.completedOnSource = completedOnSource; }

    public Integer GetAchievementsAtCompletion(){ return achievementsAtCompletion; }
    public void SetAchievementsAtCompletion(Integer achievementsAtCompletion){ this.achievementsAtCompletion = achievementsAtCompletion; }

    public Integer GetDismissedAtTotal(){ return dismissedAtTotal; }
    public void SetDismissedAtTotal(Integer dismissedAtTotal){ this.dismissedAtTotal = dismissedAtTotal; }

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

    // Null means the field isn't on this review at all (not on its template, or removed)
    public FieldValue GetFieldValue(String fieldKey){ return fieldValues.get(fieldKey); }
    public void SetFieldValue(String fieldKey, FieldValue value){ fieldValues.put(fieldKey, value); }
    public void RemoveFieldValue(String fieldKey){ fieldValues.remove(fieldKey); }
    public Map<String, FieldValue> GetFieldValues(){ return Collections.unmodifiableMap(fieldValues); }

}
