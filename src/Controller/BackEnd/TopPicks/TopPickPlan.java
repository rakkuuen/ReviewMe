package Controller.BackEnd.TopPicks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// What a Top picks change would do: the reviews that move (empty when nothing needs to), or why it can't be done
public class TopPickPlan {
    private final List<TopPickChange> changes;
    private final String error;

    private TopPickPlan(List<TopPickChange> changes, String error){
        this.changes = Collections.unmodifiableList(new ArrayList<>(changes));
        this.error = error;
    }

    public static TopPickPlan Of(List<TopPickChange> changes){ return new TopPickPlan(changes, null); }
    public static TopPickPlan Nothing(){ return new TopPickPlan(new ArrayList<>(), null); }
    public static TopPickPlan Refused(String error){ return new TopPickPlan(new ArrayList<>(), error); }

    public boolean IsRefused(){ return error != null; }
    public String GetError(){ return error; }
    public List<TopPickChange> GetChanges(){ return changes; }
}
