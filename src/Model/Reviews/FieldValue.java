package Model.Reviews;

// One section of a review. For rating fields text is the number (or ?) and comment is the note under it
public class FieldValue {
    private String text, comment;
    private FieldStatus status;

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

    public boolean IsNotApplicable(){
        return status == FieldStatus.NA;
    }

    public boolean IsMissing(){
        return status == FieldStatus.FILLED && (text == null || text.trim().isEmpty());
    }
}
