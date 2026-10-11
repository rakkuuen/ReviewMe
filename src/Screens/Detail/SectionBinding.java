package Screens.Detail;

import java.util.ArrayList;
import java.util.List;

import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.Subsection;
import Model.Reviews.TemplateField;
import Model.Widgets.EditableField;
import Model.Widgets.ValueBox;

// One template field on screen: its main box (text or rating), its comment box (ratings that have one), and its groups with characters
public class SectionBinding {
    private TemplateField field;
    private ValueBox mainBox;
    private EditableField commentBox;   // null unless this is a rating with a comment
    private List<SubsectionBinding> groups = new ArrayList<>();

    public SectionBinding(TemplateField field, ValueBox mainBox){
        this.field = field;
        this.mainBox = mainBox;
    }

    public TemplateField GetField(){ return field; }
    public ValueBox GetMainBox(){ return mainBox; }

    public EditableField GetCommentBox(){ return commentBox; }
    public void SetCommentBox(EditableField commentBox){ this.commentBox = commentBox; }

    public List<SubsectionBinding> GetGroups(){ return groups; }

    // Writes what is in the boxes into the review. A field the review didn't have stays absent until something is typed in it
    public void ReadInto(GameReview review){
        String text = mainBox.GetValue().strip();
        String comment = commentBox == null ? null : commentBox.GetValue().strip();

        FieldValue value = review.GetFieldValue(field.GetFieldKey());
        if(value == null){
            if(text.isEmpty() && (comment == null || comment.isEmpty())){
                return;
            }
            value = new FieldValue(text);
            review.SetFieldValue(field.GetFieldKey(), value);
        }

        value.SetText(text);
        if(commentBox != null){
            value.SetComment(comment.isEmpty() ? null : comment);
        }
        if(field.AllowsSubsections()){
            List<Subsection> read = new ArrayList<>();
            for(SubsectionBinding group : groups){
                read.add(group.ReadBoxes());
            }
            value.SetSubsections(read);
        }
    }

    // Puts the review's stored values back in the boxes (used to throw away changes)
    public void ShowFrom(GameReview review){
        FieldValue value = review.GetFieldValue(field.GetFieldKey());
        mainBox.SetValue(value == null ? null : value.GetText());
        if(commentBox != null){
            commentBox.SetValue(value == null ? null : value.GetComment());
        }
        if(value != null){
            for(int i = 0; i < groups.size() && i < value.GetSubsections().size(); i++){
                groups.get(i).ShowFrom(value.GetSubsections().get(i));
            }
        }
    }
}
