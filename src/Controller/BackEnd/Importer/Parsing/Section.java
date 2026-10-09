package Controller.BackEnd.Importer.Parsing;

import java.util.ArrayList;
import java.util.List;

// One heading in an md file with the text directly under it, plus any lower-level headings nested inside it
// (# Voice Acting > ## Main cast > ### Tara)
public class Section {
    private String heading;
    private int level;
    private List<String> lines = new ArrayList<>();
    private List<Section> children = new ArrayList<>();

    public Section(String heading, int level){
        this.heading = heading;
        this.level = level;
    }

    public String GetHeading(){ return heading; }
    public int GetLevel(){ return level; }
    public List<String> GetLines(){ return lines; }
    public List<Section> GetChildren(){ return children; }

    public void AddChild(Section child){
        children.add(child);
    }

    // Lines of text in every section nested below this one (not this section's own lines)
    public int CountLinesBelow(){
        int total = 0;
        for(Section child : children){
            total += child.GetLines().size() + child.CountLinesBelow();
        }
        return total;
    }

    // This section's own lines plus everything nested below it
    public int CountAllLines(){
        return lines.size() + CountLinesBelow();
    }
}
