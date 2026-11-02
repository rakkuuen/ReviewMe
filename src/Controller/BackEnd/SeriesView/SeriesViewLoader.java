package Controller.BackEnd.SeriesView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Controller.BackEnd.MainView.LibraryGameLoader;
import Database.Dao.SeriesDao;
import Database.Dao.TemplateDao;
import Model.Library.LibraryGame;
import Model.Reviews.Series;
import Model.Reviews.Template;

// Reads the database for one series' page. Null when there is no such series (it was deleted, say)
public class SeriesViewLoader {

    public static SeriesViewData Load(String seriesId){
        Series series = SeriesDao.GetSeries(seriesId);
        if(series == null){
            return null;
        }
        List<LibraryGame> games = LibraryGameLoader.Load();
        Map<String, String> names = new HashMap<>();
        for(Template template : TemplateDao.GetAllTemplates()){
            names.put(template.GetId(), template.GetName());
        }
        return SeriesViewBuilder.Build(series, games, names);
    }
}
