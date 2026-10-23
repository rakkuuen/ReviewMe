package Controller.BackEnd.SeriesManagement;

// The numbers shown on a series. averageRating is null when no game in it has a number rating
public class SeriesStats {
    private int gameCount, completedCount, ratedCount, differentTemplateCount;
    private int playtimeMinutes, unlockedAchievements, totalAchievements;
    private Double averageRating;

    public SeriesStats(int gameCount, int completedCount, int ratedCount, Double averageRating, int playtimeMinutes,
                       int unlockedAchievements, int totalAchievements, int differentTemplateCount){
        this.gameCount = gameCount;
        this.completedCount = completedCount;
        this.ratedCount = ratedCount;
        this.averageRating = averageRating;
        this.playtimeMinutes = playtimeMinutes;
        this.unlockedAchievements = unlockedAchievements;
        this.totalAchievements = totalAchievements;
        this.differentTemplateCount = differentTemplateCount;
    }

    public int GetGameCount(){ return gameCount; }

    // "3 of 5 completed"
    public int GetCompletedCount(){ return completedCount; }

    // How many games the average is over (rated games only: ? and blank are left out)
    public int GetRatedCount(){ return ratedCount; }
    public Double GetAverageRating(){ return averageRating; }

    public int GetPlaytimeMinutes(){ return playtimeMinutes; }
    public double GetHours(){ return playtimeMinutes / 60.0; }

    // Across the games Steam tracks achievements for
    public int GetUnlockedAchievements(){ return unlockedAchievements; }
    public int GetTotalAchievements(){ return totalAchievements; }
    public boolean HasAchievements(){ return totalAchievements > 0; }

    // Games that use a different template from the series (the chip on the series screen)
    public int GetDifferentTemplateCount(){ return differentTemplateCount; }
}
