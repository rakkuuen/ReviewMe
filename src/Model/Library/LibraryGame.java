package Model.Library;

import Model.Reviews.GameReview;
import Model.Reviews.ReviewStatus;

// One game as the main view and Library see it: a review, a Steam game, or both. A game on Steam with no review has no review,
// and a game that isn't on Steam has no Steam record. Either way it has a status
public class LibraryGame {
    private GameReview review;   // Null for a Steam game with no review
    private SteamGame steam;     // Null for a game that isn't linked to Steam
    private ReviewStatus status;

    public LibraryGame(GameReview review, SteamGame steam, ReviewStatus status){
        this.review = review;
        this.steam = steam;
        this.status = status;
    }

    public GameReview GetReview(){ return review; }
    public SteamGame GetSteam(){ return steam; }
    public ReviewStatus GetStatus(){ return status; }

    public boolean HasReview(){ return review != null; }
    public boolean IsOnSteam(){ return steam != null; }

    // The review's title when there is one, otherwise Steam's name
    public String GetTitle(){
        return review != null ? review.GetTitle() : steam.GetName();
    }
}
