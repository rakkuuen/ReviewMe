package Model.Reviews;

// When a game was added to the library and where that date came from. Steam doesn't say, so it's either typed in by hand
// or the day ReviewMe first saw the game in a sync
public class AddedToLibrary {
    public enum Source { MANUAL, FIRST_SEEN, UNKNOWN }

    private String date;   // Like 2025-03-02, null when unknown
    private Source source;

    public AddedToLibrary(String date, Source source){
        this.date = date;
        this.source = source;
    }

    public String GetDate(){ return date; }
    public Source GetSource(){ return source; }
}
