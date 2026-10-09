package Model.Reviews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A named part inside a field with its own text, and parts nested in it. For Voice Acting:
// a group ("Main cast") holding characters ("Tara"), each with their own text
public class Subsection {
    private String name, text;
    private List<Subsection> children = new ArrayList<>();

    public Subsection(String name, String text){
        this.name = name;
        this.text = text;
    }

    public String GetName(){ return name; }
    public void SetName(String name){ this.name = name; }

    public String GetText(){ return text; }
    public void SetText(String text){ this.text = text; }

    public List<Subsection> GetChildren(){ return Collections.unmodifiableList(children); }
    public void AddChild(Subsection child){ children.add(child); }

    // True if this part or anything nested in it has some text
    public boolean HasText(){
        if(text != null && !text.trim().isEmpty()){
            return true;
        }
        for(Subsection child : children){
            if(child.HasText()){
                return true;
            }
        }
        return false;
    }
}
