package Controller.BackEnd.Reviews;

import Database.Dao.SteamGameDao;
import Model.Library.SteamGame;
import Model.Reviews.ReviewStatus;

// Works out a loaded review's status including its Steam record, when it has one. A game that isn't linked to Steam
// (no steamAppId) or hasn't been stored yet is worked out from its own flags
public class ReviewStatusLoader {

    public static ReviewStatus Load(LoadedReview loaded){
        Integer appId = loaded.GetReview().GetSteamAppId();
        SteamGame steam = appId == null ? null : SteamGameDao.GetSteamGame(appId);
        return ReviewStatusCalculator.Calculate(loaded, steam);
    }
}
