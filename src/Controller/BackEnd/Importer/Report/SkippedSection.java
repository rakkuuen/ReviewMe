package Controller.BackEnd.Importer.Report;

import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.Importer.Results.ImportResult;

// Files that were neither a review nor a series rules file. Prints nothing if there are none
public class SkippedSection implements ReportSection {

    @Override
    public List<String> Lines(ImportResult result){
        List<String> out = new ArrayList<>();
        if(result.GetSkipped().isEmpty()){
            return out;
        }
        out.add("== SKIPPED FILES ==");
        out.addAll(result.GetSkipped());
        out.add("");
        return out;
    }
}
