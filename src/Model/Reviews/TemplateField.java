package Model.Reviews;

public class TemplateField {
    private String fieldKey, heading, placeholder;
    private FieldKind kind;
    private boolean required, allowsSubsections;
    private int displayOrder;

    // placeholder may be null (no hint text). allowsSubsections means groups and characters can sit inside the field
    public TemplateField(String fieldKey, String heading, String placeholder, FieldKind kind, boolean required, int displayOrder,
                         boolean allowsSubsections){
        this.fieldKey = fieldKey;
        this.heading = heading;
        this.placeholder = placeholder;
        this.kind = kind;
        this.required = required;
        this.displayOrder = displayOrder;
        this.allowsSubsections = allowsSubsections;
    }

    // A field without subsections, which is nearly all of them
    public TemplateField(String fieldKey, String heading, String placeholder, FieldKind kind, boolean required, int displayOrder){
        this(fieldKey, heading, placeholder, kind, required, displayOrder, false);
    }

    public String GetFieldKey(){ return fieldKey; }
    public String GetHeading(){ return heading; }
    public String GetPlaceholder(){ return placeholder; }
    public FieldKind GetKind(){ return kind; }
    public boolean IsRequired(){ return required; }
    public int GetDisplayOrder(){ return displayOrder; }
    public boolean AllowsSubsections(){ return allowsSubsections; }
}
