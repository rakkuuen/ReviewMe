package Controller.BackEnd.SeriesManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import Model.Reviews.FieldStatus;
import Model.Reviews.FieldValue;
import Model.Reviews.Template;
import Model.Reviews.TemplateField;

// What switching one game to another template does to what is on screen. Nothing is ever deleted: a field the new
// template doesn't have stays saved but hidden, and shows again if the game goes back. Only fields that hold something
// are reported, since an empty hidden field costs the user nothing
public class TemplateImpact {
    private List<String> hiddenHeadings;
    private List<String> revealedHeadings;

    public TemplateImpact(List<String> hiddenHeadings, List<String> revealedHeadings){
        this.hiddenHeadings = Collections.unmodifiableList(new ArrayList<>(hiddenHeadings));
        this.revealedHeadings = Collections.unmodifiableList(new ArrayList<>(revealedHeadings));
    }

    // values is everything saved for the game, keyed by field key (including fields hidden by an earlier switch)
    public static TemplateImpact Compute(Template from, Template to, Map<String, FieldValue> values){
        List<String> hidden = new ArrayList<>();
        for(TemplateField field : from.GetFields()){
            if(!Has(to, field.GetFieldKey()) && HoldsContent(values.get(field.GetFieldKey()))){
                hidden.add(field.GetHeading());
            }
        }
        List<String> revealed = new ArrayList<>();
        for(TemplateField field : to.GetFields()){
            if(!Has(from, field.GetFieldKey()) && HoldsContent(values.get(field.GetFieldKey()))){
                revealed.add(field.GetHeading());
            }
        }
        return new TemplateImpact(hidden, revealed);
    }

    // Headings (in the old template's order) of filled fields that disappear from view
    public List<String> GetHiddenHeadings(){ return hiddenHeadings; }

    // Headings (in the new template's order) of saved fields that come back into view
    public List<String> GetRevealedHeadings(){ return revealedHeadings; }

    public int GetHiddenCount(){ return hiddenHeadings.size(); }
    public int GetRevealedCount(){ return revealedHeadings.size(); }
    public boolean HasEffect(){ return !hiddenHeadings.isEmpty() || !revealedHeadings.isEmpty(); }

    private static boolean Has(Template template, String fieldKey){
        for(TemplateField field : template.GetFields()){
            if(field.GetFieldKey().equals(fieldKey)){
                return true;
            }
        }
        return false;
    }

    private static boolean HoldsContent(FieldValue value){
        return value != null && value.GetStatus() == FieldStatus.FILLED && !value.IsMissing();
    }
}
