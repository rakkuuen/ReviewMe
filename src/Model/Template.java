package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Template {
    private String id, name;
    private List<TemplateField> fields = new ArrayList<>();

    public Template(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String GetId(){ return id; }
    public String GetName(){ return name; }

    public void AddField(TemplateField field){
        fields.add(field);
    }

    public List<TemplateField> GetFields(){
        return Collections.unmodifiableList(fields);
    }
}
