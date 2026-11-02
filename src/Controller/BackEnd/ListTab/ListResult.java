package Controller.BackEnd.ListTab;

import java.util.Collections;
import java.util.List;

import Controller.BackEnd.Overview.GameRow;

// A list tab worked out: the rows to show now, how many games match, and the chips. Why it is empty, when it is, matters: a tab with
// no games says so, one where the search or chips filtered everything out offers to clear them
public class ListResult {

    public enum Emptiness { NOT_EMPTY, NOTHING_IN_TAB, NO_MATCH }

    private final List<GameRow> rows;
    private final int matching;
    private final int inTab;
    private final List<ListChip> reviewChips;
    private final List<ListChip> reasonChips;

    public ListResult(List<GameRow> rows, int matching, int inTab, List<ListChip> reviewChips, List<ListChip> reasonChips){
        this.rows = Collections.unmodifiableList(rows);
        this.matching = matching;
        this.inTab = inTab;
        this.reviewChips = Collections.unmodifiableList(reviewChips);
        this.reasonChips = Collections.unmodifiableList(reasonChips);
    }

    public List<GameRow> GetRows(){ return rows; }

    // Every game that passes the search and chips, shown or not ("Showing 8 of 14")
    public int GetMatching(){ return matching; }

    // Every game in the tab before the search and chips
    public int GetInTab(){ return inTab; }

    public List<ListChip> GetReviewChips(){ return reviewChips; }
    public List<ListChip> GetReasonChips(){ return reasonChips; }

    public boolean HasMore(){ return rows.size() < matching; }

    public Emptiness GetEmptiness(){
        if(matching > 0){
            return Emptiness.NOT_EMPTY;
        }
        return inTab == 0 ? Emptiness.NOTHING_IN_TAB : Emptiness.NO_MATCH;
    }
}
