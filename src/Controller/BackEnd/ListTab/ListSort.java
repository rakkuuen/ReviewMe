package Controller.BackEnd.ListTab;

// What a list can be put in order by
public enum ListSort {
    TITLE("Title"),
    RATING("Rating"),
    PLAYTIME("Playtime"),
    LAST_PLAYED("Last played");

    private final String label;

    ListSort(String label){
        this.label = label;
    }

    public String GetLabel(){ return label; }
}
