package Controller.BackEnd.ListTab;

// What a list can be put in order by
public enum ListSort {
    TITLE("Title", "A to Z", "Z to A"),
    RATING("Rating", "low to high", "high to low"),
    PLAYTIME("Playtime", "shortest first", "longest first"),
    LAST_PLAYED("Last played", "oldest first", "latest first");

    private final String label;
    private final String ascendingWords;
    private final String descendingWords;

    ListSort(String label, String ascendingWords, String descendingWords){
        this.label = label;
        this.ascendingWords = ascendingWords;
        this.descendingWords = descendingWords;
    }

    public String GetLabel(){ return label; }

    // Which way round it runs, in words that suit it ("high to low", "A to Z")
    public String Direction(boolean descending){
        return descending ? descendingWords : ascendingWords;
    }
}
