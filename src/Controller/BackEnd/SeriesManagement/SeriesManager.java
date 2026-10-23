package Controller.BackEnd.SeriesManagement;

import java.util.List;

import Database.Dao.SeriesChangeDao;
import Model.Reviews.SeriesChange;

// The series operations the screens call. Each works out a plan first (so nothing half-happens), saves it as one transaction,
// and hands back an undo. The Preview methods give the same plan without saving, for the "N games will switch" confirmations
public class SeriesManager {

    public static SeriesResult Create(String name, String templateId){ return Run(SeriesEditPlanner.Create(name, templateId)); }
    public static SeriesResult Rename(String seriesId, String name){ return Run(SeriesEditPlanner.Rename(seriesId, name)); }
    public static SeriesResult SetNotes(String seriesId, String notes){ return Run(SeriesEditPlanner.SetNotes(seriesId, notes)); }

    public static SeriesResult SetTemplate(String seriesId, String templateId){ return Run(SeriesGamesPlanner.SetTemplate(seriesId, templateId)); }
    public static SeriesResult Assign(String seriesId, List<String> titles){ return Run(SeriesGamesPlanner.Assign(seriesId, titles)); }
    public static SeriesResult Unassign(String title){ return Run(SeriesGamesPlanner.Unassign(title)); }
    public static SeriesResult Delete(String seriesId){ return Run(SeriesGamesPlanner.Delete(seriesId)); }
    public static SeriesResult Reorder(String seriesId, List<String> titles){ return Run(SeriesGamesPlanner.Reorder(seriesId, titles)); }
    public static SeriesResult MoveBy(String seriesId, String title, int delta){ return Run(SeriesGamesPlanner.MoveBy(seriesId, title, delta)); }

    public static SeriesPlan PreviewSetTemplate(String seriesId, String templateId){ return SeriesGamesPlanner.SetTemplate(seriesId, templateId); }
    public static SeriesPlan PreviewAssign(String seriesId, List<String> titles){ return SeriesGamesPlanner.Assign(seriesId, titles); }
    public static SeriesPlan PreviewUnassign(String title){ return SeriesGamesPlanner.Unassign(title); }
    public static SeriesPlan PreviewDelete(String seriesId){ return SeriesGamesPlanner.Delete(seriesId); }

    // Puts back what an earlier result's GetUndo() describes. All or nothing, like everything else
    public static SeriesResult Undo(SeriesChange undo){
        if(undo == null){
            return new SeriesResult(List.of("There is nothing to undo"), null, List.of());
        }
        if(!SeriesChangeDao.Apply(undo)){
            return new SeriesResult(List.of("Couldn't undo, nothing was changed"), null, List.of());
        }
        return new SeriesResult(List.of(), null, List.of());
    }

    private static SeriesResult Run(SeriesPlan plan){
        if(!plan.Succeeded()){
            return new SeriesResult(plan.GetErrors(), null, plan.GetSwitches());
        }
        if(plan.GetChange().IsEmpty()){
            return new SeriesResult(List.of(), null, plan.GetSwitches());
        }
        if(!SeriesChangeDao.Apply(plan.GetChange())){
            return new SeriesResult(List.of("Couldn't save the change, nothing was changed"), null, plan.GetSwitches());
        }
        return new SeriesResult(List.of(), plan.GetUndo(), plan.GetSwitches());
    }
}
