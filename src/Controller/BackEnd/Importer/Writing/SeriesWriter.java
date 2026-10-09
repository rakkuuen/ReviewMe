package Controller.BackEnd.Importer.Writing;

import java.util.LinkedHashMap;
import java.util.Map;

import Controller.BackEnd.Importer.Results.ImportResult;
import Controller.BackEnd.Importer.Results.MappedReview;
import Controller.BackEnd.Importer.Results.SeriesNote;
import Database.Dao.SeriesDao;
import Model.Reviews.Series;

// Stores each series found on the reviews or in a rules file. The id is the tag, the name is the tag spaced out
public class SeriesWriter {
    private static final String standardTemplateId = "Standard";

    public static void Write(ImportResult result, WriteResult written){
        for(Map.Entry<String, String> entry : TemplateBySeries(result).entrySet()){
            Series series = new Series(entry.getKey(), DisplayName(entry.getKey()), entry.getValue(), NotesFor(result, entry.getKey()));
            if(SeriesDao.InsertSeries(series)){
                written.CountSeries();
            } else {
                written.Fail(entry.getKey(), "couldn't save the series");
            }
        }
    }

    // How many series the import contains
    public static int CountSeries(ImportResult result){
        return TemplateBySeries(result).size();
    }

    // Series id -> the template its games use. A series with only a rules file gets the template of the same name, else Standard
    private static Map<String, String> TemplateBySeries(ImportResult result){
        Map<String, String> templateBySeries = new LinkedHashMap<>();
        for(MappedReview mapped : result.GetReviews()){
            String seriesId = mapped.GetReview().GetSeriesId();
            if(seriesId != null){
                templateBySeries.putIfAbsent(seriesId, mapped.GetReview().GetTemplateId());
            }
        }
        for(SeriesNote note : result.GetSeriesNotes()){
            String fallback = result.GetTemplates().containsKey(note.GetSeriesId()) ? note.GetSeriesId() : standardTemplateId;
            templateBySeries.putIfAbsent(note.GetSeriesId(), fallback);
        }
        return templateBySeries;
    }

    // HouseFlipper -> House Flipper
    public static String DisplayName(String seriesId){
        return seriesId.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ");
    }

    private static String NotesFor(ImportResult result, String seriesId){
        for(SeriesNote note : result.GetSeriesNotes()){
            if(note.GetSeriesId().equalsIgnoreCase(seriesId)){
                return note.GetText();
            }
        }
        return null;
    }
}
