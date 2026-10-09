package Controller.BackEnd.Importer.Report;

import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.Importer.ImportResult;
import Controller.BackEnd.Importer.MappedReview;
import Controller.BackEnd.Importer.MappedReview.TagState;

// Checks each state tag against what the sections actually hold
public class TagCheckSection implements ReportSection {

    @Override
    public List<String> Lines(ImportResult result){
        List<String> completedButMissing = new ArrayList<>();
        List<String> incompleteButFull = new ArrayList<>();
        int inProgress = 0;
        for(MappedReview m : result.GetReviews()){
            String title = m.GetReview().GetTitle();
            if(m.GetTagState() == TagState.COMPLETED && m.GetMissing() > 0){
                completedButMissing.add(title + " (" + m.GetMissing() + " missing)");
            } else if(m.GetTagState() == TagState.REVIEW_INCOMPLETE && m.GetMissing() == 0){
                incompleteButFull.add(title);
            } else if(m.GetTagState() == TagState.IN_PROGRESS){
                inProgress++;
            }
        }

        List<String> out = new ArrayList<>();
        out.add("== TAG STATE vs CONTENT ==");
        out.add("tagged #Completed but a section is missing: " + Describe(completedButMissing));
        out.add("tagged #REVIEWINCOMPLETE but nothing is missing: " + Describe(incompleteButFull));
        out.add("tagged #GameInProgress (exempt from the check): " + inProgress);
        out.add("");
        return out;
    }

    private String Describe(List<String> titles){
        return titles.isEmpty() ? "none" : titles.size() + " -> " + String.join(", ", titles);
    }
}
