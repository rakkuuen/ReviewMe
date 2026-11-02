package Controller.BackEnd.Overview;

import java.util.ArrayList;
import java.util.List;

// The Overview's stat cards, in the order they are shown, worded from the numbers. The Completed card is twice as wide for its chart
public class StatCards {
    private static final int single = 1;
    private static final int wide = 2;

    public static List<StatCard> Build(OverviewStats stats){
        List<StatCard> cards = new ArrayList<>();
        cards.add(new StatCard(StatsText.Count(stats.GetOwnedCount()), "Owned", StatsText.Count(stats.GetReviewedCount()) + " reviewed", false, single));
        cards.add(new StatCard(StatsText.Count(stats.GetCompletedCount()), "Completed", null, true, wide));
        cards.add(new StatCard(StatsText.Count(stats.GetAchievementsUnlocked()), "Achievements", "unlocked", false, single));
        cards.add(new StatCard(StatsText.Hours(stats.GetPlaytimeMinutes()), "Playtime", "hours", false, single));
        cards.add(new StatCard(StatsText.Average(stats.GetAverageRating()), "Avg rating", "of reviewed", false, single));
        return cards;
    }
}
