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
}
