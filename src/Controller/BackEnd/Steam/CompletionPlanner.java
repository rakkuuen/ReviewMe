package Controller.BackEnd.Steam;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Controller.BackEnd.Steam.CompletionAction.Kind;
import Model.Library.SteamGame;
import Model.Reviews.CompletionSource;
import Model.Reviews.GameReview;

// Works out, for each review linked to a Steam game, whether its completion should be recorded from Steam's achievements. Pure: it
// reads nothing and writes nothing. A review with no Steam link is not mentioned at all
public class CompletionPlanner {

    // steamById is the stored Steam games; lastUnlockByApp is the epoch seconds of each game's most recent unlock. The date is worked
    // out in the given time zone
    public static List<CompletionAction> Plan(List<GameReview> reviews, Map<Integer, SteamGame> steamById, Map<Integer, Long> lastUnlockByApp, ZoneId zone){
        List<CompletionAction> actions = new ArrayList<>();
        for(GameReview review : reviews){
            if(review.GetSteamAppId() == null){
                continue;
            }
            actions.add(ActionFor(review, steamById.get(review.GetSteamAppId()), lastUnlockByApp.get(review.GetSteamAppId()), zone));
        }
        return actions;
    }

    private static CompletionAction ActionFor(GameReview review, SteamGame steam, Long lastUnlock, ZoneId zone){
        if(review.IsShelved()){
            return CompletionAction.Skip(review, Kind.SHELVED);
        }
        if(steam == null || steam.GetTotalAchievements() == null){
            return CompletionAction.Skip(review, Kind.NO_STEAM_DATA);
        }
        if(!steam.HasAchievements()){
            return CompletionAction.Skip(review, Kind.NO_ACHIEVEMENTS);
        }
        if(!steam.IsHundredPercent()){
            return CompletionAction.Skip(review, Kind.BELOW_FULL);
        }

        String date = lastUnlock == null ? null : Instant.ofEpochSecond(lastUnlock).atZone(zone).toLocalDate().toString();
        if(!review.IsCompleted()){
            return new CompletionAction(review, Kind.RECORD, true, date, date == null ? null : CompletionSource.STEAM, steam.GetTotalAchievements());
        }

        boolean needsDate = review.GetCompletedOn() == null && date != null;
        boolean needsBaseline = review.GetAchievementsAtCompletion() == null;
        if(!needsDate && !needsBaseline){
            return CompletionAction.Skip(review, Kind.ALREADY_COMPLETE);
        }
        return new CompletionAction(review, Kind.FILL, true,
                needsDate ? date : review.GetCompletedOn(),
                needsDate ? CompletionSource.STEAM : review.GetCompletedOnSource(),
                needsBaseline ? steam.GetTotalAchievements() : review.GetAchievementsAtCompletion());
    }
}
