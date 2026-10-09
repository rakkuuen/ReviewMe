package Controller.BackEnd.Importer;

import java.util.List;

import Controller.BackEnd.Importer.MappedReview.TagState;

// What the tags line says: platforms, series, and the state of the game and review
public class TagInfo {
    private List<String> platforms;
    private String seriesId;
    private TagState tagState;
    private boolean inProgress, completed;

    // seriesId is null when the game has no series tag
    public TagInfo(List<String> platforms, String seriesId, TagState tagState, boolean inProgress, boolean completed){
        this.platforms = platforms;
        this.seriesId = seriesId;
        this.tagState = tagState;
        this.inProgress = inProgress;
        this.completed = completed;
    }

    public List<String> GetPlatforms(){ return platforms; }
    public String GetSeriesId(){ return seriesId; }
    public TagState GetTagState(){ return tagState; }
    public boolean IsInProgress(){ return inProgress; }
    public boolean IsCompleted(){ return completed; }
}
