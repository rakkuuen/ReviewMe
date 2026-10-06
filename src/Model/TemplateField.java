package Model;

public class TemplateField {
    private String fieldKey, heading, placeholder;
    private FieldKind kind;
    private boolean required;
    private int displayOrder;

    // placeholder may be null (no hint text)
    public TemplateField(String fieldKey, String heading, String placeholder, FieldKind kind, boolean required, int displayOrder){
        this.fieldKey = fieldKey;
        this.heading = heading;
        this.placeholder = placeholder;
        this.kind = kind;
        this.required = required;
        this.displayOrder = displayOrder;
    }

    public String GetFieldKey(){ return fieldKey; }
    public String GetHeading(){ return heading; }
    public String GetPlaceholder(){ return placeholder; }
    public FieldKind GetKind(){ return kind; }
    public boolean IsRequired(){ return required; }
    public int GetDisplayOrder(){ return displayOrder; }
}
