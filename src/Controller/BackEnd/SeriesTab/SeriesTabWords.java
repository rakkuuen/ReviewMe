package Controller.BackEnd.SeriesTab;

import Controller.BackEnd.MainView.StateSelection;

// The words on the Series tab, as the screen draws them
public class SeriesTabWords {
    private static final String dot = " " + (char) 0x00B7 + " ";   // A middle dot, by code point so the source stays plain ASCII

    public static final String collapseAll = "Collapse all";
    public static final String expandAll = "Expand all";
    public static final String newSeries = "+ New series";
    public static final String openSeries = "Open series";

    // "4 series . every group is open until you close it"
    public static String Summary(int count){
        return count + " series" + dot + "every group is open until you close it";
    }

    // "Sort: Name"
    public static String SortButton(SeriesSort sort){
        return "Sort: " + sort.GetLabel();
    }

    // What the tab says when it has no series to list
    public static String Empty(StateSelection selection){
        return selection.IsAll() ? "No series yet. Make one with New series." : "No series have games in this state.";
    }
}
