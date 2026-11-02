package Controller.BackEnd.Overview;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import Controller.BackEnd.SeriesManagement.SeriesDisplayOrder;
import Controller.BackEnd.SeriesManagement.SeriesGameFacts;
import Controller.BackEnd.SeriesManagement.SeriesOrder;
import Controller.BackEnd.SeriesManagement.SeriesStatsCalculator;
import Model.Library.LibraryGame;
import Model.Reviews.GameReview;
import Model.Reviews.Series;

// The first few series for the Overview, most recently played first, each with its games in series order and its numbers
public class OverviewSeriesBuilder {

    public static List<OverviewSeriesGroup> Build(List<Series> series, List<LibraryGame> games, int seriesLimit){
        List<Series> ordered = SeriesDisplayOrder.Sorted(series, games);
        int shown = Math.min(Math.max(seriesLimit, 0), ordered.size());
        List<OverviewSeriesGroup> groups = new ArrayList<>();
        for(Series one : ordered.subList(0, shown)){
            groups.add(GroupOf(one, games));
        }
        return groups;
    }

    // One series with its games in series order and its numbers. games can be any selection of the library: the ones in the series count
    public static OverviewSeriesGroup GroupOf(Series series, List<LibraryGame> games){
        Map<GameReview, LibraryGame> gameByReview = new IdentityHashMap<>();
        List<GameReview> reviews = new ArrayList<>();
        for(LibraryGame game : games){
            if(game.HasReview() && series.GetId().equals(game.GetReview().GetSeriesId())){
                gameByReview.put(game.GetReview(), game);
                reviews.add(game.GetReview());
            }
        }

        List<LibraryGame> inOrder = new ArrayList<>();
        List<SeriesGameFacts> facts = new ArrayList<>();
        for(GameReview review : SeriesOrder.Sorted(reviews)){
            LibraryGame game = gameByReview.get(review);
            inOrder.add(game);
            facts.add(new SeriesGameFacts(review, game.GetStatus(), game.GetSteam()));
        }
        return new OverviewSeriesGroup(series, inOrder, SeriesStatsCalculator.Calculate(series, facts));
    }
}
