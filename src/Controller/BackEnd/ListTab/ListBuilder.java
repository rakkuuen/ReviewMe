package Controller.BackEnd.ListTab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import Controller.BackEnd.MainView.MainTab;
import Controller.BackEnd.MainView.StateSelection;
import Controller.BackEnd.MainView.TabMembership;
import Controller.BackEnd.MainView.ViewCounter;
import Controller.BackEnd.MainView.ViewRules;
import Controller.BackEnd.Overview.GameRow;
import Controller.BackEnd.Overview.OverviewRows;
import Controller.BackEnd.Overview.OverviewSectionKind;
import Controller.BackEnd.Reviews.GameNumbers;
import Model.Library.LibraryGame;
import Model.Reviews.ReviewGap;
import Model.Reviews.ReviewState;

// Works out one list tab: which games are in it, which of those pass the search and chips, their order, and the first pages of
// them as rows. No drawing and no database in here
public class ListBuilder {
    private static final String reviewKeyPrefix = "review:";
    private static final String reasonKeyPrefix = "reason:";
    private static final String manualReasonKey = reasonKeyPrefix + "manual";
    private static final String manualReasonLabel = "Marked incomplete by you";
    private static final String pathSeparator = " > ";

    public static String ReviewChipKey(ReviewState review){
        return reviewKeyPrefix + review.name();
    }

