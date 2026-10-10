package Controller.BackEnd.Reviews;

import java.util.ArrayList;
import java.util.List;

// Whether a save went through/error details
public class SaveResult {
    private List<String> errors = new ArrayList<>();

    public void AddError(String error){ errors.add(error); }
    public void AddErrors(List<String> more){ errors.addAll(more); }

    public boolean Succeeded(){ return errors.isEmpty(); }
    public List<String> GetErrors(){ return errors; }
}
