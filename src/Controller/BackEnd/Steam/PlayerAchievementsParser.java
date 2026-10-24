package Controller.BackEnd.Steam;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Reads GetPlayerAchievements. Steam answers a game with no stats with a 400 and {"success": false, "error": "Requested app has
// no stats"}, a private profile with a 403 and "Profile is not public", so the error text decides the outcome
public class PlayerAchievementsParser {

    public static PlayerAchievements Parse(int status, String body){
        JSONObject stats;
        try {
            stats = new JSONObject(body).getJSONObject("playerstats");
        } catch (JSONException e) {
            return new PlayerAchievements(PlayerAchievements.Outcome.UNAVAILABLE, "HTTP " + status + " with no readable answer", new ArrayList<>());
        }

        if(!stats.optBoolean("success", false)){
            return Failed(status, stats.optString("error", ""));
        }

        List<PlayerAchievement> achievements = new ArrayList<>();
        JSONArray array = stats.optJSONArray("achievements");
        for(int i = 0; array != null && i < array.length(); i++){
            JSONObject entry = array.getJSONObject(i);
            boolean unlocked = entry.optInt("achieved", 0) == 1;
            long unlockTime = entry.optLong("unlocktime", 0);
            achievements.add(new PlayerAchievement(entry.getString("apiname"), unlocked, unlocked && unlockTime > 0 ? unlockTime : null));
        }
        return new PlayerAchievements(PlayerAchievements.Outcome.OK, null, achievements);
    }

    private static PlayerAchievements Failed(int status, String error){
        String lower = error.toLowerCase();
        PlayerAchievements.Outcome outcome = PlayerAchievements.Outcome.UNAVAILABLE;
        if(lower.contains("no stats")){
            outcome = PlayerAchievements.Outcome.NO_STATS;
        } else if(lower.contains("not public") || lower.contains("private")){
            outcome = PlayerAchievements.Outcome.NOT_PUBLIC;
        }
        return new PlayerAchievements(outcome, error.isEmpty() ? "HTTP " + status : error, new ArrayList<>());
    }
}
