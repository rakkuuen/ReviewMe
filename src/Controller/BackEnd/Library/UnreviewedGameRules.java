package Controller.BackEnd.Library;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import Model.Library.SteamGame;

// What may be said about a Steam game that has no review, and how typed text is tidied. No database in here
public class UnreviewedGameRules {
    public static final int maxReasonLength = 500;
    private static final int isoDateLength = 10;

    // The reason as stored: trimmed, and null when nothing was typed
    public static String CleanReason(String reason){
        if(reason == null || reason.trim().isEmpty()){
            return null;
        }
        return reason.trim();
    }

    public static boolean ReasonTooLong(String cleanReason){
        return cleanReason != null && cleanReason.length() > maxReasonLength;
    }

    // The date as stored: trimmed, null when blank
    public static String CleanDate(String date){
        return date == null || date.trim().isEmpty() ? null : date.trim();
    }

    // A real calendar date written like 2025-03-02
    public static boolean IsRealDate(String date){
        if(date == null || date.length() != isoDateLength){
            return false;
        }
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // Why a game can't be marked completed by hand, or null when it can. Games with achievements complete themselves at 100%, so
    // by hand is only for played games Steam has no achievements for
    public static String CompletionProblem(SteamGame steam){
        if(steam == null){
            return "There is no such game";
        }
        if(steam.GetPlaytimeMinutes() == null){
            return "Sync your Steam library first";
        }
        if(steam.IsNotStarted()){
            return "You haven't played this game yet";
        }
        if(steam.NeedsAchievementSync()){
            return "Sync this game's achievements first, so it is clear whether it has any";
        }
        if(steam.HasAchievements()){
            return "This game has achievements, so it completes itself at 100%. Shelve it if you have stopped playing";
        }
        return null;
    }
}
