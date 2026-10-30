package Controller.BackEnd.Steam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

import org.json.JSONException;
import org.json.JSONObject;

import Database.Dao.GameReviewDao;
import Database.Dao.SteamGameDao;
import Model.Library.SteamGame;

// Looks at what Steam says now against what is stored, and reports where they differ. It only makes calls that work with the profile
// private (the owned-games list and each game's schema), and it never writes: putting things right is the library sync and the
// achievement sync. Playtime shows whether a game was played since its achievements were synced; the schema's total shows whether
// the developer added or removed achievements
// Run:  java -cp "bin;lib/*" Controller.BackEnd.Steam.OutOfSyncCheck [--no-counts]
public class OutOfSyncCheck {
    private static final long minGapMillis = 300;
    private static final int maxFailuresInARow = 5;
    private static final int listedPerKind = 12;

    // The library signals, worked out from what Steam lists against what is stored. Pure
    public static OutOfSyncReport CompareLibrary(List<SteamGame> fetched, List<SteamGame> stored){
        OutOfSyncReport report = new OutOfSyncReport();
        Map<Integer, SteamGame> storedById = new HashMap<>();
        for(SteamGame game : stored){
            storedById.put(game.GetAppId(), game);
        }
        for(SteamGame game : fetched){
            SteamGame before = storedById.get(game.GetAppId());
            if(before == null){
                report.AddNewGame(game);
            }
            boolean played = game.GetPlaytimeMinutes() != null && game.GetPlaytimeMinutes() > 0;
            if(!played){
                continue;
            }
            if(before == null || before.GetAchievementsSyncedAt() == null){
                report.AddNeverSynced(game);
            } else if(!game.GetPlaytimeMinutes().equals(before.GetPlaytimeAtAchievementSync())){
                report.AddPlayedSince(game);
            }
        }
        return report;
    }

    // checkCounts false skips the per-game schema calls (the quick version). progress (done, total) and cancelRequested may be null
    public static OutOfSyncReport Run(SteamClient client, boolean checkCounts, BiConsumer<Integer, Integer> progress, BooleanSupplier cancelRequested) throws SteamException {
        SteamResponse response = client.GetOwnedGames();
        if(response.GetStatus() != 200){
            throw new SteamException("GetOwnedGames answered HTTP " + response.GetStatus() + " (check steam.apiKey and steam.id)");
        }
        OwnedGamesResult owned = OwnedGamesParser.Parse(response.GetBody());
        List<SteamGame> stored = SteamGameDao.GetAllSteamGames();
        OutOfSyncReport report = CompareLibrary(owned.GetGames(), stored);   // a hidden library has no games, so nothing is compared
        if(owned.AreDetailsHidden()){
            report.MarkLibraryHidden();
        }
        if(checkCounts){
            CheckCounts(client, stored, report, progress, cancelRequested);
        }
        return report;
    }

    // Each game that has had an achievement sync: is its total on Steam still the stored total? A game stored as having none (0) is
    // included, since a developer can add the first ones later
    private static void CheckCounts(SteamClient client, List<SteamGame> stored, OutOfSyncReport report, BiConsumer<Integer, Integer> progress, BooleanSupplier cancelRequested){
        List<SteamGame> toCheck = new ArrayList<>();
        for(SteamGame game : stored){
            if(game.GetAchievementsSyncedAt() != null && game.GetTotalAchievements() != null){
                toCheck.add(game);
            }
        }
        int done = 0, failuresInARow = 0;
        for(SteamGame game : toCheck){
            if(cancelRequested != null && cancelRequested.getAsBoolean()){
                report.MarkStoppedEarly();
                break;
            }
            Integer steamTotal = SteamTotal(client, game, report);
            failuresInARow = steamTotal == null ? failuresInARow + 1 : 0;
            if(steamTotal != null && !steamTotal.equals(game.GetTotalAchievements())){
                report.AddCountChange(new OutOfSyncReport.CountChange(game.GetAppId(), game.GetName(), game.GetTotalAchievements(), steamTotal));
            }
            done++;
            if(progress != null){
                progress.accept(done, toCheck.size());
            }
            if(failuresInARow >= maxFailuresInARow){
                report.MarkStoppedEarly();
                break;
            }
        }
        report.MarkCountsChecked(done);
    }

