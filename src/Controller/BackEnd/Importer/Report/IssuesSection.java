package Controller.BackEnd.Importer.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import Controller.BackEnd.Importer.ImportIssue;
import Controller.BackEnd.Importer.ImportResult;
import Controller.BackEnd.Importer.IssueSeverity;

// Problems or notes (one instance each), grouped by file
public class IssuesSection implements ReportSection {
    private IssueSeverity severity;
    private String heading;

    public IssuesSection(IssueSeverity severity, String heading){
        this.severity = severity;
        this.heading = heading;
    }

    @Override
    public List<String> Lines(ImportResult result){
        Map<String, List<String>> byFile = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for(ImportIssue issue : result.GetIssues()){
            if(issue.GetSeverity() == severity){
                byFile.computeIfAbsent(issue.GetFile(), key -> new ArrayList<>()).add(issue.GetMessage());
            }
        }

        List<String> out = new ArrayList<>();
        out.add(heading);
        if(byFile.isEmpty()){
            out.add("none");
        }
        for(Map.Entry<String, List<String>> entry : byFile.entrySet()){
            out.add(entry.getKey());
            for(String message : entry.getValue()){
                out.add("    - " + message);
            }
        }
        out.add("");
        return out;
    }
}
