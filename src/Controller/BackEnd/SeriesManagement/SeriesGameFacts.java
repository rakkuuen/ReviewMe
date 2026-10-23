package Controller.BackEnd.SeriesManagement;

import Model.Library.SteamGame;
import Model.Reviews.GameReview;
import Model.Reviews.ReviewStatus;

// One game in a series with what the stats need to know about it. steam is null when the game has no Steam record
public class SeriesGameFacts {
    private GameReview review;
    private ReviewStatus status;
    private SteamGame steam;

    public SeriesGameFacts(GameReview review, ReviewStatus status, SteamGame steam){
        this.review = review;
        this.status = status;
        this.steam = steam;
    }

    public GameReview GetReview(){ return review; }
    public ReviewStatus GetStatus(){ return status; }
    public SteamGame GetSteam(){ return steam; }
}
