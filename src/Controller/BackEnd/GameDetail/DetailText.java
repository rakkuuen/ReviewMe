package Controller.BackEnd.GameDetail;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

// How the page writes its numbers and dates
public class DetailText {
    private static final String noValue = "-";
    private static final int minutesPerHour = 60;
    private static final DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    // 2025-03-02 -> "02 Mar 2025"; a dash when there is no date, and the text as it is if it isn't a real date
    public static String Date(String isoDate){
        if(isoDate == null || isoDate.trim().isEmpty()){
            return noValue;
        }
        try {
            return LocalDate.parse(isoDate.trim()).format(dayFormat);
        } catch (DateTimeParseException e) {
            return isoDate.trim();
        }
    }

    // Epoch seconds -> "14 Sep 2026" (in UTC); a dash for never played
    public static String DateOfEpoch(Long epochSeconds){
        if(epochSeconds == null || epochSeconds <= 0){
            return noValue;
        }
        return Instant.ofEpochSecond(epochSeconds).atZone(ZoneOffset.UTC).toLocalDate().format(dayFormat);
    }

    // Whole hours, rounded: "214 h"
    public static String Hours(int minutes){
        return Math.round(minutes / (double) minutesPerHour) + " h";
    }
}
