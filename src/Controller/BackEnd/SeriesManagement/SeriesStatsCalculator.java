package Controller.BackEnd.SeriesManagement;

import java.util.List;

import Model.Library.SteamGame;
import Model.Reviews.FieldStatus;
import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.GameState;
import Model.Reviews.RatingRules;
import Model.Reviews.Series;

// Works out a series' numbers from its games. Nothing is stored
public class SeriesStatsCalculator {
    private static final String finalRatingKey = "finalRating";

    public static SeriesStats Calculate(Series series, List<SeriesGameFacts> games){
        int completed = 0, rated = 0, ratingSum = 0, minutes = 0, unlocked = 0, total = 0, different = 0;
        for(SeriesGameFacts game : games){
            GameReview review = game.GetReview();
            if(game.GetStatus().GetGameState() == GameState.COMPLETED){
                completed++;
            }
            Integer rating = RatingOf(review);
            if(rating != null){
                rated++;
                ratingSum += rating;
            }
            minutes += PlaytimeOf(review, game.GetSteam());
            SteamGame steam = game.GetSteam();
            if(steam != null && steam.HasAchievements()){
                total += steam.GetTotalAchievements();
                unlocked += steam.GetUnlockedAchievements() == null ? 0 : steam.GetUnlockedAchievements();
            }
            if(!SameTemplate(series, review)){
                different++;
            }
        }
        Double average = rated == 0 ? null : (double) ratingSum / rated;
        return new SeriesStats(games.size(), completed, rated, average, minutes, unlocked, total, different);
    }

    // The final rating as the number it counts as (jokes clamp to -1 or 11). Null for ?, blank, not applicable or no field
    private static Integer RatingOf(GameReview review){
        FieldValue rating = review.GetFieldValue(finalRatingKey);
        if(rating == null || rating.GetStatus() != FieldStatus.FILLED){
            return null;
        }
        return RatingRules.ToNumber(rating.GetText());
    }

    // Steam's playtime when it has been synced, otherwise what was typed in by hand
    private static int PlaytimeOf(GameReview review, SteamGame steam){
        if(steam != null && steam.GetPlaytimeMinutes() != null){
            return steam.GetPlaytimeMinutes();
        }
        return review.GetManualPlaytimeMinutes() == null ? 0 : review.GetManualPlaytimeMinutes();
    }

    private static boolean SameTemplate(Series series, GameReview review){
        return series.GetTemplateId() == null || series.GetTemplateId().equals(review.GetTemplateId());
    }
}
