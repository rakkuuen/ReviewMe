package Controller.BackEnd.Overview;

import java.util.Collections;
import java.util.List;

import Controller.BackEnd.SeriesManagement.SeriesStats;
import Model.Library.LibraryGame;
import Model.Reviews.Series;

// One series on the Overview with its games in series order and its numbers
public class OverviewSeriesGroup {
    private final Series series;
    private final List<LibraryGame> games;
    private final SeriesStats stats;

    public OverviewSeriesGroup(Series series, List<LibraryGame> games, SeriesStats stats){
        this.series = series;
        this.games = Collections.unmodifiableList(games);
        this.stats = stats;
    }

    public Series GetSeries(){ return series; }
    public List<LibraryGame> GetGames(){ return games; }
    public SeriesStats GetStats(){ return stats; }
}
