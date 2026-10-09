package Controller.BackEnd.Importer.Report;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import Controller.BackEnd.Importer.Results.ImportIssue;
import Controller.BackEnd.Importer.Results.ImportResult;
import Controller.BackEnd.Importer.Results.IssueSeverity;
import Controller.BackEnd.Importer.Results.MappedReview;
import Controller.BackEnd.Importer.Results.MappedReview.TagState;

// Counts: reviews, templates, state tags, sections, problems
public class SummarySection implements ReportSection {

    @Override
    public List<String> Lines(ImportResult result){
        int filled = 0, missing = 0, notApplicable = 0;
        Map<String, Integer> byTemplate = new TreeMap<>();
        Map<TagState, Integer> byState = new EnumMap<>(TagState.class);
        for(MappedReview m : result.GetReviews()){
            filled += m.GetFilled(); missing += m.GetMissing(); notApplicable += m.GetNotApplicable();
            byTemplate.merge(m.GetReview().GetTemplateId(), 1, Integer::sum);
            byState.merge(m.GetTagState(), 1, Integer::sum);
        }

        List<ImportIssue> issues = result.GetIssues();
        long problems = issues.stream().filter(i -> i.GetSeverity() == IssueSeverity.PROBLEM).count();

        List<String> out = new ArrayList<>();
        out.add("== SUMMARY ==");
        out.add("reviews: " + result.GetReviews().size() + " | series notes files: " + result.GetSeriesNotes().size()
                + " | skipped: " + result.GetSkipped().size());
        out.add("by template: " + byTemplate);
        out.add("by state tag: " + byState);
        out.add("sections: " + filled + " filled, " + missing + " missing, " + notApplicable + " n/a");
        out.add("problems: " + problems + " | notes: " + (issues.size() - problems));
        out.add("");
        return out;
    }
}
