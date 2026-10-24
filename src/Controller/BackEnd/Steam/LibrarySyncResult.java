package Controller.BackEnd.Steam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Model.Library.SteamGame;

// What a library sync did (or, for a dry run, would do). New games weren't stored before. Updated games were, but their name,
// playtime, last played or icon is different now. notInAnswerCount is stored games Steam didn't list this time: they are kept
public class LibrarySyncResult {
    private List<SteamGame> newGames, updatedGames;
    private int unchangedCount, notInAnswerCount;
    private boolean detailsHidden;
    private String problem;

    public LibrarySyncResult(List<SteamGame> newGames, List<SteamGame> updatedGames, int unchangedCount, int notInAnswerCount){
        this.newGames = Collections.unmodifiableList(new ArrayList<>(newGames));
        this.updatedGames = Collections.unmodifiableList(new ArrayList<>(updatedGames));
        this.unchangedCount = unchangedCount;
        this.notInAnswerCount = notInAnswerCount;
    }

    // Steam's answer was empty because the game details are private to this caller. Nothing was saved
    public static LibrarySyncResult Hidden(){
        LibrarySyncResult result = new LibrarySyncResult(new ArrayList<>(), new ArrayList<>(), 0, 0);
        result.detailsHidden = true;
        return result;
    }

    // The comparison worked but the save didn't, so nothing was changed
    public static LibrarySyncResult SaveFailed(){
        LibrarySyncResult result = new LibrarySyncResult(new ArrayList<>(), new ArrayList<>(), 0, 0);
        result.problem = "Couldn't save the library, nothing was changed";
        return result;
    }

    public List<SteamGame> GetNewGames(){ return newGames; }
    public List<SteamGame> GetUpdatedGames(){ return updatedGames; }
    public int GetUnchangedCount(){ return unchangedCount; }
    public int GetNotInAnswerCount(){ return notInAnswerCount; }
    public boolean AreDetailsHidden(){ return detailsHidden; }

    // Null when all is well
    public String GetProblem(){ return problem; }

    public boolean Succeeded(){ return !detailsHidden && problem == null; }
    public int GetTotal(){ return newGames.size() + updatedGames.size() + unchangedCount; }
}
