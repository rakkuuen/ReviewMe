package Controller.BackEnd.Importer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Controller.BackEnd.LocalConfig;
import Controller.BackEnd.Importer.Results.ImportIssue;
import Controller.BackEnd.Importer.Results.ImportResult;
import Controller.BackEnd.Importer.Results.IssueSeverity;
import Controller.BackEnd.Importer.Writing.DatabaseBackup;
import Controller.BackEnd.Importer.Writing.ImportWriter;
import Controller.BackEnd.Importer.Writing.RebuildVerifier;
import Controller.BackEnd.Importer.Writing.WriteResult;
import Database.Dao.GameReviewDao;
import Model.Reviews.Template;
import Model.Reviews.TemplateLoader;

// Rebuilds the database from the md files: backs up the old one, builds a fresh one, writes everything, checks it.
// Close the app first. Run:  java -cp "bin;lib/*" Controller.BackEnd.Importer.RebuildDatabase [--force]
// By default it refuses to write while the importer reports problems (the same ones ImportDryRun shows)
public class RebuildDatabase {
    private static final String templateFolder = "Resources/Templates";

    public static void main(String[] args) throws IOException {
        boolean force = args.length > 0 && args[0].equals("--force");
        Path vault = Paths.get(LocalConfig.Get("vault.path"));

        ImportResult result = VaultImporter.Run(vault, LoadTemplates());
        System.out.println("Read " + result.GetReviews().size() + " reviews from " + vault);
        if(!ProblemsAllowRebuild(result, force)){
            return;
        }

        Path backup = DatabaseBackup.MoveAside();
        System.out.println(backup == null ? "No existing database to back up." : "Old database moved to " + backup);

        GameReviewDao.Setup();
        WriteResult written = ImportWriter.WriteAll(result);
        System.out.println("Wrote " + written.Describe());

        List<String> differences = RebuildVerifier.Verify(result);
        ReportOutcome(written, differences, backup);
    }

    private static Map<String, Template> LoadTemplates() throws IOException {
        Map<String, Template> templates = new LinkedHashMap<>();
        for(Template template : TemplateLoader.LoadAll(templateFolder)){
            templates.put(template.GetId(), template);
        }
        return templates;
    }

    // Problems mean text would be lost or tags are wrong, so stop unless told otherwise
    private static boolean ProblemsAllowRebuild(ImportResult result, boolean force){
        long problems = result.GetIssues().stream().filter(i -> i.GetSeverity() == IssueSeverity.PROBLEM).count();
        if(problems == 0 || force){
            return true;
        }
        System.out.println("Not rebuilding: the importer found " + problems + " problem(s). Fix them in the md (or pass --force):");
        for(ImportIssue issue : result.GetIssues()){
            if(issue.GetSeverity() == IssueSeverity.PROBLEM){
                System.out.println("  " + issue.GetFile() + ": " + issue.GetMessage());
            }
        }
        return false;
    }

    private static void ReportOutcome(WriteResult written, List<String> differences, Path backup){
        for(String failure : written.GetFailures()){
            System.out.println("FAILED: " + failure);
        }
        for(String difference : differences){
            System.out.println("DIFFERENT: " + difference);
        }

        if(written.GetFailures().isEmpty() && differences.isEmpty()){
            System.out.println("Verified: everything read back matches what was imported.");
        } else {
            System.out.println("Something is wrong. To go back, delete Resources/Databases/GameReview.db and rename "
                    + (backup == null ? "(there was no old database)" : backup.getFileName()) + " to GameReview.db");
        }
    }
}
