package Controller.BackEnd.SeriesManagement;

import java.util.ArrayList;
import java.util.List;

import Model.Reviews.SeriesChange;

// What a series operation would do, worked out without saving anything: the change to write, the change that undoes it,
// which games would switch template, and why it was refused if it was. A plan with no errors and an empty change is a no-op
public class SeriesPlan {
    private List<String> errors = new ArrayList<>();
    private SeriesChange change = new SeriesChange();
    private SeriesChange undo = new SeriesChange();
    private List<TemplateSwitch> switches = new ArrayList<>();

    public static SeriesPlan Refused(String error){
        SeriesPlan plan = new SeriesPlan();
        plan.AddError(error);
        return plan;
    }

    public static SeriesPlan Refused(List<String> errors){
        SeriesPlan plan = new SeriesPlan();
        plan.errors.addAll(errors);
        return plan;
    }

    public void AddError(String error){ errors.add(error); }
    public boolean Succeeded(){ return errors.isEmpty(); }
    public List<String> GetErrors(){ return errors; }

    public SeriesChange GetChange(){ return change; }
    public SeriesChange GetUndo(){ return undo; }

    public void AddSwitch(TemplateSwitch templateSwitch){ switches.add(templateSwitch); }
    public List<TemplateSwitch> GetSwitches(){ return switches; }
}
