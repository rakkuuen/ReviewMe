package Controller.BackEnd.Importer.Mapping;

import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.Importer.Parsing.Section;
import Model.Reviews.Subsection;

// Turns the headings nested inside a field into its groups and characters (Voice Acting > Main cast > Tara)
public class SubsectionMapper {

    // In file order. A heading used twice stays as two entries
    public static List<Subsection> Map(Section field){
        List<Subsection> subsections = new ArrayList<>();
        for(Section child : field.GetChildren()){
            Subsection subsection = new Subsection(child.GetHeading(), String.join("\n", child.GetLines()));
            for(Subsection nested : Map(child)){
                subsection.AddChild(nested);
            }
            subsections.add(subsection);
        }
        return subsections;
    }
}
