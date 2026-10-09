package Controller.BackEnd.Importer.Report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Controller.BackEnd.Importer.Results.ImportResult;
import Controller.BackEnd.Importer.Results.IssueSeverity;

// Chains the report sections in the order they appear. To add a block, write a ReportSection and add it to the list
public class ReportBuilder {
    private static final List<ReportSection> sections = Arrays.asList(
            new SummarySection(),
            new SeriesSection(),
            new TagCheckSection(),
            new RatingsSection(),
            new IssuesSection(IssueSeverity.PROBLEM, "== PROBLEMS (fix these in the md) =="),
            new IssuesSection(IssueSeverity.NOTE, "== NOTES (handled automatically) =="),
            new SkippedSection(),
            new ReviewTableSection());

    public static List<String> Build(ImportResult result){
        List<String> out = new ArrayList<>();
        out.add("IMPORT DRY RUN (nothing was written to the database)");
        out.add("Vault: " + result.GetRoot());
        out.add("");

        for(ReportSection section : sections){
            out.addAll(section.Lines(result));
        }
        return out;
    }
}
