package Controller.BackEnd.Reviews;

import java.util.Collections;
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

    // steam is the review's Steam record, or null when it has none. A record that hasn't synced yet counts as none
    public static ReviewStatus Calculate(GameReview review, Template template, SteamGame steam){
        SteamGame data = HasData(steam) ? steam : null;
        List<ReviewGap> gaps = ReviewGapFinder.Find(review, template);
        GameState gameState = GameStateOf(review, data);
        String shelvedReason = gameState == GameState.SHELVED ? review.GetShelvedReason() : null;
        return new ReviewStatus(gameState, ReviewStateOf(gameState, review, template, gaps), gaps, NewAchievements(review, gameState, data), shelvedReason);
    }

    public static ReviewStatus Calculate(LoadedReview loaded, SteamGame steam){
        return Calculate(loaded.GetReview(), loaded.GetTemplate(), steam);
    }

    // A Steam game with no review. Nothing of the user's is stored for it yet, so its state comes from Steam alone
    public static ReviewStatus CalculateUnreviewed(SteamGame steam){
        GameState gameState = GameStateOf(false, false, false, HasData(steam) ? steam : null);
        return new ReviewStatus(gameState, ReviewState.NONE, Collections.<ReviewGap>emptyList(), 0, null);
    }

    private static boolean HasData(SteamGame steam){
        return steam != null && steam.GetPlaytimeMinutes() != null;
    }

    // Shelved overrides everything. A game recorded as completed stays completed, whatever Steam says afterwards. Past that
    // Steam decides: 100% is completed, never played is not started, anything else is in progress (achievements below 100%, or
    // played with none to track, until it's marked completed or shelved). With no Steam data the stored flags are all there is,
    // which is the case for games that aren't on Steam
    private static GameState GameStateOf(GameReview review, SteamGame steam){
        return GameStateOf(review.IsShelved(), review.IsCompleted(), review.IsInProgress(), steam);
    }

    private static GameState GameStateOf(boolean shelved, boolean completed, boolean inProgressFlag, SteamGame steam){
        if(shelved){
            return GameState.SHELVED;
        }
        if(completed){
            return GameState.COMPLETED;
        }
        if(steam == null){
            return inProgressFlag ? GameState.IN_PROGRESS : GameState.NOT_STARTED;
        }
        if(steam.IsHundredPercent()){
            return GameState.COMPLETED;
        }
        return steam.IsNotStarted() ? GameState.NOT_STARTED : GameState.IN_PROGRESS;
    }

    // How many achievements a completed game has gained since it was finished. Zero unless it's recorded as completed, has
    // achievements below 100%, and Steam's total is now above what it was at completion. Reaching 100% again clears it
    private static int NewAchievements(GameReview review, GameState gameState, SteamGame steam){
        if(gameState != GameState.COMPLETED || !review.IsCompleted() || steam == null || !steam.HasAchievements() || steam.IsHundredPercent()){
            return 0;
        }
        Integer atCompletion = review.GetAchievementsAtCompletion();
        if(atCompletion == null || steam.GetTotalAchievements() <= atCompletion){
            return 0;
        }
        return steam.GetTotalAchievements() - atCompletion;
    }

    // Nothing written is none. Otherwise only a completed game with no gaps is complete: a review written while the game is
    // still going (or shelved, or not started) can't be finished, however much of it is filled in
    private static ReviewState ReviewStateOf(GameState gameState, GameReview review, Template template, List<ReviewGap> gaps){
        if(!AnythingWritten(review, template)){
            return ReviewState.NONE;
        }
        return gameState == GameState.COMPLETED && gaps.isEmpty() ? ReviewState.COMPLETE : ReviewState.PARTIAL;
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
