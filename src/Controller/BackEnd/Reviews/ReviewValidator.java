package Controller.BackEnd.Reviews;

import java.util.ArrayList;
import java.util.List;

import Model.Reviews.FieldKind;
import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.RatingRules;
import Model.Reviews.TemplateField;

// Checks a review before it is saved. Nothing is written if this finds anything
public class ReviewValidator {

    // Every rating has to be a whole number from -1 to 11, or ?. Blank is fine (it is just missing)
    public static List<String> Validate(LoadedReview loaded){
        List<String> errors = new ArrayList<>();
        GameReview review = loaded.GetReview();

        for(TemplateField field : loaded.GetTemplate().GetFields()){
            FieldValue value = review.GetFieldValue(field.GetFieldKey());
            if(field.GetKind() != FieldKind.RATING_OR_UNKNOWN || value == null || value.IsNotApplicable()){
                continue;
            }
            if(!RatingRules.IsAcceptable(value.GetText())){
                errors.add(field.GetHeading() + " must be a whole number from " + RatingRules.min + " to " + RatingRules.max
                        + ", or " + RatingRules.unknown + " (not \"" + value.GetText().trim() + "\")");
            }
        }
        return errors;
    }
}
