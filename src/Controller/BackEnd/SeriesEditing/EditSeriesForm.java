package Controller.BackEnd.SeriesEditing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import Controller.BackEnd.SeriesManagement.SeriesChecker;
import Model.Reviews.Series;

// What the Edit series form holds as it is changed: the name, template and notes, and the games in the order shown (moved up or down,
// or taken out). It knows what has changed from how it started, what is wrong with the name and whether Save can be pressed. The tag is
// fixed once a series exists, so it only shows. No drawing and no database in here
public class EditSeriesForm {
    private final Series original;
    private final List<String> originalTitles;
    private final List<Series> allSeries;
    private final Map<String, String> templates;   // id to the name shown
    private String name;
    private String templateId;
    private String notes;
    private final List<String> titles;

    public EditSeriesForm(Series series, List<String> gameTitlesInOrder, List<Series> allSeries, Map<String, String> templates){
        this.original = series;
        this.originalTitles = Collections.unmodifiableList(new ArrayList<>(gameTitlesInOrder));
        this.allSeries = allSeries;
        this.templates = templates;
        this.name = series.GetName();
        this.templateId = series.GetTemplateId();
        this.notes = series.GetNotes() == null ? "" : series.GetNotes();
        this.titles = new ArrayList<>(gameTitlesInOrder);
    }

    public String GetSeriesId(){ return original.GetId(); }

    // "Tag for your notes: #Yakuza" (fixed: a rename only changes the display name)
    public String GetTagLine(){ return "Tag for your notes: #" + original.GetId(); }

    public String GetName(){ return name; }
    public void SetName(String typed){ name = typed == null ? "" : typed; }

    public String GetTemplateId(){ return templateId; }
    public void SetTemplateId(String id){
        if(templates.containsKey(id)){
            templateId = id;
        }
    }
    public Map<String, String> GetTemplates(){ return Collections.unmodifiableMap(templates); }

    public String GetNotes(){ return notes; }
    public void SetNotes(String typed){ notes = typed == null ? "" : typed; }

    // The games as they stand now, in order
    public List<String> GetTitles(){ return Collections.unmodifiableList(titles); }

    public boolean CanMoveUp(String title){ return titles.indexOf(title) > 0; }
    public boolean CanMoveDown(String title){ int at = titles.indexOf(title); return at >= 0 && at + 1 < titles.size(); }

    public void MoveUp(String title){
        if(CanMoveUp(title)){
            Collections.swap(titles, titles.indexOf(title), titles.indexOf(title) - 1);
        }
    }

    public void MoveDown(String title){
        if(CanMoveDown(title)){
            Collections.swap(titles, titles.indexOf(title), titles.indexOf(title) + 1);
        }
    }

    public void Remove(String title){
        titles.remove(title);
    }

    public boolean NameChanged(){ return !SeriesChecker.Clean(name).equals(original.GetName()); }
    public boolean TemplateChanged(){ return templateId != null && !templateId.equals(original.GetTemplateId()); }
    public boolean NotesChanged(){ return !notes.equals(original.GetNotes() == null ? "" : original.GetNotes()); }

    // The games taken out, in the order they were in
    public List<String> GetRemoved(){
        List<String> removed = new ArrayList<>();
        for(String title : originalTitles){
            if(!titles.contains(title)){
                removed.add(title);
            }
        }
        return removed;
    }

    // True when the games that are left are in a different order from how they started
    public boolean OrderChanged(){
        List<String> before = new ArrayList<>(originalTitles);
        before.retainAll(titles);
        return !before.equals(titles);
    }

    public boolean IsDirty(){
        return NameChanged() || TemplateChanged() || NotesChanged() || !GetRemoved().isEmpty() || OrderChanged();
    }

    // What is wrong with the name (nothing when it is as it started)
    public List<String> GetProblems(){
        return NameChanged() ? SeriesChecker.CheckRename(name, allSeries, original.GetId()) : new ArrayList<>();
    }

    // "53 / 40" once the name is past the limit, otherwise null
    public String GetCounter(){
        int length = SeriesChecker.Clean(name).length();
        return length > SeriesChecker.maxNameLength ? length + " / " + SeriesChecker.maxNameLength : null;
    }

    public boolean CanSave(){
        return IsDirty() && GetProblems().isEmpty();
    }
}
