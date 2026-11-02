package Screens.MainView;

import java.util.function.Consumer;

// What the Series tab hands back to whoever owns the screen: open one series (by id), or start a new one
public class SeriesActions {
    private final Consumer<String> openSeries;
    private final Runnable newSeries;

    public SeriesActions(Consumer<String> openSeries, Runnable newSeries){
        this.openSeries = openSeries;
        this.newSeries = newSeries;
    }

    public static SeriesActions None(){
        return new SeriesActions(id -> { }, () -> { });
    }

    public void OpenSeries(String seriesId){ openSeries.accept(seriesId); }
    public void NewSeries(){ newSeries.run(); }
}
