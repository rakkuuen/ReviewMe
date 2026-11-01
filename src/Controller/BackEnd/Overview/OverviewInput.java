package Controller.BackEnd.Overview;

import java.util.List;
import java.util.Map;

import Model.Library.LibraryGame;
import Model.Reviews.Series;

// Everything the Overview is worked out from, already read from the database. The two sizes come from the layout settings
public class OverviewInput {
    private List<LibraryGame> games;
    private List<Series> series;
    private Map<Integer, Integer> manualUnlockedByReviewId;
    private int rowsPerSection;
    private int topPickSlots;

    public OverviewInput(List<LibraryGame> games, List<Series> series, Map<Integer, Integer> manualUnlockedByReviewId, int rowsPerSection, int topPickSlots){
        this.games = games;
        this.series = series;
        this.manualUnlockedByReviewId = manualUnlockedByReviewId;
        this.rowsPerSection = rowsPerSection;
        this.topPickSlots = topPickSlots;
    }

    public List<LibraryGame> GetGames(){ return games; }
    public List<Series> GetSeries(){ return series; }
    public Map<Integer, Integer> GetManualUnlockedByReviewId(){ return manualUnlockedByReviewId; }
    public int GetRowsPerSection(){ return rowsPerSection; }
    public int GetTopPickSlots(){ return topPickSlots; }
}
