package Controller.BackEnd.Reviews;

import Model.Library.SteamGame;
import Model.Reviews.FieldStatus;
import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.RatingRules;

// The few numbers every summary of a game needs (rating, playtime), worked out in one place
public class GameNumbers {
    private static final String finalRatingKey = "finalRating";

    // The final rating exactly as typed. Null when there is nothing to show: no review, no field, n/a
    public static String RatingText(GameReview review){
        if(review == null){
            return null;
        }
        FieldValue rating = review.GetFieldValue(finalRatingKey);
        return rating == null || rating.GetStatus() != FieldStatus.FILLED ? null : rating.GetText();
    }

    // The final rating as the number it counts as (jokes clamp to -1 or 11). Null for ?, blank, not applicable or no field
    public static Integer RatingOf(GameReview review){
        String text = RatingText(review);
        return text == null ? null : RatingRules.ToNumber(text);
    }

    // Steam's playtime when it has been synced, otherwise what was typed in by hand. Zero when neither is known
    public static int PlaytimeMinutes(GameReview review, SteamGame steam){
        if(steam != null && steam.GetPlaytimeMinutes() != null){
            return steam.GetPlaytimeMinutes();
        }
        return review == null || review.GetManualPlaytimeMinutes() == null ? 0 : review.GetManualPlaytimeMinutes();
    }
}
