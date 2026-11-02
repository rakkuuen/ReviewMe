package Controller.BackEnd.LibraryView;

import java.time.LocalDate;

// A span of days for the Library's date filters, written like 2025-03-02. Either end can be left open. Immutable
public class DateRange {
    private final String from;   // Null for no start
    private final String to;     // Null for no end

    public DateRange(String from, String to){
        this.from = from;
        this.to = to;
    }

    public static DateRange None(){ return new DateRange(null, null); }

    // The last 30 days up to and including today
    public static DateRange Last30Days(LocalDate today){
        return new DateRange(today.minusDays(29).toString(), today.toString());
    }

    public static DateRange ThisYear(LocalDate today){
        return new DateRange(LocalDate.of(today.getYear(), 1, 1).toString(), LocalDate.of(today.getYear(), 12, 31).toString());
    }

    public static DateRange LastYear(LocalDate today){
        return new DateRange(LocalDate.of(today.getYear() - 1, 1, 1).toString(), LocalDate.of(today.getYear() - 1, 12, 31).toString());
    }

    public String GetFrom(){ return from; }
    public String GetTo(){ return to; }
    public boolean IsSet(){ return from != null || to != null; }

    // Is the day (like 2025-03-02) inside the span, ends included? A game with no date is never inside a span that is set
    public boolean Contains(String date){
        if(!IsSet()){
            return true;
        }
        if(date == null || date.isEmpty()){
            return false;
        }
        return (from == null || date.compareTo(from) >= 0) && (to == null || date.compareTo(to) <= 0);
    }

    // "2025-01-01 to 2025-12-31", "from 2025-01-01" or "up to 2025-12-31"
    public String Describe(){
        if(from != null && to != null){
            return from + " to " + to;
        }
        return from != null ? "from " + from : "up to " + to;
    }
}
