package Controller.BackEnd.SeriesManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Model.Reviews.SeriesChange;

// How a series operation went. When it worked, GetUndo() is what to pass to SeriesManager.Undo to put everything back
// (null when nothing changed), and GetSwitches() lists the games that changed template
public class SeriesResult {
    private List<String> errors;
    private SeriesChange undo;
    private List<TemplateSwitch> switches;

    public SeriesResult(List<String> errors, SeriesChange undo, List<TemplateSwitch> switches){
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
        this.undo = undo;
        this.switches = Collections.unmodifiableList(new ArrayList<>(switches));
    }

    public boolean Succeeded(){ return errors.isEmpty(); }
    public List<String> GetErrors(){ return errors; }
    public SeriesChange GetUndo(){ return undo; }
    public List<TemplateSwitch> GetSwitches(){ return switches; }
}
