package Controller.BackEnd.SeriesManagement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import Model.Reviews.GameReview;

// The order of the games inside one series. Everything here returns a new list; only Renumber changes the reviews
// (it writes positions 1 to n, which is how the order is stored). A game with no position yet goes after the ones that have one
public class SeriesOrder {
    private static final Comparator<GameReview> byPositionThenTitle = Comparator
            .comparing(GameReview::GetSeriesPosition, Comparator.nullsLast(Comparator.<Integer>naturalOrder()))
            .thenComparing((GameReview game) -> game.GetTitle().toLowerCase())
            .thenComparing(GameReview::GetTitle);

    public static List<GameReview> Sorted(List<GameReview> games){
        List<GameReview> sorted = new ArrayList<>(games);
        sorted.sort(byPositionThenTitle);
        return sorted;
    }

    // Gives the games positions 1 to n in the order given, so there are never gaps or repeats
    public static List<GameReview> Renumber(List<GameReview> ordered){
        for(int i = 0; i < ordered.size(); i++){
            ordered.get(i).SetSeriesPosition(i + 1);
        }
        return ordered;
    }

    public static List<GameReview> Appended(List<GameReview> ordered, GameReview game){
        List<GameReview> result = new ArrayList<>(ordered);
        result.add(game);
        return result;
    }

    // Games are matched by title. A game that isn't in the list leaves it as it was
    public static List<GameReview> Without(List<GameReview> ordered, GameReview game){
        List<GameReview> result = new ArrayList<>(ordered);
        result.removeIf(other -> other.GetTitle().equals(game.GetTitle()));
        return result;
    }

    // The game moved to the given 0-based place, held inside the list
    public static List<GameReview> MovedTo(List<GameReview> ordered, GameReview game, int newIndex){
        int from = IndexOf(ordered, game);
        if(from < 0){
            return new ArrayList<>(ordered);
        }
        List<GameReview> result = new ArrayList<>(ordered);
        GameReview moving = result.remove(from);
        result.add(Math.max(0, Math.min(newIndex, result.size())), moving);
        return result;
    }

    public static boolean CanMoveBy(List<GameReview> ordered, GameReview game, int delta){
        int from = IndexOf(ordered, game);
        int to = from + delta;
        return from >= 0 && to >= 0 && to < ordered.size();
    }

    // One place up (-1) or down (+1); a game at the end stays put
    public static List<GameReview> MovedBy(List<GameReview> ordered, GameReview game, int delta){
        return CanMoveBy(ordered, game, delta) ? MovedTo(ordered, game, IndexOf(ordered, game) + delta) : new ArrayList<>(ordered);
    }

    private static int IndexOf(List<GameReview> ordered, GameReview game){
        for(int i = 0; i < ordered.size(); i++){
            if(ordered.get(i).GetTitle().equals(game.GetTitle())){
                return i;
            }
        }
        return -1;
    }
}
