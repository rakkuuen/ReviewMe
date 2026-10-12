package Controller.BackEnd.Reviews;

import java.util.List;

import Model.Library.SteamGame;
import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.GameState;
import Model.Reviews.ReviewGap;
import Model.Reviews.ReviewState;
import Model.Reviews.ReviewStatus;
import Model.Reviews.Template;
import Model.Reviews.TemplateField;

// Works out both states of a review. Nothing here is stored: it's all worked out from the review as it is now
public class ReviewStatusCalculator {

    // steam is the review's Steam record, or null when it has none or hasn't synced (then nothing is claimed as not started)
    public static ReviewStatus Calculate(GameReview review, Template template, SteamGame steam){
        List<ReviewGap> gaps = ReviewGapFinder.Find(review, template);
        GameState gameState = GameStateOf(review, steam);
        return new ReviewStatus(gameState, ReviewStateOf(gameState, review, template, gaps), gaps);
    }

    public static ReviewStatus Calculate(LoadedReview loaded, SteamGame steam){
        return Calculate(loaded.GetReview(), loaded.GetTemplate(), steam);
    }

    // In progress wins over completed, the same way the tags are read
    private static GameState GameStateOf(GameReview review, SteamGame steam){
        if(review.IsInProgress()){
            return GameState.IN_PROGRESS;
        }
        if(review.IsCompleted()){
            return GameState.COMPLETED;
        }
        return steam != null && steam.IsNotStarted() ? GameState.NOT_STARTED : GameState.NOT_COMPLETED;
    }

    // Any gap means incomplete. A blank review is only "not reviewed" for a game that hasn't been started, where no review is
    // expected yet. For a game that's been played (a completed game with all-blank sections is the extreme case) it's incomplete
    private static ReviewState ReviewStateOf(GameState gameState, GameReview review, Template template, List<ReviewGap> gaps){
        if(gameState == GameState.NOT_STARTED && !AnythingWritten(review, template)){
            return ReviewState.NOT_REVIEWED;
        }
        return gaps.isEmpty() ? ReviewState.REVIEWED : ReviewState.REVIEW_INCOMPLETE;
    }

    private static boolean AnythingWritten(GameReview review, Template template){
        for(TemplateField field : template.GetFields()){
            FieldValue value = review.GetFieldValue(field.GetFieldKey());
            if(value != null && !value.IsNotApplicable() && !value.IsMissing()){
                return true;
            }
        }
        return false;
    }
}
