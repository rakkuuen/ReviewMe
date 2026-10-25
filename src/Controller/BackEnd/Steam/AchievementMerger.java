package Controller.BackEnd.Steam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Model.Library.Achievement;

// Combines what Steam says about one game into the achievement list to store. Matching is always by api name, never by position.
// The player's answer decides what is unlocked and when. The schema (when it was fetched) decides titles, descriptions, hidden
// flags and icons, otherwise what is stored is kept. Rarity is only replaced when it was fetched too. Pure: nothing is read or
// written here, and the lists passed in are not changed
public class AchievementMerger {

    // schema and rarity are null when they weren't fetched this time (stored values are kept instead). stored is what the
    // database holds now (empty if nothing). player must be OK, or NO_STATS, which is a game with no achievements
    public static MergedAchievements Merge(int appId, List<Achievement> schema, PlayerAchievements player, Map<String, Double> rarity, List<Achievement> stored){
        if(player.GetOutcome() == PlayerAchievements.Outcome.NO_STATS){
            return new MergedAchievements(new ArrayList<>(), new ArrayList<>(), NamesOf(stored), new ArrayList<>());
        }
        if(player.GetOutcome() != PlayerAchievements.Outcome.OK){
            throw new IllegalArgumentException("Can't merge a player answer that is " + player.GetOutcome());
        }

        Map<String, Achievement> storedByName = ByName(stored);
        Map<String, Achievement> definitions = ByName(schema != null ? schema : stored);
        Map<String, PlayerAchievement> playerByName = new LinkedHashMap<>();
        for(PlayerAchievement entry : player.GetAchievements()){
            playerByName.put(entry.GetApiName(), entry);
        }

        Set<String> order = new LinkedHashSet<>(definitions.keySet());
        order.addAll(playerByName.keySet());

        List<Achievement> merged = new ArrayList<>();
        List<String> newlyUnlocked = new ArrayList<>();
        for(String name : order){
            Achievement old = storedByName.get(name);
            Achievement definition = definitions.containsKey(name) ? definitions.get(name) : old;
            Achievement result = new Achievement(appId, name);
            CopyDefinition(definition, result);
            result.SetGlobalPercent(RarityOf(name, rarity, old));
            ApplyUnlock(playerByName.get(name), old, result);
            if(result.IsUnlocked() && (old == null || !old.IsUnlocked())){
                newlyUnlocked.add(name);
            }
            merged.add(result);
        }

        List<String> added = new ArrayList<>(), removed = new ArrayList<>();
        for(String name : order){
            if(!storedByName.containsKey(name)){
                added.add(name);
            }
        }
        for(String name : storedByName.keySet()){
            if(!order.contains(name)){
                removed.add(name);
            }
        }
        return new MergedAchievements(merged, added, removed, newlyUnlocked);
    }

    private static void CopyDefinition(Achievement from, Achievement to){
        if(from == null){
            return;
        }
        to.SetTitle(from.GetTitle());
        to.SetDescription(from.GetDescription());
        to.SetHidden(from.IsHidden());
        to.SetIconUrl(from.GetIconUrl());
        to.SetIconGrayUrl(from.GetIconGrayUrl());
    }

    // The fetched percentage if there is one for this achievement, otherwise the stored one (an achievement added since the
    // rarity list was fetched has none yet)
    private static Double RarityOf(String name, Map<String, Double> rarity, Achievement old){
        if(rarity != null && rarity.containsKey(name)){
            return rarity.get(name);
        }
        return old == null ? null : old.GetGlobalPercent();
    }

    // Unlocked only if the player's answer says so. Its time is Steam's; if Steam gave none, a time stored earlier for an
    // achievement that was already unlocked is kept
    private static void ApplyUnlock(PlayerAchievement entry, Achievement old, Achievement result){
        if(entry == null || !entry.IsUnlocked()){
            return;
        }
        result.SetUnlocked(true);
        if(entry.GetUnlockedAt() != null){
            result.SetUnlockedAt(entry.GetUnlockedAt());
        } else if(old != null && old.IsUnlocked()){
            result.SetUnlockedAt(old.GetUnlockedAt());
        }
    }

    private static Map<String, Achievement> ByName(List<Achievement> achievements){
        Map<String, Achievement> byName = new LinkedHashMap<>();
        for(Achievement achievement : achievements){
            byName.put(achievement.GetApiName(), achievement);
        }
        return byName;
    }

    private static List<String> NamesOf(List<Achievement> achievements){
        return new ArrayList<>(ByName(achievements).keySet());
    }
}
