package Controller.BackEnd.SeriesManagement;

import java.util.List;

// One game changing template as part of a series operation, and what that does to what it shows
public class TemplateSwitch {
    private String title, fromTemplateId, toTemplateId;
    private TemplateImpact impact;

    public TemplateSwitch(String title, String fromTemplateId, String toTemplateId, TemplateImpact impact){
        this.title = title;
        this.fromTemplateId = fromTemplateId;
        this.toTemplateId = toTemplateId;
        this.impact = impact;
    }

    public String GetTitle(){ return title; }
    public String GetFromTemplateId(){ return fromTemplateId; }
    public String GetToTemplateId(){ return toTemplateId; }
    public TemplateImpact GetImpact(){ return impact; }

    // Filled fields that go out of view across all the switches (the number in "N fields stay saved but hidden")
    public static int TotalHidden(List<TemplateSwitch> switches){
        int total = 0;
        for(TemplateSwitch change : switches){
            total += change.GetImpact().GetHiddenCount();
        }
        return total;
    }

    public static int TotalRevealed(List<TemplateSwitch> switches){
        int total = 0;
        for(TemplateSwitch change : switches){
            total += change.GetImpact().GetRevealedCount();
        }
        return total;
    }
}
