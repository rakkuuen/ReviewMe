package Controller.BackEnd.SeriesManagement;

import java.util.List;

import Controller.BackEnd.Reviews.GameNumbers;
import Model.Library.SteamGame;
import Model.Reviews.GameReview;
import Model.Reviews.GameState;
import Model.Reviews.Series;

// Works out a series' numbers from its games. Nothing is stored
public class SeriesStatsCalculator {

    public static SeriesStats Calculate(Series series, List<SeriesGameFacts> games){
        int completed = 0, rated = 0, ratingSum = 0, minutes = 0, unlocked = 0, total = 0, different = 0;
        for(SeriesGameFacts game : games){
            GameReview review = game.GetReview();
            if(game.GetStatus().GetGameState() == GameState.COMPLETED){
                completed++;
            }
            Integer rating = GameNumbers.RatingOf(review);
            if(rating != null){
                rated++;
                ratingSum += rating;
            }
            minutes += GameNumbers.PlaytimeMinutes(review, game.GetSteam());
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

    private static boolean SameTemplate(Series series, GameReview review){
        return series.GetTemplateId() == null || series.GetTemplateId().equals(review.GetTemplateId());
    }
}
