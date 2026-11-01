package Controller.BackEnd.TopPicks;

import java.util.Map;

import Database.Dao.TopPickDao;

// Pinning games to the Top picks slots. Each change is worked out first, then saved together or not at all.
// slotCount is how many slots there are (from the layout settings); the database allows up to 5
public class TopPickManager {
    private static final String noSuchGame = "There is no such game";
    private static final String couldNotSave = "Couldn't save that";

    // Pins a game that has a review to an empty slot (moves it if it is pinned already)
    public static TopPickResult Pin(int reviewId, int slot, int slotCount){
        if(!TopPickDao.ReviewExists(reviewId)){
            return TopPickResult.Refused(noSuchGame);
        }
        return Run(TopPickPlanner.Pin(TopPickDao.GetPicks(), reviewId, slot, slotCount));
    }

    // Pins a game to a slot whoever is in it; that game is unpinned
    public static TopPickResult Replace(int reviewId, int slot, int slotCount){
        if(!TopPickDao.ReviewExists(reviewId)){
            return TopPickResult.Refused(noSuchGame);
        }
        return Run(TopPickPlanner.Replace(TopPickDao.GetPicks(), reviewId, slot, slotCount));
    }

    // Moves a pinned game to another slot, swapping with whoever is there
    public static TopPickResult Move(int reviewId, int slot, int slotCount){
        return Run(TopPickPlanner.Move(TopPickDao.GetPicks(), reviewId, slot, slotCount));
    }

    public static TopPickResult Unpin(int reviewId){
        return Run(TopPickPlanner.Unpin(TopPickDao.GetPicks(), reviewId));
    }

    // Slot to review id for everything pinned now
    public static Map<Integer, Integer> GetPicks(){
        return TopPickDao.GetPicks();
    }

    private static TopPickResult Run(TopPickPlan plan){
        if(plan.IsRefused()){
            return TopPickResult.Refused(plan.GetError());
        }
        if(plan.GetChanges().isEmpty()){
            return TopPickResult.Done();
        }
        return TopPickDao.Apply(plan.GetChanges()) ? TopPickResult.Done() : TopPickResult.Refused(couldNotSave);
    }
}
