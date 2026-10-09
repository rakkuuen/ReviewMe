package Controller.BackEnd.Importer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Model.Reviews.Template;

// Everything one pass over the vault produced
public class ImportResult {
    private Path root;
    private Map<String, Template> templates;
    private List<MappedReview> reviews = new ArrayList<>();
    private List<SeriesNote> seriesNotes = new ArrayList<>();
    private List<String> skipped = new ArrayList<>();
    private List<ImportIssue> issues = new ArrayList<>();

    public ImportResult(Path root, Map<String, Template> templates){
        this.root = root;
        this.templates = templates;
    }

    public Path GetRoot(){ return root; }
    public Map<String, Template> GetTemplates(){ return templates; }
    public List<MappedReview> GetReviews(){ return reviews; }
    public List<SeriesNote> GetSeriesNotes(){ return seriesNotes; }
    public List<String> GetSkipped(){ return skipped; }
    public List<ImportIssue> GetIssues(){ return issues; }
}
