package Controller.BackEnd.NewReview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Model.Reviews.GameReview;

// What creating a review would do: the review to insert, or why it can't be made
public class NewReviewPlan {
    private final GameReview review;
    private final List<String> errors;

    private NewReviewPlan(GameReview review, List<String> errors){
        this.review = review;
        this.errors = Collections.unmodifiableList(errors);
    }

    public static NewReviewPlan Of(GameReview review){
        return new NewReviewPlan(review, new ArrayList<>());
    }

    public static NewReviewPlan Refused(List<String> errors){
        return new NewReviewPlan(null, errors);
    }

    public boolean Succeeded(){ return errors.isEmpty(); }
    public List<String> GetErrors(){ return errors; }

    // Null when refused
    public GameReview GetReview(){ return review; }
}
