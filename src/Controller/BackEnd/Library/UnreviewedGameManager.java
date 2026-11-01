package Controller.BackEnd.Library;

import Database.Dao.SteamGameDao;
import Database.Dao.SteamGameStatusDao;
import Model.Library.SteamGame;
import Model.Library.SteamGameStatus;
import Model.Reviews.CompletionSource;

// Shelving and completing by hand for Steam games that have no review. A game with a review keeps these on the review instead.
// A game's record is removed as soon as nothing is set on it
public class UnreviewedGameManager {
    private static final String couldNotSave = "Couldn't save that";

    // Shelves the game, or changes the reason of a game that is already shelved. The reason is optional
    public static UnreviewedGameResult Shelve(int appId, String reason){
        String problem = SetProblem(appId);
        String clean = UnreviewedGameRules.CleanReason(reason);
        if(problem == null && UnreviewedGameRules.ReasonTooLong(clean)){
            problem = "The reason can be at most " + UnreviewedGameRules.maxReasonLength + " characters";
        }
        if(problem != null){
            return UnreviewedGameResult.Refused(problem);
        }
        SteamGameStatus now = Current(appId);
        return Save(new SteamGameStatus(appId, true, clean, now.IsCompleted(), now.GetCompletedOn(), now.GetCompletedOnSource()));
    }

    public static UnreviewedGameResult Unshelve(int appId){
        SteamGameStatus now = Current(appId);
        if(!now.IsShelved()){
            return UnreviewedGameResult.Refused("This game isn't shelved");
        }
        return Save(new SteamGameStatus(appId, false, null, now.IsCompleted(), now.GetCompletedOn(), now.GetCompletedOnSource()));
    }

    // Marks a played game with no achievements as completed. completedOn is optional, written like 2025-03-02
    public static UnreviewedGameResult MarkCompleted(int appId, String completedOn){
        String problem = SetProblem(appId);
        if(problem == null){
            problem = UnreviewedGameRules.CompletionProblem(SteamGameDao.GetSteamGame(appId));
        }
        String date = UnreviewedGameRules.CleanDate(completedOn);
        if(problem == null && date != null && !UnreviewedGameRules.IsRealDate(date)){
            problem = "The date needs to be like 2025-03-02";
        }
        if(problem != null){
            return UnreviewedGameResult.Refused(problem);
        }
        SteamGameStatus now = Current(appId);
        return Save(new SteamGameStatus(appId, now.IsShelved(), now.GetShelvedReason(), true, date, date == null ? null : CompletionSource.MANUAL));
    }

    public static UnreviewedGameResult ClearCompleted(int appId){
        SteamGameStatus now = Current(appId);
        if(!now.IsCompleted()){
            return UnreviewedGameResult.Refused("This game isn't marked completed");
        }
        return Save(new SteamGameStatus(appId, now.IsShelved(), now.GetShelvedReason(), false, null, null));
    }

    // Null when the game can be given a status: it is on Steam and has no review
    private static String SetProblem(int appId){
        if(SteamGameDao.GetSteamGame(appId) == null){
            return "There is no such game";
        }
        if(SteamGameStatusDao.ReviewExistsFor(appId)){
            return "This game has a review, so shelve or complete it from the review";
        }
        return null;
    }

    private static SteamGameStatus Current(int appId){
        SteamGameStatus stored = SteamGameStatusDao.Get(appId);
        return stored != null ? stored : new SteamGameStatus(appId, false, null, false, null, null);
    }

    private static UnreviewedGameResult Save(SteamGameStatus status){
        boolean worked = status.IsEmpty() ? SteamGameStatusDao.Delete(status.GetAppId()) : SteamGameStatusDao.Save(status);
        return worked ? UnreviewedGameResult.Done() : UnreviewedGameResult.Refused(couldNotSave);
    }
}
