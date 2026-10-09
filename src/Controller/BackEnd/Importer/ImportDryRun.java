package Controller.BackEnd.Importer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Controller.BackEnd.LocalConfig;
import Controller.BackEnd.Importer.Report.ReportBuilder;
import Model.Reviews.Template;
import Model.Reviews.TemplateLoader;
import Controller.BackEnd.Importer.Results.ImportResult;

// Reads the whole vault the way the real import will and reports what it would store. Writes nothing to the database.
// Run:  java -cp "bin;lib/*" Controller.BackEnd.Importer.ImportDryRun [vaultFolder] [reportFile]
public class ImportDryRun {
    private static final String defaultReport = "import_dry_run.txt";
    private static final String templateFolder = "Resources/Templates";

    public static void main(String[] args) throws IOException {
        Path root = Paths.get(args.length > 0 ? args[0] : LocalConfig.Get("vault.path"));
        Path reportFile = Paths.get(args.length > 1 ? args[1] : defaultReport);

        Map<String, Template> templates = LoadTemplates();
        ImportResult result = VaultImporter.Run(root, templates);
        List<String> report = ReportBuilder.Build(result);

        PrintAndSave(report, reportFile);
    }

    private static Map<String, Template> LoadTemplates() throws IOException {
        Map<String, Template> templates = new LinkedHashMap<>();
        for(Template template : TemplateLoader.LoadAll(templateFolder)){
            templates.put(template.GetId(), template);
        }
        return templates;
    }

    private static void PrintAndSave(List<String> report, Path reportFile) throws IOException {
        PrintStream console = new PrintStream(System.out, true, "UTF-8");
        for(String line : report){
            console.println(line);
        }
        Files.write(reportFile, report, StandardCharsets.UTF_8);
        console.println();
        console.println("Report saved to " + reportFile.toAbsolutePath());
    }
}
