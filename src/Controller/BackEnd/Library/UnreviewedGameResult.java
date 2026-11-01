package Controller.BackEnd.Library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// How a change to an unreviewed game went: what was wrong (empty if it worked)
public class UnreviewedGameResult {
    private List<String> errors;

    private UnreviewedGameResult(List<String> errors){
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public static UnreviewedGameResult Done(){ return new UnreviewedGameResult(new ArrayList<>()); }
    public static UnreviewedGameResult Refused(String error){ return new UnreviewedGameResult(List.of(error)); }

    public boolean Succeeded(){ return errors.isEmpty(); }
    public List<String> GetErrors(){ return errors; }
}
