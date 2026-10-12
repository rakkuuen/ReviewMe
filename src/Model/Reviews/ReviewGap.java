package Model.Reviews;

// One reason a review counts as incomplete
public class ReviewGap {
    public enum Kind { MISSING_SECTION, EMPTY_SUBSECTION, MANUAL_FLAG }

    private Kind kind;
    private String fieldKey;   // Null for the manual flag, which isn't about one field
    private String path;       // What to show: "Final Rating", "Story > Elena", "Voice Acting > Main cast > Tara"

    public ReviewGap(Kind kind, String fieldKey, String path){
        this.kind = kind;
        this.fieldKey = fieldKey;
        this.path = path;
    }

    public Kind GetKind(){ return kind; }
    public String GetFieldKey(){ return fieldKey; }
    public String GetPath(){ return path; }
}
