package Screens.Detail;

import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.Reviews.LoadedReview;
import Model.Reviews.FieldKind;
import Model.Reviews.FieldValue;
import Model.Reviews.Subsection;
import Model.Reviews.TemplateField;
import Model.Widgets.AutoGrowFieldList;
import Model.Widgets.EditableField;

// Lays a review out as rows, one section per template field in template order
public class SectionBuilder {
    private static final String commentPlaceholder = "Add a comment (optional)";
    private static final int commentBoxHeight = 36;
    private static final int subBoxHeight = 44;
    private static final int groupLevel = 1;
    private static final int characterLevel = 2;

    public static List<SectionBinding> Build(LoadedReview loaded, AutoGrowFieldList list){
        List<SectionBinding> bindings = new ArrayList<>();
        for(TemplateField field : loaded.GetTemplate().GetFields()){
            FieldValue value = loaded.GetReview().GetFieldValue(field.GetFieldKey());
            if(IsShown(field, value)){
                bindings.add(BuildSection(field, value, list));
            }
        }
        return bindings;
    }

    // n/a sections are hidden, and so is a field this review doesn't have. A required one is always there
    private static boolean IsShown(TemplateField field, FieldValue value){
        if(value == null){
            return field.IsRequired();
        }
        return !value.IsNotApplicable();
    }

    private static SectionBinding BuildSection(TemplateField field, FieldValue value, AutoGrowFieldList list){
        if(field.GetKind() == FieldKind.RATING_OR_UNKNOWN){
            return BuildRating(field, value, list);
        }

        String text = value == null ? null : value.GetText();
        SectionBinding binding = new SectionBinding(field, list.AddField(field.GetHeading(), Shown(text), field.GetPlaceholder()));
        if(field.AllowsSubsections() && value != null){
            AddGroups(binding, value, list);
        }
        return binding;
    }

    // A rating stepper for the number, and under it a comment box if this rating has one (Final Rating doesn't)
    private static SectionBinding BuildRating(TemplateField field, FieldValue value, AutoGrowFieldList list){
        String text = value == null ? null : value.GetText();
        SectionBinding binding = new SectionBinding(field, list.AddRating(field.GetHeading(), text));
        if(!field.IsRequired()){
            String comment = value == null ? null : value.GetComment();
            binding.SetCommentBox(list.AddField("", Shown(comment), commentPlaceholder, commentBoxHeight));
        }
        return binding;
    }

    // Each group is a row indented one step, with a row for each character inside it indented two
    private static void AddGroups(SectionBinding binding, FieldValue value, AutoGrowFieldList list){
        for(Subsection group : value.GetSubsections()){
            EditableField groupBox = list.AddField(group.GetName(), Shown(group.GetText()), "Notes on " + group.GetName() + "...",
                    subBoxHeight, groupLevel);
            SubsectionBinding groupBinding = new SubsectionBinding(group, groupBox);
            binding.GetGroups().add(groupBinding);

            for(Subsection character : group.GetChildren()){
                EditableField characterBox = list.AddField(character.GetName(), Shown(character.GetText()),
                        "Notes on " + character.GetName() + "...", subBoxHeight, characterLevel);
                groupBinding.GetChildren().add(new SubsectionBinding(character, characterBox));
            }
        }
    }

    // An empty box shows its hint text, which only happens when it starts out null
    private static String Shown(String text){
        return text == null || text.isEmpty() ? null : text;
    }
}
