package Controller.BackEnd.Importer.Report;

import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.Importer.Results.ImportResult;
import Controller.BackEnd.Importer.Results.MappedReview;
import Model.Reviews.GameReview;

// One row per review. Last section, so no trailing blank line
public class ReviewTableSection implements ReportSection {
    private static final String rowFormat = "%-48s %-9s %-16s %-18s %3s %3s %3s  %-5s %s";

    @Override
    public List<String> Lines(ImportResult result){
        List<String> out = new ArrayList<>();
        out.add("== EVERY REVIEW ==  (F = filled, M = missing, N = n/a)");
        out.add(String.format(rowFormat, "title", "template", "series", "state tag", "F", "M", "N", "final", "replay"));
        for(MappedReview m : result.GetReviews()){
            GameReview review = m.GetReview();
            out.add(String.format(rowFormat, ReportFormat.Shorten(review.GetTitle(), 48), review.GetTemplateId(),
                    review.GetSeriesId() == null ? "-" : review.GetSeriesId(), m.GetTagState(),
                    m.GetFilled(), m.GetMissing(), m.GetNotApplicable(),
                    ReportFormat.Rating(review.GetFieldValue("finalRating"), false),
                    ReportFormat.Rating(review.GetFieldValue("replayability"), true)));
        }
        return out;
    }
}
