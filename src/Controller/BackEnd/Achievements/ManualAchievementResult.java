package Controller.BackEnd.Achievements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// How a manual-achievement change went: what was wrong (empty if it worked), and for an add, the new achievement's id
public class ManualAchievementResult {
    private List<String> errors;
    private Integer id;

    public ManualAchievementResult(List<String> errors, Integer id){
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
        this.id = id;
    }

    public static ManualAchievementResult Done(){ return new ManualAchievementResult(new ArrayList<>(), null); }
    public static ManualAchievementResult Added(int id){ return new ManualAchievementResult(new ArrayList<>(), id); }
    public static ManualAchievementResult Refused(String error){ return new ManualAchievementResult(List.of(error), null); }

    public boolean Succeeded(){ return errors.isEmpty(); }
    public List<String> GetErrors(){ return errors; }
    public Integer GetId(){ return id; }
}
