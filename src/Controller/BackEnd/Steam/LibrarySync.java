package Controller.BackEnd.Steam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import Database.Dao.GameReviewDao;
import Database.Dao.SteamGameDao;
import Model.Library.SteamGame;

// Copies the owned-games list from Steam into the SteamGame table: name, playtime, last played and icon. Achievement totals already
// stored are left alone, and a game's first-seen time is set once and never moved. Games Steam no longer lists are kept.
// Run:  java -cp "bin;lib/*" Controller.BackEnd.Steam.LibrarySync [--write]   (without --write it only reports what it would do)
public class LibrarySync {
    private static final long minGapMillis = 300;
    private static final int listedPerKind = 12;

    // Fetches the library and saves it. A private profile, or any failure to get a proper answer, saves nothing
    public static LibrarySyncResult Run(SteamClient client) throws SteamException {
        return Run(client, true);
    }

    // The same, but with save false it only works out what would change
    public static LibrarySyncResult Run(SteamClient client, boolean save) throws SteamException {
        SteamResponse response = client.GetOwnedGames();
        if(response.GetStatus() != 200){
            throw new SteamException("GetOwnedGames answered HTTP " + response.GetStatus() + " (check steam.apiKey and steam.id)");
        }
        OwnedGamesResult owned = OwnedGamesParser.Parse(response.GetBody());
        if(owned.AreDetailsHidden()){
            return LibrarySyncResult.Hidden();
        }
        return save ? Apply(owned.GetGames(), System.currentTimeMillis() / 1000) : Compare(owned.GetGames(), SteamGameDao.GetAllSteamGames());
    }

    // Saves the games with now (epoch seconds) as their sync time. New games also get it as their first-seen time
    public static LibrarySyncResult Apply(List<SteamGame> fetched, long now){
        LibrarySyncResult result = Compare(fetched, SteamGameDao.GetAllSteamGames());
        for(SteamGame game : fetched){
            game.SetSyncedAt(now);
        }
        if(!SteamGameDao.SaveOwnedGames(fetched)){
            return LibrarySyncResult.SaveFailed();
        }
        return result;
    }

    // Sorts the fetched games into new, updated and unchanged against what is stored. Changes nothing
    public static LibrarySyncResult Compare(List<SteamGame> fetched, List<SteamGame> stored){
        Map<Integer, SteamGame> storedById = new HashMap<>();
        for(SteamGame game : stored){
            storedById.put(game.GetAppId(), game);
        }
        List<SteamGame> newGames = new ArrayList<>(), updated = new ArrayList<>();
        Set<Integer> listed = new HashSet<>();
        int unchanged = 0;
        for(SteamGame game : fetched){
            if(!listed.add(game.GetAppId())){
                continue;
            }
            SteamGame before = storedById.get(game.GetAppId());
            if(before == null){
                newGames.add(game);
            } else if(Differs(before, game)){
                updated.add(game);
            } else {
                unchanged++;
            }
        }
        int notInAnswer = 0;
        for(Integer id : storedById.keySet()){
            notInAnswer += listed.contains(id) ? 0 : 1;
        }
        return new LibrarySyncResult(newGames, updated, unchanged, notInAnswer);
    }

    private static boolean Differs(SteamGame before, SteamGame after){
        return !Objects.equals(before.GetName(), after.GetName()) || !Objects.equals(before.GetPlaytimeMinutes(), after.GetPlaytimeMinutes())
                || !Objects.equals(before.GetLastPlayed(), after.GetLastPlayed()) || !Objects.equals(before.GetIconHash(), after.GetIconHash());
    }

    public static void main(String[] args) throws IOException {
        boolean write = args.length > 0 && args[0].equals("--write");
        GameReviewDao.Setup();
        SteamClient client = SteamClient.FromConfig(minGapMillis);
        try {
            LibrarySyncResult result = Run(client, write);
            Print(result, write);
        } catch (SteamException e) {
            System.out.println("Stopped: " + client.Scrub(e.getMessage()));
        }
    }

    private static void Print(LibrarySyncResult result, boolean write){
        if(result.AreDetailsHidden()){
            System.out.println("Steam sent nothing: the game details are private for this caller, so nothing was " + (write ? "saved." : "compared."));
            return;
        }
        if(!result.Succeeded()){
            System.out.println(result.GetProblem());
            return;
        }
        System.out.println((write ? "Saved " : "Dry run (nothing saved): would save ") + result.GetTotal() + " games from Steam.");
        System.out.println("  new: " + result.GetNewGames().size() + " | updated: " + result.GetUpdatedGames().size() + " | unchanged: " + result.GetUnchangedCount()
                + " | stored but not listed by Steam this time (kept): " + result.GetNotInAnswerCount());
        ListSome("new", result.GetNewGames());
        ListSome("updated", result.GetUpdatedGames());
        if(!write){
            System.out.println("Add --write to save it.");
        }
    }

    private static void ListSome(String kind, List<SteamGame> games){
        for(int i = 0; i < games.size() && i < listedPerKind; i++){
            SteamGame game = games.get(i);
            System.out.println("    " + kind + ": " + game.GetName() + " (" + game.GetAppId() + ")");
        }
        if(games.size() > listedPerKind){
            System.out.println("    ... and " + (games.size() - listedPerKind) + " more " + kind);
        }
    }
}
