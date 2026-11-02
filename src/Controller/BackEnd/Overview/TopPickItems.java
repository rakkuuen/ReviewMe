package Controller.BackEnd.Overview;

import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.Reviews.GameNumbers;
import Model.Library.LibraryGame;

// The Top picks panel's slots from the Overview's picks (one entry per slot, null for an empty one), and its "4 of 5 slots" line
public class TopPickItems {

    // Slot numbers start at 1
    public static List<TopPickItem> Build(List<LibraryGame> picks){
        List<TopPickItem> items = new ArrayList<>();
        for(int i = 0; i < picks.size(); i++){
            LibraryGame game = picks.get(i);
            items.add(game == null ? new TopPickItem(i + 1, null, null) : new TopPickItem(i + 1, game.GetTitle(), GameNumbers.RatingText(game.GetReview())));
        }
        return items;
    }

    public static String Summary(List<TopPickItem> items){
        int filled = 0;
        for(TopPickItem item : items){
            filled += item.IsEmpty() ? 0 : 1;
        }
        return filled + " of " + items.size() + " slots";
    }
}
