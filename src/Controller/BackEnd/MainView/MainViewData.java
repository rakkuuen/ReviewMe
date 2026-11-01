package Controller.BackEnd.MainView;

import java.util.Collections;
import java.util.List;

import Controller.BackEnd.Overview.OverviewData;
import Model.Library.LibraryGame;

// Everything the main view is drawn from, read once: every game, every series' id, and the worked-out Overview
public class MainViewData {
    private final List<LibraryGame> games;
    private final List<String> seriesIds;
    private final OverviewData overview;

    public MainViewData(List<LibraryGame> games, List<String> seriesIds, OverviewData overview){
        this.games = Collections.unmodifiableList(games);
        this.seriesIds = Collections.unmodifiableList(seriesIds);
        this.overview = overview;
    }

    public List<LibraryGame> GetGames(){ return games; }
    public List<String> GetSeriesIds(){ return seriesIds; }
    public OverviewData GetOverview(){ return overview; }
}
