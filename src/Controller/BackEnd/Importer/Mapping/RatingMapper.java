package Controller.BackEnd.Importer.Mapping;

import java.util.List;

import Model.Reviews.FieldStatus;
import Model.Reviews.FieldValue;
import Model.Reviews.RatingRules;
import Model.Reviews.RatingRules.RatingState;
import Model.Reviews.TemplateField;
import Controller.BackEnd.Importer.Results.ImportIssue;
import Controller.BackEnd.Importer.Results.IssueSeverity;

// Turns a rating section into a FieldValue. First line is the rating, anything under it is the comment.
// Required ratings (Final Rating) are strict and have no comment. Optional ones (Replay-ability) turn odd
// wording into ? and keep the wording as the comment
public class RatingMapper {

    public static FieldValue Map(TemplateField field, List<String> lines, String file, List<ImportIssue> issues){
        String heading = field.GetHeading();
        boolean strict = field.IsRequired();
        String token = lines.isEmpty() ? "" : lines.get(0).trim();
        String comment = lines.size() > 1 ? String.join("\n", lines.subList(1, lines.size())) : null;

        if(strict && comment != null){
            issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "text under '" + heading + "' would not be imported (it has no comment)"));
            comment = null;
        }

        if(token.isEmpty() || token.equalsIgnoreCase("temp")){
            return new FieldValue("", comment, FieldStatus.FILLED);
        }
        if(token.equalsIgnoreCase("n/a")){
            return MapNotApplicable(strict, heading, comment, file, issues);
        }

        RatingState state = RatingRules.Classify(token);
        if(state == RatingState.NUMBER || state == RatingState.UNKNOWN){
            return new FieldValue(token, comment, FieldStatus.FILLED);
        }
        return MapInvalid(strict, heading, token, comment, file, issues);
    }

    // Final Rating always needs a value, Replay-ability can be n/a
    private static FieldValue MapNotApplicable(boolean strict, String heading, String comment, String file, List<ImportIssue> issues){
        if(strict){
            issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "'" + heading + "' can't be n/a (it always needs a value)"));
            return new FieldValue("", comment, FieldStatus.FILLED);
        }
        return new FieldValue(null, null, FieldStatus.NA);
    }

    // Not a number and not ?. Strict ratings are a problem, lenient ones become ? with the wording kept
    private static FieldValue MapInvalid(boolean strict, String heading, String token, String comment, String file, List<ImportIssue> issues){
        if(strict){
            issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "invalid '" + heading + "' value '" + token + "' (use a whole number from -1 to 11, or ?)"));
            return new FieldValue("", comment, FieldStatus.FILLED);
        }
        issues.add(new ImportIssue(file, IssueSeverity.NOTE, "'" + heading + "' was '" + token + "', imported as ? with that wording kept as the comment"));
        return new FieldValue("?", comment == null ? token : token + "\n" + comment, FieldStatus.FILLED);
    }
}
