package Controller.BackEnd.Steam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Model.Library.SteamGame;

// What a library-wide achievement sync did. Every game it tried has its GameSyncResult here, in the order tried. The lists pick
// out what the later steps care about: games that just reached 100%, games whose achievement total grew, and failures
public class AchievementSyncSummary {
    public enum StopReason { FINISHED, LIMIT_REACHED, NOT_PUBLIC, TOO_MANY_FAILURES, CANCELLED }

    private int candidateCount;
    private StopReason stopReason = StopReason.FINISHED;
    private List<SteamGame> games = new ArrayList<>();
    private List<GameSyncResult> results = new ArrayList<>();

    public AchievementSyncSummary(int candidateCount){
        this.candidateCount = candidateCount;
    }

    void Add(SteamGame game, GameSyncResult result){
        games.add(game);
        results.add(result);
    }

    void Stop(StopReason reason){
        stopReason = reason;
    }

    // How many games needed a sync when the run started
    public int GetCandidateCount(){ return candidateCount; }
    public StopReason GetStopReason(){ return stopReason; }

    // Games tried and not tried: the ones left for next time (each game saves on its own, so a later run carries on from there)
    public int GetAttemptedCount(){ return results.size(); }
    public int GetRemainingCount(){ return candidateCount - results.size(); }

    public List<GameSyncResult> GetResults(){ return Collections.unmodifiableList(results); }

    public int CountOf(GameSyncResult.Outcome outcome){
        int count = 0;
        for(GameSyncResult result : results){
            count += result.GetOutcome() == outcome ? 1 : 0;
        }
        return count;
    }

    // Saved with a full list of achievements, now all unlocked, and not already at 100% before this sync
    public List<SteamGame> GetReachedHundredPercent(){
        List<SteamGame> reached = new ArrayList<>();
        for(int i = 0; i < results.size(); i++){
            GameSyncResult result = results.get(i);
            if(result.GetOutcome() != GameSyncResult.Outcome.SYNCED){
                continue;
            }
            boolean allUnlocked = result.GetMerged().GetUnlockedCount() == result.GetMerged().GetTotal();
            boolean wasAlready = result.GetPreviousTotal() != null && result.GetPreviousTotal() > 0 && result.GetPreviousTotal().equals(result.GetPreviousUnlocked());
            if(allUnlocked && !wasAlready){
                reached.add(games.get(i));
            }
        }
        return reached;
    }

    // Games that had a known total before and have more achievements now
    public List<SteamGame> GetMoreAchievements(){
        List<SteamGame> more = new ArrayList<>();
        for(int i = 0; i < results.size(); i++){
            GameSyncResult result = results.get(i);
            if(result.GetOutcome() == GameSyncResult.Outcome.SYNCED && result.GetPreviousTotal() != null && result.GetMerged().GetTotal() > result.GetPreviousTotal()){
                more.add(games.get(i));
            }
        }
        return more;
    }

    // Games that failed or were refused, with the reason
    public List<String> GetProblems(){
        List<String> problems = new ArrayList<>();
        for(int i = 0; i < results.size(); i++){
            GameSyncResult result = results.get(i);
            if(result.GetOutcome() == GameSyncResult.Outcome.FAILED || result.GetOutcome() == GameSyncResult.Outcome.NOT_PUBLIC){
                problems.add(games.get(i).GetName() + ": " + result.GetMessage());
            }
        }
        return problems;
    }
}
