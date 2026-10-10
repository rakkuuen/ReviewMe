package Controller.BackEnd.Reviews;

import java.util.List;
import java.util.Map;

import Database.Dao.GameReviewDao;
import Database.Dao.ReviewFieldDao;
import Database.Dao.ReviewSubsectionDao;
import Database.Dao.ReviewTagDao;
import Database.Dao.TemplateDao;
import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.Subsection;
import Model.Reviews.TagKind;
import Model.Reviews.Template;

// Gathers one whole review from the database: the row, its field values with their groups and characters, its tags, its template
public class ReviewLoader {
    private static final String standardTemplateId = "Standard";

    // Null if there's no review with that title
    public static LoadedReview Load(String title){
        GameReview review = GameReviewDao.GetGameReview(title);
        if(review == null){
            return null;
        }

        Template template = TemplateDao.GetTemplate(review.GetTemplateId() == null ? standardTemplateId : review.GetTemplateId());
        if(template == null){
            System.err.println("Template " + review.GetTemplateId() + " not found for " + title);
            return null;
        }

        AttachFields(review);
        AttachTags(review);
        return new LoadedReview(review, template);
    }

    private static void AttachFields(GameReview review){
        int id = review.GetId();
        Map<String, FieldValue> fields = ReviewFieldDao.GetFieldValues(id);

        // Groups and characters live in their own table, so hang them on their field
        for(Map.Entry<String, List<Subsection>> entry : ReviewSubsectionDao.GetAllSubsections(id).entrySet()){
            fields.computeIfAbsent(entry.getKey(), key -> new FieldValue("")).SetSubsections(entry.getValue());
        }

        for(Map.Entry<String, FieldValue> field : fields.entrySet()){
            review.SetFieldValue(field.getKey(), field.getValue());
        }
    }

    private static void AttachTags(GameReview review){
        int id = review.GetId();
        review.SetGenres(ReviewTagDao.GetTags(id, TagKind.GENRE));
        review.SetMetadata(ReviewTagDao.GetTags(id, TagKind.METADATA));
        review.SetPlatforms(ReviewTagDao.GetTags(id, TagKind.PLATFORM));
    }
}
