package Controller.BackEnd.ListTab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Controller.BackEnd.MainView.MainTab;

// What each list tab's toolbar offers and how it starts out. The screens only ask
public class ListRules {

    // The tabs that are a list of games (the Overview and Series are something else)
    public static boolean IsListTab(MainTab tab){
        return tab == MainTab.COMPLETED || tab == MainTab.INCOMPLETE || tab == MainTab.MORE_ACHIEVEMENTS || tab == MainTab.GAMES;
    }

    // The orders a tab lets you pick from. Incomplete and More achievements are short worklists, kept by title
    public static List<ListSort> SortsFor(MainTab tab){
        switch(tab){
            case COMPLETED: return Arrays.asList(ListSort.RATING, ListSort.TITLE, ListSort.PLAYTIME);
            case GAMES: return Arrays.asList(ListSort.LAST_PLAYED, ListSort.TITLE, ListSort.PLAYTIME);
            default: return new ArrayList<>();
        }
    }

    // The order after the current one in the tab's list, going round. The current one again if the tab has no choice
    public static ListSort NextSort(MainTab tab, ListSort current){
        List<ListSort> choices = SortsFor(tab);
        int at = choices.indexOf(current);
        return choices.isEmpty() ? current : choices.get((at + 1) % choices.size());
    }

    // The order a tab opens in
    public static ListSort DefaultSort(MainTab tab){
        List<ListSort> choices = SortsFor(tab);
        return choices.isEmpty() ? ListSort.TITLE : choices.get(0);
    }

    // Titles run A to Z, everything else biggest or latest first
    public static boolean DefaultDescending(ListSort sort){
        return sort != ListSort.TITLE;
    }

    // Only the Incomplete tab lists the reasons games are there
    public static boolean HasReasonChips(MainTab tab){
        return tab == MainTab.INCOMPLETE;
    }
}