    // The review state a chip key stands for, or null when it isn't a review chip
    public static ReviewState ReviewOfKey(String key){
        if(key == null || !key.startsWith(reviewKeyPrefix)){
            return null;
        }
        try {
            return ReviewState.valueOf(key.substring(reviewKeyPrefix.length()));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean IsReasonKey(String key){
        return key != null && key.startsWith(reasonKeyPrefix);
    }

    public static ListResult Build(List<LibraryGame> games, StateSelection selection, MainTab tab, ListQuery query, int pageSize){
        List<LibraryGame> inTab = new ArrayList<>();
        for(LibraryGame game : games){
            if(TabMembership.Contains(tab, selection, game)){
                inTab.add(game);
            }
        }
        List<ListChip> reviewChips = ReviewChips(games, tab, selection, query);
        List<ListChip> reasonChips = ListRules.HasReasonChips(tab) ? ReasonChips(inTab, query) : new ArrayList<>();

        Set<ReviewState> offered = new HashSet<>(ViewRules.ReviewStatesForTab(tab, selection));
        offered.retainAll(query.GetReviewStates());   // A chip the tab doesn't have can't be on
        List<LibraryGame> matching = new ArrayList<>();
        for(LibraryGame game : inTab){
            if(Passes(game, tab, query, offered)){
                matching.add(game);
            }
        }
        matching.sort(Order(query));

        OverviewSectionKind kind = KindOf(tab, selection);
        int shown = Math.min(matching.size(), pageSize * (query.GetMorePages() + 1));
        List<GameRow> rows = new ArrayList<>();
        for(LibraryGame game : matching.subList(0, shown)){
            rows.add(OverviewRows.Build(kind, game));
        }
        return new ListResult(rows, matching.size(), inTab.size(), reviewChips, reasonChips);
    }

    private static OverviewSectionKind KindOf(MainTab tab, StateSelection selection){
        switch(tab){
            case COMPLETED: return OverviewSectionKind.COMPLETED;
            case INCOMPLETE: return OverviewSectionKind.INCOMPLETE;
            case MORE_ACHIEVEMENTS: return OverviewSectionKind.MORE_ACHIEVEMENTS;
            default:
                switch(selection.GetOnly()){
                    case SHELVED: return OverviewSectionKind.SHELVED;
                    case NOT_STARTED: return OverviewSectionKind.NOT_STARTED;
                    default: return OverviewSectionKind.IN_PROGRESS;
                }
        }
    }

    private static List<ListChip> ReviewChips(List<LibraryGame> games, MainTab tab, StateSelection selection, ListQuery query){
        Map<ReviewState, Integer> counts = ViewCounter.ReviewCounts(games, tab, selection);
        List<ListChip> chips = new ArrayList<>();
        for(ReviewState review : ViewRules.ReviewStatesForTab(tab, selection)){
            chips.add(new ListChip(ReviewChipKey(review), ReviewWords(review), counts.getOrDefault(review, 0), query.GetReviewStates().contains(review)));
        }
        return chips;
    }

    private static String ReviewWords(ReviewState review){
        switch(review){
            case COMPLETE: return "Complete";
            case PARTIAL: return "Partial";
            default: return "No review yet";
        }
    }

    // The reasons games are on the Incomplete tab: each section with something missing (most games first), then the by-hand flag
    private static List<ListChip> ReasonChips(List<LibraryGame> inTab, ListQuery query){
        Map<String, Integer> sections = new TreeMap<>();
        int manual = 0;
        for(LibraryGame game : inTab){
            for(String reason : ReasonKeys(game)){
                if(reason.equals(manualReasonKey)){
                    manual++;
                } else {
                    sections.merge(reason, 1, Integer::sum);
                }
            }
        }
        List<Map.Entry<String, Integer>> ordered = new ArrayList<>(sections.entrySet());
        ordered.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        List<ListChip> chips = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : ordered){
            chips.add(new ListChip(entry.getKey(), entry.getKey().substring(reasonKeyPrefix.length()), entry.getValue(), query.GetReasons().contains(entry.getKey())));
        }
        if(manual > 0){
            chips.add(new ListChip(manualReasonKey, manualReasonLabel, manual, query.GetReasons().contains(manualReasonKey)));
        }
        return chips;
    }

    // The chip keys a game has: one per section it is missing something from, and the by-hand flag
    public static List<String> ReasonKeys(LibraryGame game){
        List<String> keys = new ArrayList<>();
        for(ReviewGap gap : game.GetStatus().GetGaps()){
            String key = gap.GetKind() == ReviewGap.Kind.MANUAL_FLAG ? manualReasonKey : reasonKeyPrefix + Section(gap.GetPath());
            if(!keys.contains(key)){
                keys.add(key);
            }
        }
        return keys;
    }

    // "Story > Elena" is a gap in Story
    private static String Section(String path){
        int split = path.indexOf(pathSeparator);
        return split < 0 ? path : path.substring(0, split);
    }

    private static boolean Passes(LibraryGame game, MainTab tab, ListQuery query, Set<ReviewState> reviewStates){
        if(!query.GetSearch().isEmpty() && !game.GetTitle().toLowerCase(Locale.ROOT).contains(query.GetSearch().toLowerCase(Locale.ROOT))){
            return false;
        }
        if(!reviewStates.isEmpty() && !reviewStates.contains(game.GetStatus().GetReviewState())){
            return false;
        }
        if(ListRules.HasReasonChips(tab) && !query.GetReasons().isEmpty()){
            for(String key : ReasonKeys(game)){
                if(query.GetReasons().contains(key)){
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    // Games with nothing to sort on (no rating, never played) always go last, whichever way round it is; ties fall back to title
    private static Comparator<LibraryGame> Order(ListQuery query){
        Comparator<LibraryGame> byTitle = (a, b) -> a.GetTitle().compareToIgnoreCase(b.GetTitle());
        Comparator<LibraryGame> main;
        switch(query.GetSort()){
            case RATING: main = NullsLast(g -> GameNumbers.RatingOf(g.GetReview()), query.IsDescending()); break;
            case PLAYTIME: main = NullsLast(g -> (long) GameNumbers.PlaytimeMinutes(g.GetReview(), g.GetSteam()), query.IsDescending()); break;
            case LAST_PLAYED: main = NullsLast(g -> g.GetSteam() == null ? null : g.GetSteam().GetLastPlayed(), query.IsDescending()); break;
            default: main = query.IsDescending() ? byTitle.reversed() : byTitle; break;
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
