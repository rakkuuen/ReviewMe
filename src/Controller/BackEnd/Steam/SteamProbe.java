package Controller.BackEnd.Steam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import Model.Library.Achievement;
import Model.Library.SteamGame;

// A few real calls to Steam to check the key, the profile's privacy and the shape of the answers. Reads only; touches no
// database. Each raw answer is saved into the folder you give (keep it outside the repo) so the parsers can be tested on real data
// Run:  java -cp "bin;lib/*" Controller.BackEnd.Steam.SteamProbe <captureFolder>
public class SteamProbe {
    private static final long gapMillis = 300;
    private static final int maxGamesTried = 8;

    public static void main(String[] args) throws IOException {
        if(args.length < 1){
            System.out.println("Give a folder to save the raw answers in (outside the repo).");
            return;
        }
        Path folder = Paths.get(args[0]);
        Files.createDirectories(folder);
        SteamClient client = SteamClient.FromConfig(gapMillis);

        try {
            Run(client, folder);
        } catch (SteamException e) {
            System.out.println("Stopped: " + client.Scrub(e.getMessage()));
        }
    }

    private static void Run(SteamClient client, Path folder) throws IOException {
        SteamResponse owned = client.GetOwnedGames();
        Save(folder, "GetOwnedGames.json", owned);
        System.out.println("GetOwnedGames: HTTP " + owned.GetStatus() + ", " + owned.GetBody().length() + " bytes");
        if(owned.GetStatus() != 200){
            System.out.println("  Steam refused it. A 401/403 usually means a wrong key; check steam.apiKey.");
            return;
        }
        OwnedGamesResult library = OwnedGamesParser.Parse(owned.GetBody());
        if(library.AreDetailsHidden()){
            System.out.println("  Steam sent nothing: the game details are private for this caller. Set Game details to Public in Steam privacy settings, then run again.");
            return;
        }
        List<SteamGame> games = library.GetGames();
        System.out.println("  " + games.size() + " games, " + Count(games, true) + " with playtime, " + Count(games, false) + " never played");

        List<SteamGame> candidates = new ArrayList<>();
        for(SteamGame game : games){
            if(game.GetPlaytimeMinutes() != null && game.GetPlaytimeMinutes() > 0){
                candidates.add(game);
            }
        }
        candidates.sort(Comparator.comparing(SteamGame::GetLastPlayed, Comparator.nullsLast(Comparator.<Long>reverseOrder())));

        boolean sampledWithAchievements = false, sampledNoStats = false;
        for(int i = 0; i < candidates.size() && i < maxGamesTried && !(sampledWithAchievements && sampledNoStats); i++){
            SteamGame game = candidates.get(i);
            SteamResponse player = client.GetPlayerAchievements(game.GetAppId());
            PlayerAchievements parsed = PlayerAchievementsParser.Parse(player.GetStatus(), player.GetBody());
            System.out.println("\n" + game.GetName() + " (" + game.GetAppId() + ")");
            System.out.println("  GetPlayerAchievements: HTTP " + player.GetStatus() + " -> " + parsed.GetOutcome()
                    + (parsed.GetMessage() == null ? "" : " (" + parsed.GetMessage() + ")"));
            if(parsed.GetOutcome() == PlayerAchievements.Outcome.NOT_PUBLIC){
                Save(folder, "player_" + game.GetAppId() + ".json", player);
                System.out.println("  Achievements are private for this caller. Games list worked, so only the achievement data needs Public.");
                return;
            }
            boolean wantThis = (parsed.GetOutcome() == PlayerAchievements.Outcome.OK && !parsed.GetAchievements().isEmpty() && !sampledWithAchievements)
                    || (parsed.GetOutcome() == PlayerAchievements.Outcome.NO_STATS && !sampledNoStats);
            if(!wantThis){
                continue;
            }
            Save(folder, "player_" + game.GetAppId() + ".json", player);
            sampledNoStats |= parsed.GetOutcome() == PlayerAchievements.Outcome.NO_STATS;
            if(parsed.GetOutcome() == PlayerAchievements.Outcome.OK){
                sampledWithAchievements = true;
                System.out.println("  " + parsed.GetAchievements().size() + " achievements, " + parsed.GetUnlockedCount() + " unlocked");
            }
            ProbeSchemaAndPercents(client, folder, game);
        }
        if(!sampledWithAchievements){
            System.out.println("\nNo game with achievements in the first " + maxGamesTried + " tried, so nothing to check the schema against.");
        }
    }

    private static void ProbeSchemaAndPercents(SteamClient client, Path folder, SteamGame game) throws IOException {
        SteamResponse schema = client.GetSchemaForGame(game.GetAppId());
        Save(folder, "schema_" + game.GetAppId() + ".json", schema);
        List<Achievement> defined = SchemaParser.Parse(game.GetAppId(), schema.GetBody());
        int hidden = 0, withDescription = 0;
        for(Achievement achievement : defined){
            hidden += achievement.IsHidden() ? 1 : 0;
            withDescription += achievement.GetDescription() != null ? 1 : 0;
        }
        System.out.println("  GetSchemaForGame: HTTP " + schema.GetStatus() + " -> " + defined.size() + " defined, " + hidden + " hidden, " + withDescription + " with a description");

        SteamResponse percents = client.GetGlobalAchievementPercentages(game.GetAppId());
        Save(folder, "global_" + game.GetAppId() + ".json", percents);
        Map<String, Double> rarity = GlobalPercentParser.Parse(percents.GetBody());
        System.out.println("  GetGlobalAchievementPercentages: HTTP " + percents.GetStatus() + " -> " + rarity.size() + " percentages");
    }

    private static int Count(List<SteamGame> games, boolean played){
        int count = 0;
        for(SteamGame game : games){
            boolean hasPlaytime = game.GetPlaytimeMinutes() != null && game.GetPlaytimeMinutes() > 0;
            count += hasPlaytime == played ? 1 : 0;
        }
        return count;
    }

    private static void Save(Path folder, String fileName, SteamResponse response) throws IOException {
        Files.writeString(folder.resolve(fileName), response.GetBody(), StandardCharsets.UTF_8);
    }
}
