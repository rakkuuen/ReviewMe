package Model.Reviews;

public class Series {
    private String id, name, templateId, notes;

    // id is the series tag without the #, e.g. Yakuza. templateId and notes may be null
    public Series(String id, String name, String templateId, String notes){
        this.id = id;
        this.name = name;
        this.templateId = templateId;
        this.notes = notes;
    }

    public String GetId(){ return id; }

    public String GetName(){ return name; }
    public void SetName(String name){ this.name = name; }

    public String GetTemplateId(){ return templateId; }
    public void SetTemplateId(String templateId){ this.templateId = templateId; }

    public String GetNotes(){ return notes; }
    public void SetNotes(String notes){ this.notes = notes; }
}
