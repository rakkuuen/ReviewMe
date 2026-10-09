package Controller.BackEnd.Importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Model.Reviews.Template;

// Walks the vault and runs every md file through the parser and mapper. Writes nothing
public class VaultImporter {

    public static ImportResult Run(Path root, Map<String, Template> templates) throws IOException {
        ImportResult result = new ImportResult(root, templates);
        Map<String, List<String>> pathsByTitle = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for(Path file : FindMarkdownFiles(root)){
            ImportFile(file, result, pathsByTitle);
        }

        FlagDuplicateTitles(pathsByTitle, result.GetIssues());
        return result;
    }

    private static List<Path> FindMarkdownFiles(Path root) throws IOException {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(p -> p.toString().endsWith(".md"))
                    .sorted(Comparator.comparing(p -> root.relativize(p).toString().toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    // A review, a series rules file, or something to skip. A file that can't be read is a problem, not a crash
    private static void ImportFile(Path file, ImportResult result, Map<String, List<String>> pathsByTitle){
        ParsedMarkdown parsed;
        try {
            parsed = MarkdownFileParser.Parse(file, result.GetRoot());
        } catch (IOException e) {
            String path = result.GetRoot().relativize(file).toString().replace('\\', '/');
            result.GetIssues().add(new ImportIssue(path, IssueSeverity.PROBLEM, "couldn't read the file: " + e.getMessage()));
            return;
        }

        if(ReviewMapper.IsReview(parsed)){
            result.GetReviews().add(ReviewMapper.Map(parsed, result.GetTemplates(), result.GetIssues()));
            pathsByTitle.computeIfAbsent(parsed.GetTitle(), key -> new ArrayList<>()).add(parsed.GetRelativePath());
            return;
        }

        SeriesNote note = ReviewMapper.MapSeriesNote(parsed);
        if(note != null){
            result.GetSeriesNotes().add(note);
        } else {
            result.GetSkipped().add(parsed.GetRelativePath() + " (no #Review tag)");
        }
    }

    // Two files with the same title would collide in the database
    private static void FlagDuplicateTitles(Map<String, List<String>> pathsByTitle, List<ImportIssue> issues){
        for(Map.Entry<String, List<String>> entry : pathsByTitle.entrySet()){
            if(entry.getValue().size() > 1){
                for(String path : entry.getValue()){
                    issues.add(new ImportIssue(path, IssueSeverity.PROBLEM, "title '" + entry.getKey() + "' is used by "
                            + entry.getValue().size() + " files: " + String.join(", ", entry.getValue())));
                }
            }
        }
    }
}
