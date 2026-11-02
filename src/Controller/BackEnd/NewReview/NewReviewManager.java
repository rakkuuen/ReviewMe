package Controller.BackEnd.NewReview;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Database.Dao.GameReviewDao;
import Database.Dao.SeriesDao;
import Database.Dao.SteamGameStatusDao;
import Database.Dao.TemplateDao;
import Model.Reviews.GameReview;
import Model.Reviews.Template;

// Creates a review for a Steam game or a game that isn't on Steam. A Steam game's own marks (shelved, completed by hand) were said
// before it had a review; they come over in the request, and once the review holds them the old note is removed
public class NewReviewManager {
    private static final String couldNotSave = "Couldn't save the new review";

    // The new review's title when it was made, otherwise null and the reasons in errors
    public static NewReviewResult Create(NewReviewRequest request){
        Set<String> templateIds = new HashSet<>();
        for(Template template : TemplateDao.GetAllTemplates()){
            templateIds.add(template.GetId());
        }
        NewReviewPlan plan = NewReviewPlanner.Plan(request, GameReviewDao.GetAllGameReviews(), SeriesDao.GetAllSeries(), templateIds);
        if(!plan.Succeeded()){
            return NewReviewResult.Refused(plan.GetErrors());
        }
        GameReview review = plan.GetReview();
        Integer id = GameReviewDao.InsertImportedReview(review);
        if(id == null){
            return NewReviewResult.Refused(List.of(couldNotSave));
        }
        if(request.GetSteamAppId() != null){
            SteamGameStatusDao.Delete(request.GetSteamAppId());   // The review holds those marks now, and a game with a review ignores the note anyway
        }
        return NewReviewResult.Created(review.GetTitle());
    }
}
