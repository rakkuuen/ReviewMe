package Controller.BackEnd.Importer.Writing;

import java.util.ArrayList;
import java.util.List;

// What a write pass stored, and what it couldn't
public class WriteResult {
    private int reviews, fieldRows, subsectionFields, tagRows, series;
    private List<String> failures = new ArrayList<>();

    public void CountReview(){ reviews++; }
    public void CountFieldRows(int count){ fieldRows += count; }
    public void CountSubsectionField(){ subsectionFields++; }
    public void CountTagRows(int count){ tagRows += count; }
    public void CountSeries(){ series++; }

    // what is the review title or series id the failure is about
    public void Fail(String what, String message){
        failures.add(what + ": " + message);
    }

    public int GetReviews(){ return reviews; }
    public int GetFieldRows(){ return fieldRows; }
    public int GetSubsectionFields(){ return subsectionFields; }
    public int GetTagRows(){ return tagRows; }
    public int GetSeries(){ return series; }
    public List<String> GetFailures(){ return failures; }

    public String Describe(){
        return reviews + " reviews, " + fieldRows + " field rows, " + subsectionFields + " fields with groups/characters, "
                + tagRows + " tags, " + series + " series";
    }
}
