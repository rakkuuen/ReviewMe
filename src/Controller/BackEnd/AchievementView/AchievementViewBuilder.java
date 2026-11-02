package Controller.BackEnd.AchievementView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Controller.BackEnd.GameDetail.DetailText;
import Model.Library.Achievement;
import Model.Reviews.ManualAchievement;

// Works out a game's achievements page. Steam's achievements are used when it has any; otherwise the ones kept by hand for the review.
// No drawing and no database in here
public class AchievementViewBuilder {
    private static final String dot = " " + (char) 0x00B7 + " ";   // A middle dot, by code point so the source stays plain ASCII
    private static final String noValue = "-";
    private static final String hiddenTitle = "Hidden achievement";
    private static final String hiddenDescription = "Unlock it to reveal what it is.";
    private static final int percentOfWhole = 100;

    // The share of players below which an achievement counts as rare
    public static final double rareBelowPercent = 5.0;

    public static AchievementViewData Build(String gameTitle, List<Achievement> steam, List<ManualAchievement> manual){
        List<AchievementItem> items = new ArrayList<>();
        if(steam != null && !steam.isEmpty()){
            for(Achievement achievement : steam){
                items.add(FromSteam(achievement));
            }
        } else if(manual != null){
            for(ManualAchievement achievement : manual){
                items.add(FromManual(achievement));
            }
        }
        return Summarise(gameTitle, items);
    }

    private static AchievementItem FromSteam(Achievement achievement){
        boolean hiddenLocked = achievement.IsHidden() && !achievement.IsUnlocked();
        String title = hiddenLocked || achievement.GetTitle() == null || achievement.GetTitle().trim().isEmpty() ? (hiddenLocked ? hiddenTitle : achievement.GetApiName()) : achievement.GetTitle().trim();
        String description = hiddenLocked ? hiddenDescription : achievement.GetDescription() == null ? "" : achievement.GetDescription().trim();
        long time = achievement.IsUnlocked() && achievement.GetUnlockedAt() != null ? achievement.GetUnlockedAt() : 0;
        String when = achievement.IsUnlocked() ? DetailText.DateOfEpoch(achievement.GetUnlockedAt()) : "";
        return new AchievementItem(title, description, achievement.IsUnlocked(), hiddenLocked, achievement.GetGlobalPercent(), time, when);
    }

    // A manual achievement has no rarity and only a typed date (which sorts as a day number so the dates still order)
    private static AchievementItem FromManual(ManualAchievement achievement){
        String when = achievement.IsUnlocked() ? DetailText.Date(achievement.GetUnlockedOn()) : "";
        long time = achievement.IsUnlocked() ? DayNumber(achievement.GetUnlockedOn()) : 0;
        return new AchievementItem(achievement.GetTitle(), achievement.GetDescription() == null ? "" : achievement.GetDescription(), achievement.IsUnlocked(), false, null, time, when);
    }

    private static long DayNumber(String isoDate){
        try {
            return isoDate == null ? 0 : java.time.LocalDate.parse(isoDate.trim()).toEpochDay() + 1;
        } catch (java.time.format.DateTimeParseException e) {
            return 0;
        }
    }

    private static AchievementViewData Summarise(String title, List<AchievementItem> items){
        int unlocked = 0, hidden = 0;
        Double rarest = null;
        for(AchievementItem item : items){
            if(item.IsUnlocked()){
                unlocked++;
                if(item.GetRarity() != null && (rarest == null || item.GetRarity() < rarest)){
                    rarest = item.GetRarity();
                }
            } else if(item.IsHiddenLocked()){
                hidden++;
            }
        }
        int total = items.size();
        int locked = total - unlocked;
        double fraction = total == 0 ? 0.0 : unlocked / (double) total;
        String percent = Math.round(fraction * percentOfWhole) + "% complete";
        String lockedText = locked + " locked" + (hidden > 0 ? dot + hidden + " hidden" : "");
        String lastText = LatestText(items);
        return new AchievementViewData(title, unlocked, total, percent, lockedText, fraction, lastText, rarest == null ? noValue : Percent(rarest), items);
    }

    // The date text of the achievement unlocked last
    private static String LatestText(List<AchievementItem> items){
        AchievementItem latest = null;
        for(AchievementItem item : items){
            if(item.IsUnlocked() && item.GetSortTime() > 0 && (latest == null || item.GetSortTime() > latest.GetSortTime())){
                latest = item;
            }
        }
        return latest == null ? noValue : latest.GetUnlockedText();
    }

    // 61.2 -> "61.2%"
    public static String Percent(double percent){
        return String.format(Locale.US, "%.1f%%", percent);
    }

    // The words under the figure on a row: "of players" for a common one, "rare" for one under the rare line, nothing when unknown
    public static String RarityWords(AchievementItem item){
        if(item.GetRarity() == null){
            return "";
        }
        return item.GetRarity() < rareBelowPercent ? "rare" : "of players";
    }
}
