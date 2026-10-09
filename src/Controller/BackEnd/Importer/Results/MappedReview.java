package Controller.BackEnd.Importer.Results;

import Model.Reviews.GameReview;

// A mapped review plus what the md said about its state, for checking tags against content
public class MappedReview {
    // What the state tag in the md claims. CONFLICT is more than one, NONE is no state tag
    public enum TagState { COMPLETED, REVIEW_INCOMPLETE, IN_PROGRESS, CONFLICT, NONE }

    private GameReview review;
    private TagState tagState;
    private int filled, missing, notApplicable;

    public MappedReview(GameReview review, TagState tagState, int filled, int missing, int notApplicable){
        this.review = review;
        this.tagState = tagState;
        this.filled = filled;
        this.missing = missing;
        this.notApplicable = notApplicable;
    }

    public GameReview GetReview(){ return review; }
    public TagState GetTagState(){ return tagState; }
    public int GetFilled(){ return filled; }
    public int GetMissing(){ return missing; }
    public int GetNotApplicable(){ return notApplicable; }
}
