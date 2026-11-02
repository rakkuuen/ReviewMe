package Controller.BackEnd.ListTab;

import Controller.BackEnd.MainView.MainTab;
import Controller.BackEnd.MainView.StateSelection;
import Controller.BackEnd.Overview.OverviewSectionInfo;

// The words around a list: its status line, button labels and what it says when it is empty. The screen draws them as they are
public class ListWords {
    private static final String dot = " " + (char) 0x00B7 + " ";   // A middle dot, by code point so the source stays plain ASCII
    private static final char openQuote = (char) 0x201C;
    private static final char closeQuote = (char) 0x201D;

    public static final String searchPlaceholder = "Search titles";
    public static final String reviewCaption = "Review";
    public static final String reasonCaption = "Reason";
    public static final String clearFilters = "Clear filters";
    public static final String showMore = "Show more";
    public static final String noMatchTitle = "No games match";
    public static final String menuButton = "...";

    // "Showing 8 of 34 . sorted by rating, high to low" (the sorted part only for tabs that can be sorted)
    public static String Status(ListResult result, ListQuery query, MainTab tab){
        String text = "Showing " + result.GetRows().size() + " of " + result.GetMatching();
        if(!ListRules.SortsFor(tab).isEmpty()){
            text += dot + "sorted by " + query.GetSort().GetLabel().toLowerCase() + ", " + query.GetSort().Direction(query.IsDescending());
        }
        return text;
    }

    // "Sort: Rating"
    public static String SortButton(ListQuery query){
        return "Sort: " + query.GetSort().GetLabel();
    }

    // "high to low"
    public static String DirectionButton(ListQuery query){
        return query.GetSort().Direction(query.IsDescending());
    }

    // What an empty list says as its heading: the tab's usual "nothing here" words, or that nothing matched
    public static String EmptyTitle(ListResult.Emptiness emptiness, MainTab tab, StateSelection selection){
        if(emptiness == ListResult.Emptiness.NO_MATCH){
            return noMatchTitle;
        }
        return OverviewSectionInfo.EmptyText(ListBuilder.KindOf(tab, selection));
    }

    // The line under it. Only a search that found nothing has more to say
    public static String EmptyDetail(ListResult.Emptiness emptiness, ListQuery query){
        if(emptiness != ListResult.Emptiness.NO_MATCH){
            return "";
        }
        if(query.GetSearch().isEmpty()){
            return "Nothing here matches those filters. Try different ones, or clear them.";
        }
        return "Nothing here matches " + openQuote + query.GetSearch() + closeQuote + ". Try a different title, or clear the filters.";
    }
}
