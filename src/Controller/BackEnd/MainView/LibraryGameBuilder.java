package Controller.BackEnd.MainView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Controller.BackEnd.Reviews.LoadedReview;
import Controller.BackEnd.Reviews.ReviewStatusCalculator;
import Model.Library.LibraryGame;
import Model.Library.SteamGame;
import Model.Library.SteamGameStatus;

// Joins reviews and Steam games into one list with no duplicates: every review (with its Steam game when it has one), then every
// Steam game that no review is linked to. Pure: the database is read elsewhere
public class LibraryGameBuilder {

    public static List<LibraryGame> Build(List<LoadedReview> reviews, List<SteamGame> steamGames){
        return Build(reviews, steamGames, Collections.<Integer, SteamGameStatus>emptyMap());
    }

    // statusByAppId is what the user said about Steam games with no review. A game that has a review ignores it: the review holds that
    public static List<LibraryGame> Build(List<LoadedReview> reviews, List<SteamGame> steamGames, Map<Integer, SteamGameStatus> statusByAppId){
        Map<Integer, SteamGame> steamById = new HashMap<>();
        for(SteamGame steam : steamGames){
            steamById.put(steam.GetAppId(), steam);
        }

        List<LibraryGame> games = new ArrayList<>();
        Set<Integer> linked = new HashSet<>();
        for(LoadedReview loaded : reviews){
            Integer appId = loaded.GetReview().GetSteamAppId();
            SteamGame steam = appId == null ? null : steamById.get(appId);
            if(steam != null){
                linked.add(steam.GetAppId());
            }
            games.add(new LibraryGame(loaded.GetReview(), steam, ReviewStatusCalculator.Calculate(loaded, steam)));
        }

        for(SteamGame steam : steamGames){
            if(!linked.contains(steam.GetAppId())){
                SteamGameStatus flags = statusByAppId.get(steam.GetAppId());
                games.add(new LibraryGame(null, steam, ReviewStatusCalculator.CalculateUnreviewed(steam, flags), flags));
            }
        }
        return games;
    }
}
