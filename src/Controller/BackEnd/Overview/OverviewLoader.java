package Controller.BackEnd.Overview;

import Controller.BackEnd.MainView.LibraryGameLoader;
import Database.Dao.ManualAchievementDao;
import Database.Dao.SeriesDao;

// Reads what the Overview needs from the database and works it out
public class OverviewLoader {

    public static OverviewData Load(int rowsPerSection, int topPickSlots){
        return OverviewCalculator.Calculate(new OverviewInput(
                LibraryGameLoader.Load(), SeriesDao.GetAllSeries(), ManualAchievementDao.GetUnlockedCounts(), rowsPerSection, topPickSlots));
    }
}
