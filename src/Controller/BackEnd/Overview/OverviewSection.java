package Controller.BackEnd.Overview;

import java.util.Collections;
import java.util.List;

import Model.Library.LibraryGame;

// One short list on the Overview: the first few rows, and how many games the full list holds ("View all 34")
public class OverviewSection {
    private final List<LibraryGame> rows;
    private final int total;

    public OverviewSection(List<LibraryGame> rows, int total){
        this.rows = Collections.unmodifiableList(rows);
        this.total = total;
    }

    public List<LibraryGame> GetRows(){ return rows; }
    public int GetTotal(){ return total; }
    public boolean HasMore(){ return total > rows.size(); }
}
