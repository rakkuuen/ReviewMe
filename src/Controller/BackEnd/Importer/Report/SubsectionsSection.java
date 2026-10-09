package Controller.BackEnd.Importer.Report;

import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.Importer.Results.ImportResult;
import Controller.BackEnd.Importer.Results.MappedReview;
import Model.Reviews.FieldValue;
import Model.Reviews.Subsection;

// Reviews with groups and characters (Voice Acting), showing what was read
public class SubsectionsSection implements ReportSection {

    @Override
    public List<String> Lines(ImportResult result){
        List<String> out = new ArrayList<>();
        out.add("== GROUPS AND CHARACTERS ==");

        boolean any = false;
        for(MappedReview m : result.GetReviews()){
            for(String fieldKey : m.GetReview().GetFieldValues().keySet()){
                FieldValue value = m.GetReview().GetFieldValue(fieldKey);
                if(value.GetSubsections().isEmpty()){
                    continue;
                }
                any = true;
                out.add(m.GetReview().GetTitle() + " / " + fieldKey + (value.IsMissing() ? "  [no text anywhere]" : ""));
                for(Subsection group : value.GetSubsections()){
                    out.add("    " + group.GetName() + " (" + group.GetChildren().size() + " characters): " + CharacterNames(group));
                }
            }
        }
        if(!any){
            out.add("none");
        }
        out.add("");
        return out;
    }

    private String CharacterNames(Subsection group){
        List<String> names = new ArrayList<>();
        for(Subsection character : group.GetChildren()){
            names.add(character.GetName() + (character.HasText() ? "" : " [empty]"));
        }
        return String.join(", ", names);
    }
}
