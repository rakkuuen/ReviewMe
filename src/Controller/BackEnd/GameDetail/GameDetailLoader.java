package Controller.BackEnd.GameDetail;

import Controller.BackEnd.Reviews.LoadedReview;
import Controller.BackEnd.Reviews.ReviewLoader;
import Controller.BackEnd.Reviews.ReviewStatusCalculator;
import Database.Dao.GameReviewDao;
import Database.Dao.SeriesDao;
import Database.Dao.SteamGameDao;
import Model.Library.SteamGame;
import Model.Reviews.GameReview;
import Model.Reviews.ReviewStatus;
import Model.Reviews.Series;

// Reads the database for one game's page. Null when there is no review with that title
public class GameDetailLoader {

    public static GameDetailData Load(String title){
        LoadedReview loaded = ReviewLoader.Load(title);
        if(loaded == null){
            return null;
        }
        GameReview review = loaded.GetReview();
        SteamGame steam = review.GetSteamAppId() == null ? null : SteamGameDao.GetSteamGame(review.GetSteamAppId());
        ReviewStatus status = ReviewStatusCalculator.Calculate(loaded, steam);
        Series series = review.GetSeriesId() == null ? null : SeriesDao.GetSeries(review.GetSeriesId());
        int seriesGames = 0;
        if(series != null){
            for(GameReview other : GameReviewDao.GetAllGameReviews()){
                seriesGames += series.GetId().equals(other.GetSeriesId()) ? 1 : 0;
            }
        }
        return GameDetailBuilder.Build(review, loaded.GetTemplate(), status, steam, series, seriesGames);
    }
}
