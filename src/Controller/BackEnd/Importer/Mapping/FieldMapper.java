package Controller.BackEnd.Importer.Mapping;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Model.Reviews.FieldKind;
import Model.Reviews.FieldStatus;
import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.Template;
import Model.Reviews.TemplateField;
import Controller.BackEnd.Importer.Parsing.ParsedMarkdown;
import Controller.BackEnd.Importer.Parsing.Section;
import Controller.BackEnd.Importer.Results.ImportIssue;
import Controller.BackEnd.Importer.Results.IssueSeverity;

// Fills a review's fields by matching the template's headings against the sections in the file
public class FieldMapper {

    // Walks the template's fields, not the file's headings, and matches each by exact heading
    public static SectionCounts MapFields(Template template, ParsedMarkdown parsed, GameReview review, List<ImportIssue> issues){
        String file = parsed.GetRelativePath();
        SectionCounts counts = new SectionCounts();

        for(TemplateField field : template.GetFields()){
            Section section = parsed.GetSections().get(field.GetHeading());
            if(section == null){
                // No heading means the field isn't on this review, unless it's required
                if(field.IsRequired()){
                    counts.AddMissing();
                    issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "'" + field.GetHeading() + "' has no heading (it is required, so it counts as missing)"));
                }
                continue;
            }

            List<String> lines = section.GetLines();
            FieldValue value = field.GetKind() == FieldKind.TEXT ? MapText(lines) : RatingMapper.Map(field, lines, file, issues);
            review.SetFieldValue(field.GetFieldKey(), value);
            counts.Add(value);
            FlagNestedHeadings(field, section, file, issues);
        }
        return counts;
    }

    // Headings nested inside a field (## Main cast under Voice Acting) have nowhere to go yet
    private static void FlagNestedHeadings(TemplateField field, Section section, String file, List<ImportIssue> issues){
        if(section.GetChildren().isEmpty()){
            return;
        }
        issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "'" + field.GetHeading() + "' has " + section.GetChildren().size()
                + " sub-heading(s) that would not be imported (" + section.CountLinesBelow() + " line(s) of text)"));
    }

    // Anything the template has no place for would be lost on import
    public static void FlagUnmappedHeadings(Template template, ParsedMarkdown parsed, List<ImportIssue> issues){
        Set<String> templateHeadings = new HashSet<>();
        for(TemplateField field : template.GetFields()){
            templateHeadings.add(field.GetHeading());
        }

        for(Map.Entry<String, Section> entry : parsed.GetSections().entrySet()){
            int lineCount = entry.getValue().CountAllLines();
            if(!templateHeadings.contains(entry.getKey()) && lineCount > 0){
                issues.add(new ImportIssue(parsed.GetRelativePath(), IssueSeverity.PROBLEM, "heading '" + entry.getKey() + "' isn't on the "
                        + template.GetId() + " template (" + lineCount + " line(s) of text would not be imported)"));
            }
        }
    }

    // n/a means not relevant; an empty section stays empty (missing)
    private static FieldValue MapText(List<String> lines){
        if(lines.size() == 1 && lines.get(0).equalsIgnoreCase("n/a")){
            return new FieldValue(null, null, FieldStatus.NA);
        }
        return new FieldValue(String.join("\n", lines));
    }
}
