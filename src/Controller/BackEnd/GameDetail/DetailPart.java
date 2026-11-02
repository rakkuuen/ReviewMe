package Controller.BackEnd.GameDetail;

import java.util.Collections;
import java.util.List;

// A named part inside a section (a group like "Main cast", or a character inside it) with its own text and the parts nested in it.
// A part with no text anywhere in it is empty
public class DetailPart {
    private final String name;
    private final String text;
    private final boolean empty;
    private final List<DetailPart> children;

    public DetailPart(String name, String text, boolean empty, List<DetailPart> children){
        this.name = name;
        this.text = text;
        this.empty = empty;
        this.children = Collections.unmodifiableList(children);
    }

    public String GetName(){ return name; }

    // Empty string when the part has no text of its own
    public String GetText(){ return text; }
    public boolean IsEmpty(){ return empty; }
    public List<DetailPart> GetChildren(){ return children; }
}
