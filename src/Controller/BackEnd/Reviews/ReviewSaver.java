package Controller.BackEnd.Reviews;

import java.util.ArrayList;
import java.util.Map;

import Database.Dao.GameReviewDao;
import Database.Dao.ReviewFieldDao;
import Database.Dao.ReviewSubsectionDao;
import Database.Dao.ReviewTagDao;
import Model.Reviews.FieldKind;
import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.TagKind;
import Model.Reviews.TemplateField;

// Saves a whole review back to the database so that what is stored matches what is in memory. A review that fails
// validation is refused before anything is written
public class ReviewSaver {

    public static SaveResult Save(LoadedReview loaded){
        SaveResult result = new SaveResult();
        result.AddErrors(ReviewValidator.Validate(loaded));
        if(!result.Succeeded()){
            return result;
        }

        GameReview review = loaded.GetReview();
        if(review.GetId() == null){
            result.AddError("this review hasn't been saved before, so it has no id to save under");
            return result;
        }

        TidyRatings(loaded);
        if(!GameReviewDao.UpdateReviewDetails(review)){
            result.AddError("couldn't save the review's details");
        }
        if(!SaveFields(review)){
            result.AddError("couldn't save the review's sections");
        }
        if(!SaveSubsections(loaded)){
            result.AddError("couldn't save the groups and characters");
        }
        if(!SaveTags(review)){
            result.AddError("couldn't save the tags");
        }
        return result;
    }

    // Ratings are stored trimmed. A required rating (Final Rating) has no comment
    private static void TidyRatings(LoadedReview loaded){
        for(TemplateField field : loaded.GetTemplate().GetFields()){
            FieldValue value = loaded.GetReview().GetFieldValue(field.GetFieldKey());
            if(field.GetKind() != FieldKind.RATING_OR_UNKNOWN || value == null){
                continue;
            }
            if(value.GetText() != null){
                value.SetText(value.GetText().trim());
            }
            if(field.IsRequired()){
                value.SetComment(null);
            }
        }
    }

    // Writes every field in memory, and removes any stored field that is no longer there
    private static boolean SaveFields(GameReview review){
        int id = review.GetId();
        Map<String, FieldValue> stored = ReviewFieldDao.GetFieldValues(id);
        boolean ok = ReviewFieldDao.SaveFieldValues(id, review.GetFieldValues());
        for(String key : stored.keySet()){
            if(review.GetFieldValue(key) == null){
                ok &= ReviewFieldDao.DeleteFieldValue(id, key);
            }
        }
        return ok;
    }

    // Replacing with an empty list clears a field's groups, so every field that allows them is written
    private static boolean SaveSubsections(LoadedReview loaded){
        GameReview review = loaded.GetReview();
        boolean ok = true;
        for(TemplateField field : loaded.GetTemplate().GetFields()){
            if(!field.AllowsSubsections()){
                continue;
            }
            FieldValue value = review.GetFieldValue(field.GetFieldKey());
            ok &= ReviewSubsectionDao.ReplaceSubsections(review.GetId(), field.GetFieldKey(),
                    value == null ? new ArrayList<>() : value.GetSubsections());
        }
        return ok;
    }

    // A single & on purpose, so every kind is attempted even if one fails
    private static boolean SaveTags(GameReview review){
        int id = review.GetId();
        return ReviewTagDao.ReplaceTags(id, TagKind.GENRE, review.GetGenres())
                & ReviewTagDao.ReplaceTags(id, TagKind.METADATA, review.GetMetadata())
                & ReviewTagDao.ReplaceTags(id, TagKind.PLATFORM, review.GetPlatforms());
    }
}
