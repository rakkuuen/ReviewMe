package Controller.BackEnd.Importer.Report;

import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.Importer.ImportResult;
import Controller.BackEnd.Importer.MappedReview;
import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;

// The ratings worth a look: every ? and every missing Final Rating
public class RatingsSection implements ReportSection {

    @Override
    public List<String> Lines(ImportResult result){
        List<String> finalUnknown = new ArrayList<>(), replayUnknown = new ArrayList<>(), finalMissing = new ArrayList<>();
        for(MappedReview m : result.GetReviews()){
            GameReview review = m.GetReview();
            FieldValue finalRating = review.GetFieldValue("finalRating");
            FieldValue replay = review.GetFieldValue("replayability");

            if(finalRating == null || finalRating.IsMissing()){
                finalMissing.add(review.GetTitle());
            } else if("?".equals(finalRating.GetText())){
                finalUnknown.add(review.GetTitle());
            }
            if(replay != null && "?".equals(replay.GetText())){
                replayUnknown.add(review.GetTitle() + CommentNote(replay));
            }
        }

        List<String> out = new ArrayList<>();
        out.add("== RATINGS ==");
        out.add("Final Rating = ?  (" + finalUnknown.size() + "): " + String.join(", ", finalUnknown));
        out.add("Replay-ability = ?  (" + replayUnknown.size() + "):");
        for(String line : replayUnknown){
            out.add("    " + line);
        }
        out.add("Final Rating missing  (" + finalMissing.size() + "): " + String.join(", ", finalMissing));
        out.add("");
        return out;
    }

    private String CommentNote(FieldValue replay){
        if(replay.GetComment() == null){
            return "";
        }
        return "  [comment: " + ReportFormat.Shorten(replay.GetComment().replace("\n", " / "), 70) + "]";
    }
}
