package Controller.BackEnd.Steam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;

import Database.Dao.GameReviewDao;
import Database.Dao.SteamGameDao;
import Model.Library.SteamGame;

// Syncs achievements for every stored game that needs it, one game at a time. Each game is saved on its own, so a stopped or failed
// run loses nothing and the next run carries on with what is left. It stops at once if Steam says the profile isn't Public, and
// after too many failures in a row (Steam is probably down). Games never played have nothing to unlock and are never asked about
// Run:  java -cp "bin;lib/*" Controller.BackEnd.Steam.AchievementSyncRun [--write] [--refresh-all] [--limit N]
//   without --write it only lists what it would do and makes no calls
public class AchievementSyncRun {
    public static final int maxFailuresInARow = 5;
    private static final long minGapMillis = 300;

    // The played games to sync, most recently played first. Normally only those that need it (never synced, or played since);
    // refreshAll takes every played game. limit (null for none) keeps just the first few
    public static List<SteamGame> SelectCandidates(List<SteamGame> stored, boolean refreshAll, Integer limit){
        List<SteamGame> candidates = new ArrayList<>();
        for(SteamGame game : stored){
            boolean played = game.GetPlaytimeMinutes() != null && game.GetPlaytimeMinutes() > 0;
            if(played && (refreshAll || game.NeedsAchievementSync())){
                candidates.add(game);
            }
        }
        candidates.sort(Comparator.comparing(SteamGame::GetLastPlayed, Comparator.nullsLast(Comparator.<Long>reverseOrder())).thenComparing(SteamGame::GetName, String.CASE_INSENSITIVE_ORDER));
        if(limit != null && limit < candidates.size()){
            return new ArrayList<>(candidates.subList(0, Math.max(0, limit)));
        }
        return candidates;
    }

    // progress and cancelRequested may be null. refreshAll is the "resync the whole library": every played game, everything refetched
    public static AchievementSyncSummary Run(SteamClient client, boolean refreshAll, Integer limit, SyncProgress progress, BooleanSupplier cancelRequested){
        List<SteamGame> all = SteamGameDao.GetAllSteamGames();
        List<SteamGame> candidates = SelectCandidates(all, refreshAll, null);
        AchievementSyncSummary summary = new AchievementSyncSummary(candidates.size());
        List<SteamGame> toSync = SelectCandidates(all, refreshAll, limit);

        int failuresInARow = 0;
        for(SteamGame game : toSync){
            if(cancelRequested != null && cancelRequested.getAsBoolean()){
                summary.Stop(AchievementSyncSummary.StopReason.CANCELLED);
                return summary;
            }
            GameSyncResult result = AchievementSync.SyncGame(client, game.GetAppId(), refreshAll, System.currentTimeMillis() / 1000);
            summary.Add(game, result);
            if(progress != null){
                progress.Report(summary.GetAttemptedCount(), toSync.size(), game, result);
            }
            if(result.GetOutcome() == GameSyncResult.Outcome.NOT_PUBLIC){
                summary.Stop(AchievementSyncSummary.StopReason.NOT_PUBLIC);
                return summary;
            }
            failuresInARow = result.GetOutcome() == GameSyncResult.Outcome.FAILED ? failuresInARow + 1 : 0;
            if(failuresInARow >= maxFailuresInARow){
                summary.Stop(AchievementSyncSummary.StopReason.TOO_MANY_FAILURES);
                return summary;
            }
        }
        if(limit != null && limit < candidates.size()){
            summary.Stop(AchievementSyncSummary.StopReason.LIMIT_REACHED);
        }
        return summary;
    }

    public static void main(String[] args) throws IOException {
        boolean write = false, refreshAll = false;
        Integer limit = null;
        for(int i = 0; i < args.length; i++){
            if(args[i].equals("--write")){
                write = true;
            } else if(args[i].equals("--refresh-all")){
                refreshAll = true;
            } else if(args[i].equals("--limit") && i + 1 < args.length){
                limit = Integer.parseInt(args[++i]);
            }
        }
        GameReviewDao.Setup();
        List<SteamGame> stored = SteamGameDao.GetAllSteamGames();
        if(stored.isEmpty()){
            System.out.println("No games are stored yet. Sync the library first (Controller.BackEnd.Steam.LibrarySync --write).");
            return;
        }
        List<SteamGame> planned = SelectCandidates(stored, refreshAll, limit);
        if(!write){
            PrintPlan(planned, refreshAll);
            return;
        }
        SteamClient client = SteamClient.FromConfig(minGapMillis);
        System.out.println("Syncing achievements for " + planned.size() + " games (needs Game details set to Public)...");
        AchievementSyncSummary summary = Run(client, refreshAll, limit, (done, total, game, result) -> System.out.println("[" + done + "/" + total + "] " + Describe(game, result)), null);
        PrintSummary(summary, client);
    }

    private static void PrintPlan(List<SteamGame> planned, boolean refreshAll){
        System.out.println("Dry run (no calls, nothing saved): " + planned.size() + " games would be synced" + (refreshAll ? " (refresh all)." : "."));
        for(int i = 0; i < planned.size() && i < 15; i++){
            System.out.println("    " + planned.get(i).GetName() + " (" + planned.get(i).GetAppId() + ")");
        }
        if(planned.size() > 15){
            System.out.println("    ... and " + (planned.size() - 15) + " more");
        }
        System.out.println("About " + (planned.size() * 3 * minGapMillis / 1000) + " seconds at the most (3 calls a game on a first sync). Add --write to run it.");
    }

    private static String Describe(SteamGame game, GameSyncResult result){
        String detail;
        switch(result.GetOutcome()){
            case SYNCED: detail = result.GetMerged().GetUnlockedCount() + " of " + result.GetMerged().GetTotal() + " unlocked"; break;
            case NO_ACHIEVEMENTS: detail = "no achievements"; break;
            case NOT_PUBLIC: case FAILED: detail = result.GetOutcome() + ": " + result.GetMessage(); break;
            default: detail = result.GetOutcome().toString();
        }
        return game.GetName() + " - " + detail;
    }

    private static void PrintSummary(AchievementSyncSummary summary, SteamClient client){
        System.out.println();
        System.out.println("Done: " + summary.GetAttemptedCount() + " of " + summary.GetCandidateCount() + " games tried (" + summary.GetStopReason() + ").");
        System.out.println("  synced: " + summary.CountOf(GameSyncResult.Outcome.SYNCED) + " | no achievements: " + summary.CountOf(GameSyncResult.Outcome.NO_ACHIEVEMENTS)
                + " | failed: " + summary.CountOf(GameSyncResult.Outcome.FAILED) + " | left for next time: " + summary.GetRemainingCount());
        if(summary.GetStopReason() == AchievementSyncSummary.StopReason.NOT_PUBLIC){
            System.out.println("  Steam says the profile isn't public. Set Game details to Public, then run again; it carries on where it stopped.");
        }
        for(SteamGame game : summary.GetReachedHundredPercent()){
            System.out.println("  reached 100%: " + game.GetName());
        }
        for(SteamGame game : summary.GetMoreAchievements()){
            System.out.println("  more achievements than before: " + game.GetName());
        }
        for(String problem : summary.GetProblems()){
            System.out.println("  problem: " + client.Scrub(problem));
        }
    }
}
