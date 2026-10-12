package Controller.BackEnd.Reviews;

import java.util.ArrayList;
import java.util.List;

import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.ReviewGap;
import Model.Reviews.ReviewGap.Kind;
import Model.Reviews.Subsection;
import Model.Reviews.Template;
import Model.Reviews.TemplateField;

// Finds everything that makes a review incomplete: a missing section, an empty sub-heading, or the manual flag.
// n/a sections are never gaps, ? counts as filled, and a field the review doesn't have is only a gap if it's required
public class ReviewGapFinder {
    private static final String manualFlagText = "marked incomplete by you";
    private static final String separator = " > ";

    public static List<ReviewGap> Find(GameReview review, Template template){
        List<ReviewGap> gaps = new ArrayList<>();

        for(TemplateField field : template.GetFields()){
            FieldValue value = review.GetFieldValue(field.GetFieldKey());
            if(IsMissing(field, value)){
                gaps.add(new ReviewGap(Kind.MISSING_SECTION, field.GetFieldKey(), field.GetHeading()));
            } else if(value != null && field.AllowsSubsections()){
                AddEmptySubsections(field, value, gaps);
            }
        }

        if(review.IsReviewIncompleteFlagged()){
            gaps.add(new ReviewGap(Kind.MANUAL_FLAG, null, manualFlagText));
        }
        return gaps;
    }

    // A required field the review doesn't have, or one it has with nothing in it anywhere. n/a is never missing
    private static boolean IsMissing(TemplateField field, FieldValue value){
        return value == null ? field.IsRequired() : value.IsMissing();
    }

    // A field with some text can still have a sub-heading with none. A group with no text of its own is fine when its
    // characters have text (Main cast), so it's the empty characters that count
    private static void AddEmptySubsections(TemplateField field, FieldValue value, List<ReviewGap> gaps){
        for(Subsection group : value.GetSubsections()){
            String groupPath = field.GetHeading() + separator + group.GetName();
            if(group.GetChildren().isEmpty()){
                if(!HasOwnText(group)){
                    gaps.add(new ReviewGap(Kind.EMPTY_SUBSECTION, field.GetFieldKey(), groupPath));
                }
                continue;
            }
            for(Subsection character : group.GetChildren()){
                if(!HasOwnText(character)){
                    gaps.add(new ReviewGap(Kind.EMPTY_SUBSECTION, field.GetFieldKey(), groupPath + separator + character.GetName()));
                }
            }
        }
    }

    private static boolean HasOwnText(Subsection subsection){
        return subsection.GetText() != null && !subsection.GetText().trim().isEmpty();
    }
}
