package Controller.BackEnd.TopPicks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Works out what each Top picks change does. picks is who holds each slot now (slot to review id); slots run 1 to slotCount.
// Nothing here touches the database, and unpinning leaves the other picks where they are
public class TopPickPlanner {

    // Puts a game in an empty slot. A game that is already pinned is moved instead
    public static TopPickPlan Pin(Map<Integer, Integer> picks, int reviewId, int slot, int slotCount){
        if(!InRange(slot, slotCount)){
            return Refused(slotCount);
        }
        if(SlotOf(picks, reviewId) != null){
            return Move(picks, reviewId, slot, slotCount);
        }
        if(picks.containsKey(slot)){
            return TopPickPlan.Refused("That slot is taken. Replace it, or pick another slot");
        }
        return Plan(new TopPickChange(reviewId, slot));
    }

    // Puts a game in a slot, taking the place of whoever is there (who is unpinned). A game pinned elsewhere leaves that slot
    public static TopPickPlan Replace(Map<Integer, Integer> picks, int reviewId, int slot, int slotCount){
        if(!InRange(slot, slotCount)){
            return Refused(slotCount);
        }
        Integer occupant = picks.get(slot);
        if(occupant != null && occupant == reviewId){
            return TopPickPlan.Nothing();
        }
        List<TopPickChange> changes = new ArrayList<>();
        changes.add(new TopPickChange(reviewId, slot));
        if(occupant != null){
            changes.add(new TopPickChange(occupant, null));
        }
        return TopPickPlan.Of(changes);
    }

    // Moves a pinned game to another slot. If that slot is held, the two swap places
    public static TopPickPlan Move(Map<Integer, Integer> picks, int reviewId, int slot, int slotCount){
        Integer from = SlotOf(picks, reviewId);
        if(from == null){
            return TopPickPlan.Refused("This game isn't pinned");
        }
        if(!InRange(slot, slotCount)){
            return Refused(slotCount);
        }
        if(from == slot){
            return TopPickPlan.Nothing();
        }
        List<TopPickChange> changes = new ArrayList<>();
        changes.add(new TopPickChange(reviewId, slot));
        Integer occupant = picks.get(slot);
        if(occupant != null){
            changes.add(new TopPickChange(occupant, from));
        }
        return TopPickPlan.Of(changes);
    }

    public static TopPickPlan Unpin(Map<Integer, Integer> picks, int reviewId){
        if(SlotOf(picks, reviewId) == null){
            return TopPickPlan.Refused("This game isn't pinned");
        }
        return Plan(new TopPickChange(reviewId, null));
    }

    // The slot a game holds, or null when it isn't pinned
    public static Integer SlotOf(Map<Integer, Integer> picks, int reviewId){
        for(Map.Entry<Integer, Integer> pick : picks.entrySet()){
            if(pick.getValue() == reviewId){
                return pick.getKey();
            }
        }
        return null;
    }

    private static boolean InRange(int slot, int slotCount){
        return slot >= 1 && slot <= slotCount;
    }

    private static TopPickPlan Refused(int slotCount){
        return TopPickPlan.Refused("The slot has to be from 1 to " + slotCount);
    }

    private static TopPickPlan Plan(TopPickChange change){
        List<TopPickChange> changes = new ArrayList<>();
        changes.add(change);
        return TopPickPlan.Of(changes);
    }
}
