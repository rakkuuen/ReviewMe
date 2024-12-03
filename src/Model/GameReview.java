package Model;

public class GameReview {
    private String gameplay, story, setting, achievements, replayability, music,
            conclusion, voiceActing;
    private String alternateTitles;
    int finalRating;

    public GameReview(String gameplay, String story, String setting, String achievements, 
    String replayability, String music, String conclusion, String voiceActing, 
    String alternateTitles, int finalRating){
        this.gameplay = gameplay;
        this.story = story;
        this.setting = setting;
        this.achievements = achievements;
        this.replayability = replayability;
        this.music = music;
        this.conclusion = conclusion;
        this.voiceActing = voiceActing;
        this.alternateTitles = alternateTitles;
        this.finalRating = finalRating;
                    
    }

    // Getters / Setters
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

}
