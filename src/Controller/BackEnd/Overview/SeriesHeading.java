package Controller.BackEnd.Overview;

import Controller.BackEnd.SeriesManagement.SeriesStats;

// The line on a series' heading in the Overview: its name, then how many games, how many are done, and the average rating
public class SeriesHeading {
    private static final String dot = " " + (char) 0x00B7 + " ";   // A middle dot, by code point so the source stays plain ASCII

    public static String Name(OverviewSeriesGroup group){
        return group.GetSeries().GetName();
    }

    // "5 games - 3 done - avg 9.2" (the average is left out when none of its games is rated)
    public static String Summary(OverviewSeriesGroup group){
        SeriesStats stats = group.GetStats();
        StringBuilder text = new StringBuilder(stats.GetGameCount() + (stats.GetGameCount() == 1 ? " game" : " games"));
        text.append(dot).append(stats.GetCompletedCount()).append(" done");
        if(stats.GetAverageRating() != null){
            text.append(dot).append("avg ").append(StatsText.Average(stats.GetAverageRating()));
        }
        return text.toString();
    }
}
