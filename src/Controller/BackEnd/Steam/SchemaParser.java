package Controller.BackEnd.Steam;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Model.Library.Achievement;

// Reads GetSchemaForGame: every achievement the game defines (name, description, hidden, icons), with nothing about the player.
// A game with no achievements has no list, and that gives an empty result. A hidden achievement has no description until Steam
// reveals it, so its description stays null
public class SchemaParser {
    private static final String iconFolder = "https://cdn.cloudflare.steamstatic.com/steamcommunity/public/images/apps/";

    public static List<Achievement> Parse(int appId, String body){
        List<Achievement> achievements = new ArrayList<>();
        JSONArray array;
        try {
            array = new JSONObject(body).getJSONObject("game").getJSONObject("availableGameStats").getJSONArray("achievements");
        } catch (JSONException e) {
            return achievements;
        }

        try {
            for(int i = 0; i < array.length(); i++){
                JSONObject entry = array.getJSONObject(i);
                Achievement achievement = new Achievement(appId, entry.getString("name"));
                achievement.SetTitle(entry.optString("displayName", null));
                achievement.SetDescription(entry.has("description") ? entry.getString("description") : null);
                achievement.SetHidden(entry.optInt("hidden", 0) == 1);
                achievement.SetIconUrl(IconUrl(appId, entry.optString("icon", null)));
                achievement.SetIconGrayUrl(IconUrl(appId, entry.optString("icongray", null)));
                achievements.add(achievement);
            }
        } catch (JSONException e) {
            return new ArrayList<>();   // one unreadable entry makes the whole list unreliable, so it counts as no list
        }
        return achievements;
    }

    // Steam gives either a full link or just the file's hash, so a bare hash is turned into a link
    private static String IconUrl(int appId, String icon){
        if(icon == null || icon.isEmpty() || icon.startsWith("http")){
            return icon == null || icon.isEmpty() ? null : icon;
        }
        return iconFolder + appId + "/" + icon + ".jpg";
    }
}
