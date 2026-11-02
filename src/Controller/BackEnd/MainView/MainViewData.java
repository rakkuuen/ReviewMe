package Controller.BackEnd.MainView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Controller.BackEnd.Overview.OverviewData;
import Model.Library.LibraryGame;
import Model.Reviews.Series;

// Everything the main view is drawn from, read once: every game, every series, and the worked-out Overview
public class MainViewData {
    private final List<LibraryGame> games;
    private final List<String> seriesIds;
    private final List<Series> series;
    private final OverviewData overview;

    // For data that knows only the series' ids (the Series tab then has nothing to list)
    public MainViewData(List<LibraryGame> games, List<String> seriesIds, OverviewData overview){
        this(games, seriesIds, new ArrayList<>(), overview);
    }

    private MainViewData(List<LibraryGame> games, List<String> seriesIds, List<Series> series, OverviewData overview){
        this.games = Collections.unmodifiableList(games);
        this.seriesIds = Collections.unmodifiableList(seriesIds);
        this.series = Collections.unmodifiableList(series);
        this.overview = overview;
    }

    public static MainViewData WithSeries(List<LibraryGame> games, List<Series> series, OverviewData overview){
        List<String> ids = new ArrayList<>();
        for(Series one : series){
            ids.add(one.GetId());
        }
        return new MainViewData(games, ids, series, overview);
    }

    public List<LibraryGame> GetGames(){ return games; }
    public List<String> GetSeriesIds(){ return seriesIds; }
    public List<Series> GetSeries(){ return series; }
    public OverviewData GetOverview(){ return overview; }
}
