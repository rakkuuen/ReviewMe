package Controller.BackEnd.Overview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// The bars for the completed-per-year chart: the last few years ending at the most recent year with a completion, oldest first, with
// years in between that had none shown as empty bars. No completions at all means no bars
public class YearBars {

    public static List<YearBar> Build(Map<Integer, Integer> completedByYear, int barCount){
        List<YearBar> bars = new ArrayList<>();
        if(completedByYear.isEmpty() || barCount < 1){
            return bars;
        }
        int last = new TreeMap<>(completedByYear).lastKey();
        int first = last - barCount + 1;

        int tallest = 0;
        for(int year = first; year <= last; year++){
            tallest = Math.max(tallest, completedByYear.getOrDefault(year, 0));
        }
        for(int year = first; year <= last; year++){
            int count = completedByYear.getOrDefault(year, 0);
            bars.add(new YearBar(year, StatsText.YearLabel(year), count, tallest == 0 ? 0 : count / (double) tallest));
        }
        return bars;
    }
}
