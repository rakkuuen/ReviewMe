package Controller.BackEnd.AchievementView;

import Database.Dao.AchievementDao;
import Database.Dao.GameReviewDao;
import Database.Dao.ManualAchievementDao;
import Model.Reviews.GameReview;

// Reads the database for one game's achievements page. Null when there is no review with that title
public class AchievementViewLoader {

    public static AchievementViewData Load(String title){
        GameReview review = GameReviewDao.GetGameReview(title);
        if(review == null){
            return null;
        }
        return AchievementViewBuilder.Build(review.GetTitle(), review.GetSteamAppId() == null ? null : AchievementDao.GetAchievements(review.GetSteamAppId()),
                review.GetId() == null ? null : ManualAchievementDao.GetForReview(review.GetId()));
    }
}