    // How many achievements the game's schema lists now, or null (with a problem noted) if that couldn't be found out
    private static Integer SteamTotal(SteamClient client, SteamGame game, OutOfSyncReport report){
        try {
            SteamResponse response = client.GetSchemaForGame(game.GetAppId());
            if(response.GetStatus() != 200){
                report.AddProblem(game.GetName() + ": the schema answered HTTP " + response.GetStatus());
                return null;
            }
            if(!LooksLikeSchema(response.GetBody())){
                report.AddProblem(game.GetName() + ": the schema answer wasn't readable");
                return null;
            }
            return SchemaParser.Parse(game.GetAppId(), response.GetBody()).size();
        } catch (SteamException e) {
            report.AddProblem(game.GetName() + ": " + client.Scrub(e.getMessage()));
            return null;
        }
    }

    // A game with no achievements has {"game":{}}; anything that isn't JSON with a "game" object isn't a schema answer at all
    private static boolean LooksLikeSchema(String body){
        try {
            new JSONObject(body).getJSONObject("game");
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        boolean checkCounts = !(args.length > 0 && args[0].equals("--no-counts"));
        GameReviewDao.Setup();
        SteamClient client = SteamClient.FromConfig(minGapMillis);
        try {
            System.out.println("Checking Steam against what's stored" + (checkCounts ? " (this looks at every synced game, so it takes a minute or two)..." : "..."));
            Print(Run(client, checkCounts, (done, total) -> { if(done % 25 == 0 || done.equals(total)) System.out.println("  checked " + done + " of " + total); }, null));
        } catch (SteamException e) {
            System.out.println("Stopped: " + client.Scrub(e.getMessage()));
        }
    }

    private static void Print(OutOfSyncReport report){
        System.out.println();
        if(report.IsLibraryHidden()){
            System.out.println("Steam sent no library, so new games and playtime couldn't be compared.");
        }
        System.out.println("New games not stored yet: " + report.GetNewGames().size());
        ListGames(report.GetNewGames());
        System.out.println("Played since their last achievement sync: " + report.GetPlayedSince().size());
        ListGames(report.GetPlayedSince());
        System.out.println("Played but never achievement-synced: " + report.GetNeverSynced().size());
        ListGames(report.GetNeverSynced());
        if(report.WereCountsChecked()){
            System.out.println("Achievement totals checked for " + report.GetGamesCountChecked() + " games. Changed: " + report.GetCountChanges().size());
            for(OutOfSyncReport.CountChange change : report.GetCountChanges()){
                System.out.println("    " + change.GetName() + ": " + change.GetStoredTotal() + " stored, " + change.GetSteamTotal() + " on Steam");
            }
        }
        for(String problem : report.GetProblems()){
            System.out.println("  problem: " + problem);
        }
        if(report.StoppedEarly()){
            System.out.println("  (stopped before every game was checked)");
        }
        System.out.println();
        if(report.IsInSync()){
            System.out.println("Everything is in sync.");
            return;
        }
        if(report.NeedsLibrarySync()){
            System.out.println("-> Run the library sync (works with your profile private).");
        }
        if(report.NeedsAchievementSync()){
            System.out.println("-> Run the achievement sync (needs Game details set to Public).");
        }
    }

    private static void ListGames(List<SteamGame> games){
        for(int i = 0; i < games.size() && i < listedPerKind; i++){
            System.out.println("    " + games.get(i).GetName() + " (" + games.get(i).GetAppId() + ")");
        }
        if(games.size() > listedPerKind){
            System.out.println("    ... and " + (games.size() - listedPerKind) + " more");
        }
    }
}
