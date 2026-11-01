package Controller.BackEnd.Overview;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import Controller.BackEnd.Reviews.GameNumbers;
import Model.Library.LibraryGame;
import Model.Library.SteamGame;
import Model.Reviews.GameState;
import Model.Reviews.ReviewState;
import Model.Reviews.ReviewStatus;

// The Overview's stats row and header numbers. A Steam game linked to two reviews is only counted once
public class OverviewStatsCalculator {
    private static final Pattern isoDate = Pattern.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2}$");
    private static final int yearLength = 4;

    public static OverviewStats Calculate(List<LibraryGame> games, Map<Integer, Integer> manualUnlockedByReviewId){
        int reviewed = 0, completed = 0, undated = 0, unlocked = 0, minutes = 0, newAchievements = 0, rated = 0, ratingSum = 0;
        Map<Integer, Integer> completedByYear = new TreeMap<>();
        Set<Integer> countedSteamGames = new HashSet<>();
        Long lastSynced = null;

        for(LibraryGame game : games){
            ReviewStatus status = game.GetStatus();
            reviewed += status.GetReviewState() != ReviewState.NONE ? 1 : 0;
            newAchievements += status.GetNewAchievements();

            if(status.GetGameState() == GameState.COMPLETED){
                completed++;
                Integer year = CompletedYear(game);
                if(year == null){
                    undated++;
                } else {
                    completedByYear.merge(year, 1, Integer::sum);
                }
            }

            Integer rating = GameNumbers.RatingOf(game.GetReview());
            if(rating != null){
                rated++;
                ratingSum += rating;
            }

            SteamGame steam = game.GetSteam();
            if(steam == null || countedSteamGames.add(steam.GetAppId())){
                minutes += GameNumbers.PlaytimeMinutes(game.GetReview(), steam);
                unlocked += steam == null || steam.GetUnlockedAchievements() == null ? 0 : steam.GetUnlockedAchievements();
            }
            if(steam != null){
                lastSynced = Latest(Latest(lastSynced, steam.GetSyncedAt()), steam.GetAchievementsSyncedAt());
            }
            if(game.HasReview()){
                unlocked += manualUnlockedByReviewId.getOrDefault(game.GetReview().GetId(), 0);
            }
        }

        Double average = rated == 0 ? null : (double) ratingSum / rated;
        return new OverviewStats(games.size(), reviewed, completed, completedByYear, undated, unlocked, minutes, average, rated, newAchievements, lastSynced);
    }

    // The year of the game's completed-on date (yyyy-MM-dd). Null when it has none or it isn't usable
    private static Integer CompletedYear(LibraryGame game){
        String date = game.GetCompletedOn();
        if(date == null || !isoDate.matcher(date).matches()){
            return null;
        }
        return Integer.parseInt(date.substring(0, yearLength));
    }

    private static Long Latest(Long a, Long b){
        if(a == null){
            return b;
        }
        return b == null ? a : Long.valueOf(Math.max(a, b));
    }
}
