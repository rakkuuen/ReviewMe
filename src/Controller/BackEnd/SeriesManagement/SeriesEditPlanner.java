package Controller.BackEnd.SeriesManagement;

import java.util.List;
import java.util.Objects;

import Database.Dao.SeriesDao;
import Database.Dao.TemplateDao;
import Model.Reviews.Series;

// Plans the operations on a series itself (not its games): create, rename, notes. Reads the database, writes nothing
public class SeriesEditPlanner {

    // The tag comes from the name and is the series' id from then on
    public static SeriesPlan Create(String name, String templateId){
        List<String> errors = SeriesChecker.CheckNew(name, SeriesDao.GetAllSeries());
        if(TemplateDao.GetTemplate(templateId) == null){
            errors.add("There is no template called " + templateId);
        }
        if(!errors.isEmpty()){
            return SeriesPlan.Refused(errors);
        }
        String cleaned = SeriesChecker.Clean(name);
        Series created = new Series(SeriesTag.FromName(cleaned), cleaned, templateId, null);
        SeriesPlan plan = new SeriesPlan();
        plan.GetChange().SetToInsert(created);
        plan.GetUndo().SetIdToDelete(created.GetId());
        return plan;
    }

    // Only the display name changes; the tag stays
    public static SeriesPlan Rename(String seriesId, String name){
        Series series = SeriesDao.GetSeries(seriesId);
        if(series == null){
            return SeriesPlan.Refused(SeriesGamesPlanner.NoSeries(seriesId));
        }
        List<String> errors = SeriesChecker.CheckRename(name, SeriesDao.GetAllSeries(), seriesId);
        if(!errors.isEmpty()){
            return SeriesPlan.Refused(errors);
        }
        Series renamed = series.Copy();
        renamed.SetName(SeriesChecker.Clean(name));
        return Replacing(series, renamed);
    }

    // Notes are kept as typed; blank means none
    public static SeriesPlan SetNotes(String seriesId, String notes){
        Series series = SeriesDao.GetSeries(seriesId);
        if(series == null){
            return SeriesPlan.Refused(SeriesGamesPlanner.NoSeries(seriesId));
        }
        Series edited = series.Copy();
        edited.SetNotes(notes == null || notes.trim().isEmpty() ? null : notes);
        return Replacing(series, edited);
    }

    // Nothing to write if nothing is different
    private static SeriesPlan Replacing(Series original, Series edited){
        SeriesPlan plan = new SeriesPlan();
        if(!Same(original, edited)){
            plan.GetChange().SetToUpdate(edited);
            plan.GetUndo().SetToUpdate(original);
        }
        return plan;
    }

    private static boolean Same(Series a, Series b){
        return Objects.equals(a.GetName(), b.GetName()) && Objects.equals(a.GetNotes(), b.GetNotes())
                && Objects.equals(a.GetTemplateId(), b.GetTemplateId());
    }
}
