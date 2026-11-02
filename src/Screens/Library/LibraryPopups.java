package Screens.Library;

import java.util.List;
import java.util.function.Consumer;

import Controller.BackEnd.LibraryView.DateRange;
import Controller.BackEnd.ListTab.ListChip;

// What the Library asks its owner to show when a filter button is pressed: a list of things to tick (series, genres, platforms) or a
// choice of date ranges. The owner draws them however it likes and hands the answer back
public interface LibraryPopups {

    // Shows the options (each with whether it is picked) and calls onToggle with the key of the one the user clicks
    void ShowChoices(String title, List<ListChip> options, Consumer<String> onToggle);

    // Shows the date ranges to choose from and calls onPick with the one chosen (DateRange.None() for any time)
    void ShowRanges(String title, Consumer<DateRange> onPick);

    // Shows the window for adding a game that isn't on Steam
    default void ShowAddGame(){ }

    static LibraryPopups None(){
        return new LibraryPopups(){
            @Override public void ShowChoices(String title, List<ListChip> options, Consumer<String> onToggle){ }
            @Override public void ShowRanges(String title, Consumer<DateRange> onPick){ }
        };
    }
}
