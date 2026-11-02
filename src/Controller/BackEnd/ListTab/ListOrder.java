package Controller.BackEnd.ListTab;

import java.util.Comparator;
import java.util.function.Function;

import Controller.BackEnd.Reviews.GameNumbers;
import Model.Library.LibraryGame;

// Puts games in the order a list asks for. Games with nothing to sort on (no rating, never played) always go last, whichever way
// round it is; ties fall back to title
public class ListOrder {

    public static Comparator<LibraryGame> Of(ListSort sort, boolean descending){
        Comparator<LibraryGame> byTitle = (a, b) -> a.GetTitle().compareToIgnoreCase(b.GetTitle());
        Comparator<LibraryGame> main;
        switch(sort){
            case RATING: main = NullsLast(g -> GameNumbers.RatingOf(g.GetReview()), descending); break;
            case PLAYTIME: main = NullsLast(g -> (long) GameNumbers.PlaytimeMinutes(g.GetReview(), g.GetSteam()), descending); break;
            case LAST_PLAYED: main = NullsLast(g -> g.GetSteam() == null ? null : g.GetSteam().GetLastPlayed(), descending); break;
            default: main = descending ? byTitle.reversed() : byTitle; break;
        }
        return main.thenComparing(byTitle);
    }

    private static <T extends Comparable<T>> Comparator<LibraryGame> NullsLast(Function<LibraryGame, T> key, boolean descending){
        return (a, b) -> {
            T x = key.apply(a), y = key.apply(b);
            if(x == null || y == null){
                return x == y ? 0 : (x == null ? 1 : -1);
            }
            return descending ? y.compareTo(x) : x.compareTo(y);
        };
    }
}
