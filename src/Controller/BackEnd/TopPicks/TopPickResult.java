package Controller.BackEnd.TopPicks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// How a Top picks change went: what was wrong (empty if it worked)
public class TopPickResult {
    private final List<String> errors;

    private TopPickResult(List<String> errors){
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public static TopPickResult Done(){ return new TopPickResult(new ArrayList<>()); }
    public static TopPickResult Refused(String error){ return new TopPickResult(List.of(error)); }

    public boolean Succeeded(){ return errors.isEmpty(); }
    public List<String> GetErrors(){ return errors; }
}
