package Model;

public class GameReview {
    private String gameTitle, gameplay, story, setting, achievements, replayability, music,
            conclusion, voiceActing;
    private String alternateTitles;
    private int finalRating;

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

}
