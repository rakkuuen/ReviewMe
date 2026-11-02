package Controller.BackEnd.SeriesEditing;

import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.SeriesManagement.SeriesManager;
import Controller.BackEnd.SeriesManagement.SeriesPlan;
import Controller.BackEnd.SeriesManagement.SeriesResult;

// Saves an Edit series form through SeriesManager, one change at a time in a safe order: name, notes, games taken out, template, then
// the order. Each change is its own transaction, so if one is refused the earlier ones stay saved and the rest are not tried
public class EditSeriesSaver {

    // The first thing that went wrong, as a one-item list, or an empty list when everything saved
    public static List<String> Save(EditSeriesForm form){
        String id = form.GetSeriesId();
        List<SeriesResult> steps = new ArrayList<>();
        if(form.NameChanged()){
            if(!Run(SeriesManager.Rename(id, form.GetName()), steps)){
                return Errors(steps);
            }
        }
        if(form.NotesChanged() && !Run(SeriesManager.SetNotes(id, form.GetNotes()), steps)){
            return Errors(steps);
        }
        for(String title : form.GetRemoved()){
            if(!Run(SeriesManager.Unassign(title), steps)){
                return Errors(steps);
            }
        }
        if(form.TemplateChanged() && !Run(SeriesManager.SetTemplate(id, form.GetTemplateId()), steps)){
            return Errors(steps);
        }
        if(form.OrderChanged() && !Run(SeriesManager.Reorder(id, form.GetTitles()), steps)){
            return Errors(steps);
        }
        return new ArrayList<>();
    }

    // What to ask before saving, or null when nothing needs asking: games that will switch template and games leaving the series
    public static String Confirmation(EditSeriesForm form){
        List<String> lines = new ArrayList<>();
        if(form.TemplateChanged()){
            SeriesPlan plan = SeriesManager.PreviewSetTemplate(form.GetSeriesId(), form.GetTemplateId());
            int switching = plan.GetSwitches().size();
            if(plan.Succeeded() && switching > 0){
                lines.add(switching + (switching == 1 ? " game" : " games") + " will switch to the " + form.GetTemplates().get(form.GetTemplateId()) + " template. Fields not in that template stay saved but are hidden.");
            }
        }
        if(!form.GetRemoved().isEmpty()){
            lines.add("Leaving the series (their reviews are kept, and they go back to the Standard template): " + String.join(", ", form.GetRemoved()) + ".");
        }
        return lines.isEmpty() ? null : String.join("\n\n", lines);
    }

    private static boolean Run(SeriesResult result, List<SeriesResult> steps){
        steps.add(result);
        return result.Succeeded();
    }

    private static List<String> Errors(List<SeriesResult> steps){
        List<String> errors = new ArrayList<>();
        SeriesResult last = steps.get(steps.size() - 1);
        errors.add(String.join("; ", last.GetErrors()));
        return errors;
    }
}
