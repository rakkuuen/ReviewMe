package Model.Reviews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// One section of a review. For rating fields text is the number (or ?) and comment is the note under it.
// A field that allows subsections (Voice Acting) also holds its groups and characters
public class FieldValue {
    private String text, comment;
    private FieldStatus status;
    private List<Subsection> subsections = new ArrayList<>();

    public FieldValue(String text, String comment, FieldStatus status){
        this.text = text;
        this.comment = comment;
        this.status = status;
    }

    public FieldValue(String text){
        this(text, null, FieldStatus.FILLED);
    }

    public String GetText(){ return text; }
    public void SetText(String text){ this.text = text; }

    public String GetComment(){ return comment; }
    public void SetComment(String comment){ this.comment = comment; }

    public FieldStatus GetStatus(){ return status; }
    public void SetStatus(FieldStatus status){ this.status = status; }

    public List<Subsection> GetSubsections(){ return Collections.unmodifiableList(subsections); }
    public void SetSubsections(List<Subsection> subsections){ this.subsections = new ArrayList<>(subsections); }

    public boolean IsNotApplicable(){
        return status == FieldStatus.NA;
    }

    // Empty text is only missing if no subsection has any text either
    public boolean IsMissing(){
        return status == FieldStatus.FILLED && (text == null || text.trim().isEmpty()) && !AnySubsectionHasText();
    }

    private boolean AnySubsectionHasText(){
        for(Subsection subsection : subsections){
            if(subsection.HasText()){
                return true;
            }
        }
        return false;
    }
}
