package Controller.BackEnd.ListTab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Controller.BackEnd.Library.UnreviewedGameRules;
import Model.Library.LibraryGame;
import Model.Reviews.GameState;

// What a list row offers for a game. A game with a review is simply opened by clicking its row. One without gets buttons for the
// thing it needs most, and a menu for the rest. Shelved overrides everything, so a shelved game with no review can only be
// unshelved or have its reason edited
public class RowActions {

    // The buttons drawn on the row, in order
    public static List<RowAction> Buttons(LibraryGame game){
        List<RowAction> buttons = new ArrayList<>();
        if(game.HasReview()){
            return buttons;
        }
        buttons.add(RowAction.WRITE_REVIEW);
        if(IsShelved(game)){
            buttons.add(RowAction.UNSHELVE);
        }
        return buttons;
    }

    // What the row's menu button lists; empty means the row has no menu button
    public static List<RowAction> MenuItems(LibraryGame game){
        if(game.HasReview()){
            return new ArrayList<>();
        }
        if(IsShelved(game)){
            return Arrays.asList(RowAction.EDIT_REASON);
        }
        List<RowAction> items = new ArrayList<>(Arrays.asList(RowAction.WRITE_REVIEW, RowAction.SHELVE));
        if(CanMarkCompleted(game)){
            items.add(RowAction.MARK_COMPLETED);
        }
        return items;
    }

    // Only a played game that Steam has no achievements for can be completed by hand (the rest complete themselves at 100%)
    private static boolean CanMarkCompleted(LibraryGame game){
        return game.GetStatus().GetGameState() != GameState.COMPLETED && UnreviewedGameRules.CompletionProblem(game.GetSteam()) == null;
    }

    private static boolean IsShelved(LibraryGame game){
        return game.GetStatus().GetGameState() == GameState.SHELVED;
    }
}
