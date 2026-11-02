package Controller.BackEnd.NewReview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// How creating a review went: its title if it was made, otherwise what was wrong
public class NewReviewResult {
    private final String title;
    private final List<String> errors;

    private NewReviewResult(String title, List<String> errors){
        this.title = title;
        this.errors = Collections.unmodifiableList(errors);
    }

    public static NewReviewResult Created(String title){ return new NewReviewResult(title, new ArrayList<>()); }
    public static NewReviewResult Refused(List<String> errors){ return new NewReviewResult(null, errors); }

    public boolean Succeeded(){ return errors.isEmpty(); }
    public List<String> GetErrors(){ return errors; }
    public String GetTitle(){ return title; }
}
