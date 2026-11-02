package Controller.BackEnd.MainView;

import java.util.List;

import Controller.BackEnd.Overview.OverviewLoader;
import Database.Dao.SeriesDao;
import Model.Library.LibraryGame;
import Model.Reviews.Series;

// Reads the database once for the main view. rowsPerSection and topPickSlots come from the layout settings
public class MainViewLoader {

    public static MainViewData Load(int rowsPerSection, int topPickSlots){
        List<LibraryGame> games = LibraryGameLoader.Load();
        List<Series> series = SeriesDao.GetAllSeries();
        return MainViewData.WithSeries(games, series, OverviewLoader.Load(games, rowsPerSection, topPickSlots));
    }
}
