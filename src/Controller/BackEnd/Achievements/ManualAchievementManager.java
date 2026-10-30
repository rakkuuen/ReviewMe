package Controller.BackEnd.Achievements;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Database.Dao.ManualAchievementDao;
import Database.Dao.SteamGameDao;
import Model.Library.SteamGame;
import Model.Reviews.GameReview;
import Model.Reviews.ManualAchievement;

// The achievements a user keeps by hand for a review. Checks what is typed before anything is stored, and keeps each review's list
// in order. None of this touches Steam data, and a Steam sync never touches these
public class ManualAchievementManager {
    public static final int maxTitleLength = 80;
    public static final int maxDescriptionLength = 500;

    public static ManualAchievementResult Add(int reviewId, String title, String description){
        if(!ManualAchievementDao.ReviewExists(reviewId)){
            return ManualAchievementResult.Refused("There is no such game");
        }
        String cleanTitle = CleanTitle(title);
        String problem = CheckText(cleanTitle, description);
        if(problem == null && TitleTaken(reviewId, cleanTitle, null)){
            problem = "This game already has an achievement called " + cleanTitle;
        }
        if(problem != null){
            return ManualAchievementResult.Refused(problem);
        }
        Integer id = ManualAchievementDao.Insert(reviewId, cleanTitle, CleanDescription(description));
        return id == null ? ManualAchievementResult.Refused("Couldn't save the achievement") : ManualAchievementResult.Added(id);
    }

    public static ManualAchievementResult Edit(int id, String title, String description){
        ManualAchievement existing = ManualAchievementDao.Get(id);
        if(existing == null){
            return ManualAchievementResult.Refused(NoSuchAchievement());
        }
        String cleanTitle = CleanTitle(title);
        String problem = CheckText(cleanTitle, description);
        if(problem == null && TitleTaken(existing.GetReviewId(), cleanTitle, id)){
            problem = "This game already has an achievement called " + cleanTitle;
        }
        if(problem != null){
            return ManualAchievementResult.Refused(problem);
        }
        return ManualAchievementDao.UpdateText(id, cleanTitle, CleanDescription(description)) ? ManualAchievementResult.Done() : ManualAchievementResult.Refused("Couldn't save the achievement");
    }

    // unlockedOn is a date like 2025-03-02, or null for none. It is ignored (and cleared) when locking
    public static ManualAchievementResult SetUnlocked(int id, boolean unlocked, String unlockedOn){
        if(ManualAchievementDao.Get(id) == null){
            return ManualAchievementResult.Refused(NoSuchAchievement());
        }
        String date = unlockedOn == null || unlockedOn.trim().isEmpty() ? null : unlockedOn.trim();
        if(unlocked && date != null && !IsRealDate(date)){
            return ManualAchievementResult.Refused("The date needs to be like 2025-03-02");
        }
        return ManualAchievementDao.SetUnlocked(id, unlocked, date) ? ManualAchievementResult.Done() : ManualAchievementResult.Refused("Couldn't save the achievement");
    }

    public static ManualAchievementResult Remove(int id){
        return ManualAchievementDao.Delete(id) ? ManualAchievementResult.Done() : ManualAchievementResult.Refused(NoSuchAchievement());
    }

    // idsInOrder has to be every achievement of the review exactly once
    public static ManualAchievementResult Reorder(int reviewId, List<Integer> idsInOrder){
        Set<Integer> current = new HashSet<>();
        for(ManualAchievement achievement : ManualAchievementDao.GetForReview(reviewId)){
            current.add(achievement.GetId());
        }
        Set<Integer> given = new HashSet<>(idsInOrder);
        if(given.size() != idsInOrder.size() || !given.equals(current)){
            return ManualAchievementResult.Refused("The new order has to list every achievement exactly once");
        }
        return ManualAchievementDao.Reorder(reviewId, idsInOrder) ? ManualAchievementResult.Done() : ManualAchievementResult.Refused("Couldn't save the new order");
    }

    // Steam's counts (from the review's linked game, if it has one and it's been synced) next to the manual ones
    public static AchievementTotals TotalsFor(GameReview review){
        SteamGame steam = review.GetSteamAppId() == null ? null : SteamGameDao.GetSteamGame(review.GetSteamAppId());
        return AchievementTotals.Of(steam, ManualAchievementDao.GetForReview(review.GetId()));
    }

    private static String NoSuchAchievement(){ return "There is no such achievement"; }

    private static String CleanTitle(String title){
        return title == null ? "" : title.trim().replaceAll("\\s+", " ");
    }

    private static String CleanDescription(String description){
        return description == null || description.trim().isEmpty() ? null : description.trim();
    }

    // Null if the text is fine, otherwise what is wrong
    private static String CheckText(String cleanTitle, String description){
        if(cleanTitle.isEmpty()){
            return "Give the achievement a title";
        }
        if(cleanTitle.length() > maxTitleLength){
            return "The title is over " + maxTitleLength + " characters";
        }
        if(description != null && description.trim().length() > maxDescriptionLength){
            return "The description is over " + maxDescriptionLength + " characters";
        }
        return null;
    }

    // ignoreId is the achievement being edited, which may keep its own title
    private static boolean TitleTaken(int reviewId, String cleanTitle, Integer ignoreId){
        for(ManualAchievement other : ManualAchievementDao.GetForReview(reviewId)){
            if(!other.GetId().equals(ignoreId) && other.GetTitle().equalsIgnoreCase(cleanTitle)){
                return true;
            }
        }
        return false;
    }

    private static boolean IsRealDate(String text){
        try {
            LocalDate.parse(text);
            return text.length() == 10;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
