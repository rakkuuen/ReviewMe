package Controller.BackEnd.Importer.Mapping;

import Model.Reviews.FieldValue;

// How many of a review's sections are filled, missing or n/a
public class SectionCounts {
    private int filled, missing, notApplicable;

    public void Add(FieldValue value){
        if(value.IsNotApplicable()){
            notApplicable++;
        } else if(value.IsMissing()){
            missing++;
        } else {
            filled++;
        }
    }

    // A required field with no heading at all counts as missing
    public void AddMissing(){
        missing++;
    }

    public int GetFilled(){ return filled; }
    public int GetMissing(){ return missing; }
    public int GetNotApplicable(){ return notApplicable; }
}
