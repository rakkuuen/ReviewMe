package Controller.BackEnd.Overview;

import java.util.Locale;

// How the Overview's numbers are written
public class StatsText {
    private static final int minutesPerHour = 60;
    private static final String noValue = "-";

    // 1842 -> "1,842"
    public static String Count(int number){
        return String.format(Locale.US, "%,d", number);
    }

    // Whole hours, rounded: 193 minutes is "3"
    public static String Hours(int minutes){
        return Count((int) Math.round(minutes / (double) minutesPerHour));
    }

    // One decimal place, or a dash when there is nothing to average
    public static String Average(Double average){
        return average == null ? noValue : String.format(Locale.US, "%.1f", average);
    }

    // 2026 -> "'26"
    public static String YearLabel(int year){
        return "'" + String.format(Locale.US, "%02d", Math.floorMod(year, 100));
    }
}
