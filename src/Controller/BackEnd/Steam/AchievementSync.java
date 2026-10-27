package Controller.BackEnd.Steam;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Database.Dao.AchievementDao;
import Database.Dao.SteamGameDao;
import Model.Library.Achievement;
import Model.Library.SteamGame;

// Syncs one stored game's achievements. The player's unlocks are always fetched (and need the profile Public). The schema and
// rarity are fetched only when they have to be: nothing is stored yet, the number of achievements changed, or it's forced.
// Everything is merged and saved as one step, so a failure part-way leaves the game exactly as it was
public class AchievementSync {

    // now is epoch seconds. Unless force is set, a game that doesn't need it (never played, or not played since its last sync)
    // is skipped without any call. force is the manual "resync this game": it fetches everything again whatever the state
    public static GameSyncResult SyncGame(SteamClient client, int appId, boolean force, long now){
        SteamGame game = SteamGameDao.GetSteamGame(appId);
        if(game == null){
            return GameSyncResult.NotStored(appId);
        }
        if(!force && !game.NeedsAchievementSync()){
            return GameSyncResult.Skipped(appId);
        }
        try {
            return Sync(client, game, force, now);
        } catch (SteamException e) {
            return GameSyncResult.Failed(appId, client.Scrub(e.getMessage()));
        }
    }

    private static GameSyncResult Sync(SteamClient client, SteamGame game, boolean force, long now) throws SteamException {
        int appId = game.GetAppId();
        List<Achievement> stored = AchievementDao.GetAchievements(appId);

        SteamResponse response = client.GetPlayerAchievements(appId);
        PlayerAchievements player = PlayerAchievementsParser.Parse(response.GetStatus(), response.GetBody());
        if(player.GetOutcome() == PlayerAchievements.Outcome.NOT_PUBLIC){
            return GameSyncResult.NotPublic(appId, player.GetMessage());
        }
        if(player.GetOutcome() == PlayerAchievements.Outcome.UNAVAILABLE){
            return GameSyncResult.Failed(appId, "GetPlayerAchievements: " + player.GetMessage());
        }

        List<Achievement> schema = null;
        Map<String, Double> rarity = null;
        boolean fetchSchema = player.GetOutcome() == PlayerAchievements.Outcome.OK && !player.GetAchievements().isEmpty() && NeedsDefinitions(force, stored, player);
        if(fetchSchema){
            SteamResponse schemaResponse = client.GetSchemaForGame(appId);
            if(schemaResponse.GetStatus() != 200){
                return GameSyncResult.Failed(appId, "GetSchemaForGame answered HTTP " + schemaResponse.GetStatus());
            }
            schema = SchemaParser.Parse(appId, schemaResponse.GetBody());
            if(schema.isEmpty()){
                return GameSyncResult.Failed(appId, "GetSchemaForGame came back empty although the game has achievements");
            }
            rarity = FetchRarity(client, appId);
        }

        MergedAchievements merged = AchievementMerger.Merge(appId, schema, player, rarity, stored);
        if(!AchievementDao.SaveGameSync(appId, merged.GetAchievements(), merged.GetUnlockedCount(), now, game.GetPlaytimeMinutes())){
            return GameSyncResult.Failed(appId, "Couldn't save the achievements, nothing was changed");
        }
        return GameSyncResult.Saved(appId, merged, game.GetTotalAchievements(), game.GetUnlockedAchievements(), fetchSchema);
    }

    // Rarity is a nice extra: if it can't be had, the stored percentages stay and the sync carries on
    private static Map<String, Double> FetchRarity(SteamClient client, int appId){
        try {
            SteamResponse response = client.GetGlobalAchievementPercentages(appId);
            if(response.GetStatus() != 200){
                return null;
            }
            Map<String, Double> rarity = GlobalPercentParser.Parse(response.GetBody());
            return rarity.isEmpty() ? null : rarity;
        } catch (SteamException e) {
            return null;
        }
    }

    // The definitions (titles, icons, ...) are fetched when forced, or when the player's list isn't the stored one: a different
    // count (which includes nothing being stored yet), or a name that isn't stored
    private static boolean NeedsDefinitions(boolean force, List<Achievement> stored, PlayerAchievements player){
        if(force || stored.size() != player.GetAchievements().size()){
            return true;
        }
        Set<String> storedNames = new HashSet<>();
        for(Achievement achievement : stored){
            storedNames.add(achievement.GetApiName());
        }
        for(PlayerAchievement entry : player.GetAchievements()){
            if(!storedNames.contains(entry.GetApiName())){
                return true;
            }
        }
        return false;
    }
}
