package Controller.BackEnd.Steam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Model.Library.SteamGame;

// What an out-of-sync check found, without having changed anything. The library parts need no Public profile to fix; the achievement
// parts (played since, never synced, count changed) are put right by an achievement sync, which does
public class OutOfSyncReport {

    // A game whose achievement total on Steam is not the total stored for it
    public static class CountChange {
        private int appId, storedTotal, steamTotal;
        private String name;

        public CountChange(int appId, String name, int storedTotal, int steamTotal){
            this.appId = appId;
            this.name = name;
            this.storedTotal = storedTotal;
            this.steamTotal = steamTotal;
        }

        public int GetAppId(){ return appId; }
        public String GetName(){ return name; }
        public int GetStoredTotal(){ return storedTotal; }
        public int GetSteamTotal(){ return steamTotal; }
    }

    private List<SteamGame> newGames = new ArrayList<>(), playedSince = new ArrayList<>(), neverSynced = new ArrayList<>();
    private List<CountChange> countChanges = new ArrayList<>();
    private List<String> problems = new ArrayList<>();
    private boolean libraryHidden, countsChecked, stoppedEarly;
    private int gamesCountChecked;

    void AddNewGame(SteamGame game){ newGames.add(game); }
    void AddPlayedSince(SteamGame game){ playedSince.add(game); }
    void AddNeverSynced(SteamGame game){ neverSynced.add(game); }
    void AddCountChange(CountChange change){ countChanges.add(change); }
    void AddProblem(String problem){ problems.add(problem); }
    void MarkLibraryHidden(){ libraryHidden = true; }
    void MarkCountsChecked(int gamesChecked){ countsChecked = true; gamesCountChecked = gamesChecked; }
    void MarkStoppedEarly(){ stoppedEarly = true; }

    // In Steam's library but not stored yet
    public List<SteamGame> GetNewGames(){ return Collections.unmodifiableList(newGames); }

    // Stored with an achievement sync, but Steam now reports different playtime than it had then: played since
    public List<SteamGame> GetPlayedSince(){ return Collections.unmodifiableList(playedSince); }

    // Played, and no achievement sync yet
    public List<SteamGame> GetNeverSynced(){ return Collections.unmodifiableList(neverSynced); }

    public List<CountChange> GetCountChanges(){ return Collections.unmodifiableList(countChanges); }
    public List<String> GetProblems(){ return Collections.unmodifiableList(problems); }

    // Steam sent no library (game details are private to this caller), so the library parts below could not be compared
    public boolean IsLibraryHidden(){ return libraryHidden; }

    // The per-game achievement totals were looked at (and for how many games)
    public boolean WereCountsChecked(){ return countsChecked; }
    public int GetGamesCountChecked(){ return gamesCountChecked; }

    // The run was cancelled, or Steam kept failing, before every game's total was looked at
    public boolean StoppedEarly(){ return stoppedEarly; }

    // Nothing found, and nothing left unchecked
    public boolean IsInSync(){
        return newGames.isEmpty() && playedSince.isEmpty() && neverSynced.isEmpty() && countChanges.isEmpty() && problems.isEmpty() && !libraryHidden && !stoppedEarly;
    }

    // Whether putting it right needs an achievement sync (and so a Public profile). New games alone only need a library sync
    public boolean NeedsAchievementSync(){
        return !playedSince.isEmpty() || !neverSynced.isEmpty() || !countChanges.isEmpty();
    }

    public boolean NeedsLibrarySync(){
        return !newGames.isEmpty();
    }
}
