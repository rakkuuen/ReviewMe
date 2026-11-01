package Controller.BackEnd.Overview;

import java.util.Collections;
import java.util.Map;

// The numbers in the Overview's stats row and header
public class OverviewStats {
    private final int ownedCount;
    private final int reviewedCount;
    private final int completedCount;
    private final Map<Integer, Integer> completedByYear;
    private final int undatedCompletedCount;
    private final int achievementsUnlocked;
    private final int playtimeMinutes;
    private final Double averageRating;
    private final int ratedCount;
    private final int newAchievementsTotal;
    private final Long lastSyncedAt;

    public OverviewStats(int ownedCount, int reviewedCount, int completedCount, Map<Integer, Integer> completedByYear, int undatedCompletedCount,
                         int achievementsUnlocked, int playtimeMinutes, Double averageRating, int ratedCount, int newAchievementsTotal, Long lastSyncedAt){
        this.ownedCount = ownedCount;
        this.reviewedCount = reviewedCount;
        this.completedCount = completedCount;
        this.completedByYear = Collections.unmodifiableMap(completedByYear);
        this.undatedCompletedCount = undatedCompletedCount;
        this.achievementsUnlocked = achievementsUnlocked;
        this.playtimeMinutes = playtimeMinutes;
        this.averageRating = averageRating;
        this.ratedCount = ratedCount;
        this.newAchievementsTotal = newAchievementsTotal;
        this.lastSyncedAt = lastSyncedAt;
    }

    // Every game: Steam games and reviews with no Steam game, counted once
    public int GetOwnedCount(){ return ownedCount; }

    // Games with something written (a complete or partial review)
    public int GetReviewedCount(){ return reviewedCount; }

    public int GetCompletedCount(){ return completedCount; }

    // Year to how many were completed that year, oldest year first. Completions with no date are counted apart
    public Map<Integer, Integer> GetCompletedByYear(){ return completedByYear; }
    public int GetUndatedCompletedCount(){ return undatedCompletedCount; }

    // Steam achievements plus manual ones
    public int GetAchievementsUnlocked(){ return achievementsUnlocked; }

    public int GetPlaytimeMinutes(){ return playtimeMinutes; }
    public double GetHours(){ return playtimeMinutes / 60.0; }

    // Mean of the final ratings that are numbers (jokes count as -1 or 11). Null when none are
    public Double GetAverageRating(){ return averageRating; }
    public int GetRatedCount(){ return ratedCount; }

    // Achievements added to completed games since they were finished, all games together
    public int GetNewAchievementsTotal(){ return newAchievementsTotal; }

    // Epoch seconds of the latest Steam sync, or null when nothing has been synced
    public Long GetLastSyncedAt(){ return lastSyncedAt; }
}
