package Controller.BackEnd.Importer;

import java.util.List;
import java.util.Map;

import Model.Reviews.GameReview;
import Model.Reviews.Template;

// Gives a parsed md file its meaning. Each step is its own class: TagReader, FieldMapper, RatingMapper
public class ReviewMapper {
    private static final String standardTemplateId = "Standard";

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
        GameReview review = new GameReview();
        review.SetTitle(parsed.GetTitle());
        review.SetSourcePath(parsed.GetRelativePath());

        TagInfo tags = TagReader.Read(parsed.GetTags(), parsed.GetRelativePath(), issues);
        review.SetInProgress(tags.IsInProgress());
        review.SetCompleted(tags.IsCompleted());
        review.SetSeriesId(tags.GetSeriesId());
        review.SetPlatforms(tags.GetPlatforms());
        review.SetGenres(TagReader.StripHashes(parsed.GetGenres()));
        review.SetMetadata(TagReader.StripHashes(parsed.GetMetadata()));

        Template template = ChooseTemplate(tags.GetSeriesId(), templates);
        review.SetTemplateId(template.GetId());

        SectionCounts counts = FieldMapper.MapFields(template, parsed, review, issues);
        FieldMapper.FlagUnmappedHeadings(template, parsed, issues);

        return new MappedReview(review, tags.GetTagState(), counts.GetFilled(), counts.GetMissing(), counts.GetNotApplicable());
    }

    // A template with the same id as the series (Yakuza -> Yakuza.template), otherwise Standard
    private static Template ChooseTemplate(String seriesId, Map<String, Template> templates){
        String templateId = seriesId != null && templates.containsKey(seriesId) ? seriesId : standardTemplateId;
        Template template = templates.get(templateId);
        if(template == null){
            throw new IllegalStateException("Template not found: " + templateId);
        }
        return template;
    }
}
