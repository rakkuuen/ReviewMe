package Screens.Detail;

import java.util.ArrayList;
import java.util.List;

import Model.Reviews.TemplateField;
import Model.Widgets.EditableField;

// One template field on screen: its main box, its comment box (ratings that have one), and its groups with characters
public class SectionBinding {
    private TemplateField field;
    private EditableField mainBox;
    private EditableField commentBox;   // null unless this is a rating with a comment
    private List<SubsectionBinding> groups = new ArrayList<>();

    public SectionBinding(TemplateField field, EditableField mainBox){
        this.field = field;
        this.mainBox = mainBox;
    }

    public TemplateField GetField(){ return field; }
    public EditableField GetMainBox(){ return mainBox; }

    public EditableField GetCommentBox(){ return commentBox; }
    public void SetCommentBox(EditableField commentBox){ this.commentBox = commentBox; }

    public List<SubsectionBinding> GetGroups(){ return groups; }
}
