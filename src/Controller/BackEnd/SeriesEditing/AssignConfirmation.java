package Controller.BackEnd.SeriesEditing;

import java.util.List;

import Controller.BackEnd.SeriesManagement.SeriesManager;
import Controller.BackEnd.SeriesManagement.SeriesPlan;

// What to ask before games are assigned: how many switch template, and which of them leave another series. Null when nothing needs asking
public class AssignConfirmation {

    public static String Text(String seriesId, String templateName, List<String> titles){
        SeriesPlan plan = SeriesManager.PreviewAssign(seriesId, titles);
        if(!plan.Succeeded() || plan.GetSwitches().isEmpty()){
            return null;
        }
        int switching = plan.GetSwitches().size();
        return switching + (switching == 1 ? " game" : " games") + " will switch to the " + templateName + " template. Fields not in that template stay saved but are hidden.";
    }
}
