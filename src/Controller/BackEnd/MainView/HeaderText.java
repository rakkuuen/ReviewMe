package Controller.BackEnd.MainView;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

// The two lines of words in the main view's header: how many games (and new achievements), and when Steam was last synced
public class HeaderText {
    private static final String dot = " " + (char) 0x00B7 + " ";   // A middle dot, by code point so the source stays plain ASCII
    private static final long daysShownAsDays = 7;

    // "497 games" or "497 games - 76 new achievements" (nothing is said about new achievements when there are none)
    public static String Summary(int gameCount, int newAchievements){
        String games = gameCount + (gameCount == 1 ? " game" : " games");
        if(newAchievements <= 0){
            return games;
        }
        return games + dot + newAchievements + (newAchievements == 1 ? " new achievement" : " new achievements");
    }

    // "Last synced 5 min ago", "Last synced 2 days ago", "Last synced on 2026-10-25" a week or more on, "Never synced" when null.
    // Both times are epoch seconds. A sync time in the future counts as just now
    public static String LastSynced(Long syncedAt, long now, ZoneId zone){
        if(syncedAt == null){
            return "Never synced";
        }
        Duration ago = Duration.between(Instant.ofEpochSecond(syncedAt), Instant.ofEpochSecond(now));
        if(ago.toMinutes() < 1){
            return "Last synced just now";
        }
        if(ago.toHours() < 1){
            return "Last synced " + ago.toMinutes() + " min ago";
        }
        if(ago.toDays() < 1){
            return "Last synced " + ago.toHours() + " h ago";
        }
        if(ago.toDays() < daysShownAsDays){
            return "Last synced " + ago.toDays() + (ago.toDays() == 1 ? " day ago" : " days ago");
        }
        return "Last synced on " + Instant.ofEpochSecond(syncedAt).atZone(zone).toLocalDate();
    }
}
