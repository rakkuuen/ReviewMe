package Controller.BackEnd.Reviews;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.Subsection;
import Model.Reviews.Template;
import Model.Reviews.TemplateField;

// Which optional sections a review uses. An optional section is ticked when the review has a row for it (even an empty one): it is
// then shown and counts towards the review being complete. Unticked, it is hidden and doesn't count. Required sections are always used.
// This works the ticks out and applies them to a review; nothing is saved here
public class OptionalSections {

    // The template's optional fields, in template order
    public static List<TemplateField> OptionalFields(Template template){
        List<TemplateField> optional = new ArrayList<>();
        for(TemplateField field : template.GetFields()){
            if(!field.IsRequired()){
                optional.add(field);
            }
        }
        return optional;
    }

    public static boolean IsTicked(GameReview review, TemplateField field){
        FieldValue value = review.GetFieldValue(field.GetFieldKey());
        return value != null && !value.IsNotApplicable();
    }

    // The headings of the sections that would lose something somebody wrote if they were unticked: ticked ones that are not in the new
    // set and have text, a comment or a subsection with text
    public static List<String> WouldDelete(GameReview review, Template template, Set<String> tickedKeys){
        List<String> headings = new ArrayList<>();
        for(TemplateField field : OptionalFields(template)){
            if(IsTicked(review, field) && !tickedKeys.contains(field.GetFieldKey()) && HasContent(review.GetFieldValue(field.GetFieldKey()))){
                headings.add(field.GetHeading());
            }
        }
        return headings;
    }

    // Makes the review match the ticks: a newly ticked section gets an empty row, an unticked one loses its row (and what was in it).
    // Sections already ticked, and every required section, are left as they are
    public static void Apply(GameReview review, Template template, Set<String> tickedKeys){
        for(TemplateField field : OptionalFields(template)){
            boolean wanted = tickedKeys.contains(field.GetFieldKey());
            boolean has = IsTicked(review, field);
            if(wanted && !has){
                review.SetFieldValue(field.GetFieldKey(), new FieldValue(""));
            } else if(!wanted && has){
                review.RemoveFieldValue(field.GetFieldKey());
            }
        }
    }

    // True when a row has anything written in it: text, a comment, or text in any subsection
    public static boolean HasContent(FieldValue value){
        if(value == null){
            return false;
        }
        if(value.GetText() != null && !value.GetText().trim().isEmpty() || value.GetComment() != null && !value.GetComment().trim().isEmpty()){
            return true;
        }
        for(Subsection subsection : value.GetSubsections()){
            if(subsection.HasText()){
                return true;
            }
        }
        return false;
    }
}
