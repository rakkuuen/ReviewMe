package Screens.Detail;

import java.util.ArrayList;
import java.util.List;

import Model.Reviews.Subsection;
import Model.Widgets.EditableField;

// A group or character on screen: the part it came from and the box showing its text
public class SubsectionBinding {
    private Subsection subsection;
    private EditableField field;
    private List<SubsectionBinding> children = new ArrayList<>();

    public SubsectionBinding(Subsection subsection, EditableField field){
        this.subsection = subsection;
        this.field = field;
    }

    public Subsection GetSubsection(){ return subsection; }
    public EditableField GetField(){ return field; }
    public List<SubsectionBinding> GetChildren(){ return children; }

    // A new part with this one's name and whatever is in its boxes now
    public Subsection ReadBoxes(){
        Subsection read = new Subsection(subsection.GetName(), field.GetValue().strip());
        for(SubsectionBinding child : children){
            read.AddChild(child.ReadBoxes());
        }
        return read;
    }

    // Puts a stored part's text back in the boxes. The stored part has the same shape as this binding
    public void ShowFrom(Subsection stored){
        field.SetValue(stored.GetText());
        for(int i = 0; i < children.size() && i < stored.GetChildren().size(); i++){
            children.get(i).ShowFrom(stored.GetChildren().get(i));
        }
    }
}
