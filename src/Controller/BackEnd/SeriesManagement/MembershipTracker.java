package Controller.BackEnd.SeriesManagement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Model.Reviews.GameReview;
import Model.Reviews.SeriesMembership;

// Remembers how each game looked before an operation touched it, so afterwards it can say which games really changed
// (to write) and what they were (to undo). Track a game before changing it
public class MembershipTracker {
    private Map<Integer, SeriesMembership> before = new LinkedHashMap<>();
    private Map<Integer, GameReview> games = new LinkedHashMap<>();

    public void Track(GameReview game){
        before.putIfAbsent(game.GetId(), SeriesMembership.Of(game));
        games.put(game.GetId(), game);
    }

    public void TrackAll(List<GameReview> more){
        for(GameReview game : more){
            Track(game);
        }
    }

    // What the changed games look like now
    public List<SeriesMembership> Forward(){
        List<SeriesMembership> changed = new ArrayList<>();
        for(Map.Entry<Integer, GameReview> entry : games.entrySet()){
            SeriesMembership after = SeriesMembership.Of(entry.getValue());
            if(!after.equals(before.get(entry.getKey()))){
                changed.add(after);
            }
        }
        return changed;
    }

    // What the same games looked like before
    public List<SeriesMembership> Backward(){
        List<SeriesMembership> original = new ArrayList<>();
        for(Map.Entry<Integer, GameReview> entry : games.entrySet()){
            if(!SeriesMembership.Of(entry.getValue()).equals(before.get(entry.getKey()))){
                original.add(before.get(entry.getKey()));
            }
        }
        return original;
    }
}
