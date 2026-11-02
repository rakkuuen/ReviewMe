package Controller.BackEnd.SeriesTab;

// The orders the Series tab can list series in
public enum SeriesSort {
    NAME("Name"),
    RECENT("Recently played"),
    GAMES("Most games");

    private final String label;

    SeriesSort(String label){
        this.label = label;
    }

    public String GetLabel(){ return label; }

    // The order after this one, going round
    public SeriesSort Next(){
        SeriesSort[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
