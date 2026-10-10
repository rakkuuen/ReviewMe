package Controller.BackEnd.Reviews;

import Model.Reviews.GameReview;
import Model.Reviews.Template;

// A review with everything attached (fields, groups and characters, tags) and the template that says how to lay it out
public class LoadedReview {
    private GameReview review;
    private Template template;

    public LoadedReview(GameReview review, Template template){
        this.review = review;
        this.template = template;
    }

    public GameReview GetReview(){ return review; }
    public Template GetTemplate(){ return template; }
}
