package Controller.BackEnd.LibraryView;

import java.util.Collections;
import java.util.List;

import Controller.BackEnd.ListTab.ListChip;
import Controller.BackEnd.Overview.GameRow;

// The Library worked out: the rows to show now, how many games match and how many there are, the chips (state, review and the More
// achievements tag, with counts) and the filters that are on, each with what removes it
public class LibraryResult {

    public enum Emptiness { NOT_EMPTY, NOTHING_IN_LIBRARY, NO_MATCH }

    // A filter that is on: what it says ("State: 100%ed") and its key (the screen hands it back to remove it)
    public static class ActiveFilter {
        private final String key;
        private final String label;

        public ActiveFilter(String key, String label){
            this.key = key;
            this.label = label;
        }

        public String GetKey(){ return key; }
        public String GetLabel(){ return label; }
    }

    private final List<GameRow> rows;
    private final int matching;
    private final int total;
    private final List<ListChip> stateChips;
    private final List<ListChip> reviewChips;
    private final ListChip moreAchievementsChip;
    private final List<ActiveFilter> activeFilters;
    private final List<ListChip> seriesOptions;
    private final List<ListChip> genreOptions;
    private final List<ListChip> platformOptions;

    public LibraryResult(List<GameRow> rows, int matching, int total, List<ListChip> stateChips, List<ListChip> reviewChips, ListChip moreAchievementsChip, List<ActiveFilter> activeFilters,
            List<ListChip> seriesOptions, List<ListChip> genreOptions, List<ListChip> platformOptions){
        this.rows = Collections.unmodifiableList(rows);
        this.matching = matching;
        this.total = total;
        this.stateChips = Collections.unmodifiableList(stateChips);
        this.reviewChips = Collections.unmodifiableList(reviewChips);
        this.moreAchievementsChip = moreAchievementsChip;
        this.activeFilters = Collections.unmodifiableList(activeFilters);
        this.seriesOptions = Collections.unmodifiableList(seriesOptions);
        this.genreOptions = Collections.unmodifiableList(genreOptions);
        this.platformOptions = Collections.unmodifiableList(platformOptions);
    }

    public List<GameRow> GetRows(){ return rows; }
    public int GetMatching(){ return matching; }

    // Every game in the Library, whatever is picked
    public int GetTotal(){ return total; }

    public List<ListChip> GetStateChips(){ return stateChips; }

    // Empty when the picked states leave nothing to choose between (only Not started)
    public List<ListChip> GetReviewChips(){ return reviewChips; }

    // Null when the picked states can't have the tag (it only exists on 100%ed games)
    public ListChip GetMoreAchievementsChip(){ return moreAchievementsChip; }

    public List<ActiveFilter> GetActiveFilters(){ return activeFilters; }

    // What can be picked for each of the other filters, with how many games have it. Each is on when it is picked (key is the series' id,
    // or the genre or platform as written)
    public List<ListChip> GetSeriesOptions(){ return seriesOptions; }
    public List<ListChip> GetGenreOptions(){ return genreOptions; }
    public List<ListChip> GetPlatformOptions(){ return platformOptions; }
    public boolean HasMore(){ return rows.size() < matching; }

    public Emptiness GetEmptiness(){
        if(matching > 0){
            return Emptiness.NOT_EMPTY;
        }
        return total == 0 ? Emptiness.NOTHING_IN_LIBRARY : Emptiness.NO_MATCH;
    }
}
