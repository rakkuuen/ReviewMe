package Controller.BackEnd.MainView;

import Model.Library.LibraryGame;
import Model.Reviews.GameTab;

// Which games a tab lists, given the State row. Completed and Incomplete split the 100%ed games between them; a game with the
// More achievements tag is in that tab too (and in Incomplete as well when its review is partial or none)
public class TabMembership {

    public static boolean Contains(MainTab tab, StateSelection selection, LibraryGame game){
        if(!selection.Includes(game.GetStatus().GetGameState())){
            return false;
        }
        switch(tab){
            case COMPLETED: return game.GetStatus().Tabs().contains(GameTab.COMPLETED);
            case INCOMPLETE: return game.GetStatus().Tabs().contains(GameTab.INCOMPLETE);
            case MORE_ACHIEVEMENTS: return game.GetStatus().Tabs().contains(GameTab.MORE_ACHIEVEMENTS);
            case SERIES: return game.HasReview() && game.GetReview().GetSeriesId() != null;
            default: return true;
        }
    }
}
