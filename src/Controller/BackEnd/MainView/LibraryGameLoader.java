package Controller.BackEnd.MainView;

import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.Reviews.LoadedReview;
import Controller.BackEnd.Reviews.ReviewLoader;
import Database.Dao.GameReviewDao;
import Database.Dao.SteamGameDao;
import Database.Dao.SteamGameStatusDao;
import Model.Library.LibraryGame;
import Model.Reviews.GameReview;

// Reads every review and every Steam game from the database and joins them into the game list the main view works from
public class LibraryGameLoader {

    public static List<LibraryGame> Load(){
        List<LoadedReview> reviews = new ArrayList<>();
        for(GameReview review : GameReviewDao.GetAllGameReviews()){
            LoadedReview loaded = ReviewLoader.Load(review.GetTitle());
            if(loaded != null){
                reviews.add(loaded);
            }
        }
        return LibraryGameBuilder.Build(reviews, SteamGameDao.GetAllSteamGames(), SteamGameStatusDao.GetAll());
    }
}
