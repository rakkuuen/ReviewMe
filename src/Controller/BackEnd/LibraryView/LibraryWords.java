package Controller.BackEnd.LibraryView;

// The words on the Library screen
public class LibraryWords {
    private static final String dot = " " + (char) 0x00B7 + " ";   // A middle dot, by code point so the source stays plain ASCII

    public static final String title = "Library";
    public static final String subtitle = "Owned games";
    public static final String description = "Every game in ReviewMe and on Steam, no duplicates";
    public static final String searchPlaceholder = "Search titles";
    public static final String stateCaption = "State";
    public static final String reviewCaption = "Review";
    public static final String filtersCaption = "Filters";
    public static final String moreFiltersCaption = "Filter by";
    public static final String seriesFilter = "Series";
    public static final String genreFilter = "Genre";
    public static final String platformFilter = "Platform";
    public static final String completedFilter = "Completed on";
    public static final String addedFilter = "Added";
    public static final String last30Days = "Last 30 days";
    public static final String thisYear = "This year";
    public static final String lastYear = "Last year";
    public static final String anyTime = "Any time";
    public static final String clearAll = "Clear all";
    public static final String addGame = "+ Add game manually";
    public static final String noMatchTitle = "No games match";
    public static final String noMatchHint = "Nothing in your library matches these filters. Remove one, or clear them all.";
    public static final String clearAllFilters = "Clear all filters";
    public static final String emptyLibrary = "There are no games in your library yet. Sync with Steam to bring them in.";

    // "Showing 8 of 48 games . sorted by title, A to Z"
    public static String Status(LibraryResult result, LibraryQuery query){
        return "Showing " + result.GetRows().size() + " of " + result.GetMatching() + (result.GetMatching() == 1 ? " game" : " games")
                + dot + "sorted by " + query.GetSort().GetLabel().toLowerCase() + ", " + query.GetSort().Direction(query.IsDescending());
    }

    // "Sort: Title"
    public static String SortButton(LibraryQuery query){
        return "Sort: " + query.GetSort().GetLabel();
    }

    // "A to Z"
    public static String DirectionButton(LibraryQuery query){
        return query.GetSort().Direction(query.IsDescending());
    }
}
