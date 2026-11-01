package Controller.BackEnd.Overview;

import java.util.List;

import Controller.BackEnd.MainView.LibraryGameLoader;
import Database.Dao.ManualAchievementDao;
import Database.Dao.SeriesDao;
import Model.Library.LibraryGame;

// Reads what the Overview needs from the database and works it out
public class OverviewLoader {

    public static OverviewData Load(int rowsPerSection, int topPickSlots){
        return Load(LibraryGameLoader.Load(), rowsPerSection, topPickSlots);
    }

    // For when the games have been read already
    public static OverviewData Load(List<LibraryGame> games, int rowsPerSection, int topPickSlots){
        return OverviewCalculator.Calculate(new OverviewInput(
                games, SeriesDao.GetAllSeries(), ManualAchievementDao.GetUnlockedCounts(), rowsPerSection, topPickSlots));
    }
}
