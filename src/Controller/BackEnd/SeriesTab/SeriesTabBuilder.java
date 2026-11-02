package Controller.BackEnd.SeriesTab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import Controller.BackEnd.MainView.StateSelection;
import Controller.BackEnd.Overview.OverviewSeriesBuilder;
import Controller.BackEnd.Overview.OverviewSeriesGroup;
import Controller.BackEnd.SeriesManagement.SeriesDisplayOrder;
import Model.Library.LibraryGame;
import Model.Reviews.Series;

// Works out the Series tab: every series with its games in order and its numbers. With All picked every series is there, empty ones
// too; with a state picked only the series that have a game in that state, listing (and counting) just those games. No drawing
public class SeriesTabBuilder {

    public static List<OverviewSeriesGroup> Build(List<LibraryGame> games, List<Series> series, StateSelection selection, SeriesSort sort){
        List<LibraryGame> inScope = new ArrayList<>();
        for(LibraryGame game : games){
            if(selection.Includes(game.GetStatus().GetGameState())){
                inScope.add(game);
            }
        }
        List<Series> ordered = Ordered(series, inScope, sort);
        List<OverviewSeriesGroup> groups = new ArrayList<>();
        for(Series one : ordered){
            OverviewSeriesGroup group = OverviewSeriesBuilder.GroupOf(one, inScope);
            if(selection.IsAll() || !group.GetGames().isEmpty()){
                groups.add(group);
            }
        }
        if(sort == SeriesSort.GAMES){
            groups.sort(Comparator.comparing((OverviewSeriesGroup group) -> group.GetStats().GetGameCount()).reversed());
        }
        return groups;
    }

    // Name order is by name alone; recently played is the Overview's order; most games starts from name order so ties read A to Z
    private static List<Series> Ordered(List<Series> series, List<LibraryGame> inScope, SeriesSort sort){
        if(sort == SeriesSort.RECENT){
            return SeriesDisplayOrder.Sorted(series, inScope);
        }
        List<Series> byName = new ArrayList<>(series);
        byName.sort(Comparator.comparing((Series one) -> one.GetName().toLowerCase()).thenComparing(Series::GetName));
        return byName;
    }
}
