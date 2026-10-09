package Controller.BackEnd.Importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Model.Reviews.FieldKind;
import Model.Reviews.FieldStatus;
import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.RatingRules;
import Model.Reviews.RatingRules.RatingState;
import Model.Reviews.Template;
import Model.Reviews.TemplateField;

import Controller.BackEnd.Importer.MappedReview.TagState;

// Gives a parsed md file its meaning: which template, which state, and what each section holds
public class ReviewMapper {
    private static final String standardTemplateId = "Standard";
    private static final Set<String> platformTags = new HashSet<>(Arrays.asList("pc", "switch", "ds", "wii", "gameboy", "gamecube", "ps4", "ps5", "xbox", "mobile", "mac", "psp"));

    public static boolean IsReview(ParsedMarkdown parsed){
        for(String tag : parsed.GetTags()){
            if(tag.equalsIgnoreCase("#Review")){
                return true;
            }
        }
        return false;
    }

    // Problems are added to issues. Call this only for files where IsReview is true
    public static MappedReview Map(ParsedMarkdown parsed, Map<String, Template> templates, List<ImportIssue> issues){
        String file = parsed.GetRelativePath();
        GameReview review = new GameReview();
        review.SetTitle(parsed.GetTitle());
        review.SetSourcePath(file);

        // Tags line: #Review #Game #PC [#Series] #State
        List<String> platforms = new ArrayList<>();
        List<String> seriesTags = new ArrayList<>();
        int inProgressTags = 0, incompleteTags = 0, completedTags = 0;
        for(String token : parsed.GetTags()){
            String name = StripHash(token);
            String lower = name.toLowerCase();
            if(lower.equals("review") || lower.equals("game")){
                continue;
            }
            if(lower.equals("gameinprogress")){
                inProgressTags++;
            } else if(lower.equals("reviewincomplete")){
                incompleteTags++;
            } else if(lower.equals("completed")){
                completedTags++;
            } else if(lower.equals("incomplete")){
                issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "old #Incomplete tag, remove it"));
            } else if(platformTags.contains(lower)){
                platforms.add(name);
            } else {
                seriesTags.add(name);
            }
        }

        int stateTags = (inProgressTags > 0 ? 1 : 0) + (incompleteTags > 0 ? 1 : 0) + (completedTags > 0 ? 1 : 0);
        TagState tagState;
        if(stateTags == 0){
            tagState = TagState.NONE;
            issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "no state tag (#Completed, #REVIEWINCOMPLETE or #GameInProgress)"));
        } else if(stateTags > 1){
            tagState = TagState.CONFLICT;
            issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "more than one state tag"));
        } else if(inProgressTags > 0){
            tagState = TagState.IN_PROGRESS;
        } else if(incompleteTags > 0){
            tagState = TagState.REVIEW_INCOMPLETE;
        } else {
            tagState = TagState.COMPLETED;
        }
        review.SetInProgress(inProgressTags > 0);
        review.SetCompleted(inProgressTags == 0 && (completedTags > 0 || incompleteTags > 0));

        // Series decides the template: one with the same id as the series, otherwise Standard
        if(seriesTags.size() > 1){
            issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "more than one series tag: " + String.join(", ", seriesTags)));
        }
        String seriesId = seriesTags.isEmpty() ? null : seriesTags.get(0);
        review.SetSeriesId(seriesId);
        String templateId = seriesId != null && templates.containsKey(seriesId) ? seriesId : standardTemplateId;
        Template template = templates.get(templateId);
        if(template == null){
            throw new IllegalStateException("Template not found: " + templateId);
        }
        review.SetTemplateId(templateId);

        review.SetPlatforms(platforms);
        review.SetGenres(StripHashes(parsed.GetGenres()));
        review.SetMetadata(StripHashes(parsed.GetMetadata()));

        // Sections -> template fields
        Set<String> usedHeadings = new HashSet<>();
        int filled = 0, missing = 0, notApplicable = 0;
        for(TemplateField field : template.GetFields()){
            List<String> lines = parsed.GetSections().get(field.GetHeading());
            if(lines == null){
                if(field.IsRequired()){
                    missing++;
                    issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "'" + field.GetHeading() + "' has no heading (it is required, so it counts as missing)"));
                }
                continue;
            }

            usedHeadings.add(field.GetHeading());
            FieldValue value = field.GetKind() == FieldKind.TEXT ? MapText(lines) : MapRating(field, lines, file, issues);
            review.SetFieldValue(field.GetFieldKey(), value);
            if(value.IsNotApplicable()){
                notApplicable++;
            } else if(value.IsMissing()){
                missing++;
            } else {
                filled++;
            }
        }

        // Anything the template has no place for would be lost on import
        for(Map.Entry<String, List<String>> section : parsed.GetSections().entrySet()){
            if(!usedHeadings.contains(section.getKey()) && !section.getValue().isEmpty()){
                issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "heading '" + section.getKey() + "' isn't on the " + templateId
                        + " template (" + section.getValue().size() + " line(s) of text would not be imported)"));
            }
        }

        return new MappedReview(review, tagState, filled, missing, notApplicable);
    }

    // The rules file of a series: tagged with the series and #ReviewInfo instead of #Review. Null if it isn't one
    public static SeriesNote MapSeriesNote(ParsedMarkdown parsed){
        boolean isInfo = false;
        String seriesId = null;
        for(String token : parsed.GetTags()){
            String name = StripHash(token);
            String lower = name.toLowerCase();
            if(lower.equals("reviewinfo")){
                isInfo = true;
            } else if(!lower.equals("review") && !lower.equals("game") && !platformTags.contains(lower) && seriesId == null){
                seriesId = name;
            }
        }
        if(!isInfo || seriesId == null){
            return null;
        }

        List<String> text = new ArrayList<>(parsed.GetLooseLines());
        for(Map.Entry<String, List<String>> section : parsed.GetSections().entrySet()){
            text.add(section.getKey());
            text.addAll(section.getValue());
        }
        return new SeriesNote(seriesId, parsed.GetRelativePath(), String.join("\n", text));
    }

    // n/a means not relevant; an empty section stays empty (missing)
    private static FieldValue MapText(List<String> lines){
        if(lines.size() == 1 && lines.get(0).equalsIgnoreCase("n/a")){
            return new FieldValue(null, null, FieldStatus.NA);
        }
        return new FieldValue(String.join("\n", lines));
    }

    // First line is the rating, anything under it is the comment. Required ratings (Final Rating) are strict and
    // have no comment. Optional ones (Replay-ability) turn odd wording into ? and keep the wording as the comment
    private static FieldValue MapRating(TemplateField field, List<String> lines, String file, List<ImportIssue> issues){
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
            if(strict){
                issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "'" + heading + "' can't be n/a (it always needs a value)"));
                return new FieldValue("", comment, FieldStatus.FILLED);
            }
            return new FieldValue(null, null, FieldStatus.NA);
        }

        RatingState state = RatingRules.Classify(token);
        if(state == RatingState.NUMBER || state == RatingState.UNKNOWN){
            return new FieldValue(token, comment, FieldStatus.FILLED);
        }

        if(strict){
            issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "invalid '" + heading + "' value '" + token + "' (use a whole number from -1 to 11, or ?)"));
            return new FieldValue("", comment, FieldStatus.FILLED);
        }

        issues.add(new ImportIssue(file, IssueSeverity.NOTE, "'" + heading + "' was '" + token + "', imported as ? with that wording kept as the comment"));
        return new FieldValue("?", comment == null ? token : token + "\n" + comment, FieldStatus.FILLED);
    }

    private static String StripHash(String token){
        return token.startsWith("#") ? token.substring(1) : token;
    }

    private static List<String> StripHashes(List<String> tokens){
        List<String> names = new ArrayList<>();
        for(String token : tokens){
            String name = StripHash(token);
            if(!name.isEmpty()){
                names.add(name);
            }
        }
        return Collections.unmodifiableList(names);
    }
}
