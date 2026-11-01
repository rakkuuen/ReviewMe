package Controller.BackEnd.SeriesManagement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.Library.LibraryGame;
import Model.Reviews.Series;

// The order series are listed in: the series whose game was played most recently first (Steam's last played time), then the ones
// never played by name. Derived every time, nothing is stored
public class SeriesDisplayOrder {

    public static List<Series> Sorted(List<Series> series, List<LibraryGame> games){
        Map<String, Long> lastPlayed = LastPlayedBySeries(games);
        List<Series> sorted = new ArrayList<>(series);
        sorted.sort(Comparator
                .comparing((Series s) -> lastPlayed.getOrDefault(s.GetId(), 0L), Comparator.reverseOrder())
                .thenComparing((Series s) -> s.GetName().toLowerCase())
                .thenComparing(Series::GetName));
        return sorted;
    }

    // Zero for a series with no game that has ever been played
    private static Map<String, Long> LastPlayedBySeries(List<LibraryGame> games){
        Map<String, Long> latest = new HashMap<>();
        for(LibraryGame game : games){
            if(!game.HasReview() || game.GetReview().GetSeriesId() == null || !game.IsOnSteam() || game.GetSteam().GetLastPlayed() == null){
                continue;
            }
            latest.merge(game.GetReview().GetSeriesId(), game.GetSteam().GetLastPlayed(), Math::max);
        }
        return latest;
    }
}
