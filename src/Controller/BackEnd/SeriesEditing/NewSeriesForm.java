package Controller.BackEnd.SeriesEditing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Controller.BackEnd.SeriesManagement.SeriesChecker;
import Controller.BackEnd.SeriesManagement.SeriesTag;
import Model.Reviews.Series;

// What the New series form holds and says as it is filled in: the name typed, the template picked, the tag the name will give (shown
// live), what is wrong with the name and whether Create can be pressed. No drawing and no database in here; the rules are
// SeriesChecker's and SeriesTag's
public class NewSeriesForm {
    public static final String tagHint = "The tag for your notes appears here once you type a name";
    public static final String tagFixedNote = "Fixed once the series is created";

    private final List<Series> existing;
    private final Map<String, String> templates;   // id to the name shown, in the order offered
    private String name = "";
    private String templateId;

    // defaultTemplateId is picked to start with when it is one of the templates, otherwise the first
    public NewSeriesForm(List<Series> existing, Map<String, String> templates, String defaultTemplateId){
        this.existing = existing;
        this.templates = Collections.unmodifiableMap(new LinkedHashMap<>(templates));
        this.templateId = templates.containsKey(defaultTemplateId) ? defaultTemplateId : (templates.isEmpty() ? null : templates.keySet().iterator().next());
    }

    public String GetName(){ return name; }
    public void SetName(String typed){ name = typed == null ? "" : typed; }

    public String GetTemplateId(){ return templateId; }
    public void SetTemplateId(String id){
        if(templates.containsKey(id)){
            templateId = id;
        }
    }

    // The templates to choose from, id to name
    public Map<String, String> GetTemplates(){ return templates; }

    // "#SteamworldDig" for the name typed, or null while there is nothing to make a tag from
    public String GetTag(){
        String cleaned = SeriesChecker.Clean(name);
        return cleaned.isEmpty() ? null : "#" + SeriesTag.FromName(cleaned);
    }

    // The line under the name: the tag it will get, or the hint before anything is typed
    public String GetTagLine(){
        return GetTag() == null ? tagHint : "Tag for your notes: " + GetTag();
    }

    // What is wrong with the name. Nothing is said before a name is typed
    public List<String> GetProblems(){
        if(SeriesChecker.Clean(name).isEmpty()){
            return new ArrayList<>();
        }
        return SeriesChecker.CheckNew(name, existing);
    }

    // "53 / 40" once the name is past the limit, otherwise null
    public String GetCounter(){
        int length = SeriesChecker.Clean(name).length();
        return length > SeriesChecker.maxNameLength ? length + " / " + SeriesChecker.maxNameLength : null;
    }

    public boolean CanCreate(){
        return !SeriesChecker.Clean(name).isEmpty() && GetProblems().isEmpty() && templateId != null;
    }
}
