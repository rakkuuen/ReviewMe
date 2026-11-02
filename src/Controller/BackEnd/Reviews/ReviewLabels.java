package Controller.BackEnd.Reviews;

import Model.Reviews.ReviewStatus;

// The words for how complete a review is. A partial review is a warning only on a completed game, so that's the only place it reads
// "Review incomplete"
public class ReviewLabels {

    public static String Label(ReviewStatus status){
        switch(status.GetReviewState()){
            case COMPLETE: return "Reviewed";
            case PARTIAL: return status.CountsAsIncomplete() ? "Review incomplete" : "Partial review";
            default: return "No review yet";
        }
    }
}
