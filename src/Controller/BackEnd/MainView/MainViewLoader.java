package Controller.BackEnd.MainView;

import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.Overview.OverviewLoader;
import Database.Dao.SeriesDao;
import Model.Library.LibraryGame;
import Model.Reviews.Series;

// Reads the database once for the main view. rowsPerSection and topPickSlots come from the layout settings
public class MainViewLoader {

    public static MainViewData Load(int rowsPerSection, int topPickSlots){
        List<LibraryGame> games = LibraryGameLoader.Load();
        List<String> seriesIds = new ArrayList<>();
        for(Series series : SeriesDao.GetAllSeries()){
            seriesIds.add(series.GetId());
        }
        return new MainViewData(games, seriesIds, OverviewLoader.Load(games, rowsPerSection, topPickSlots));
    }
}
