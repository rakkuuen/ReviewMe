package Controller.BackEnd.Steam;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Model.Library.SteamGame;

// Reads GetOwnedGames. Only the fields ReviewMe keeps: id, name, playtime (minutes), last played, icon. A game missing a field
// gets null for it rather than a made-up zero, so "no data" stays distinguishable from "zero hours"
public class OwnedGamesParser {

    // Throws SteamException if the body isn't the JSON Steam normally sends (an HTML error page, say)
    public static OwnedGamesResult Parse(String body) throws SteamException {
        JSONObject response;
        try {
            response = new JSONObject(body).getJSONObject("response");
        } catch (JSONException e) {
            throw new SteamException("GetOwnedGames sent something that isn't the usual JSON");
        }
        if(!response.has("game_count")){
            return new OwnedGamesResult(new ArrayList<>(), true);
        }

        List<SteamGame> games = new ArrayList<>();
        JSONArray array = response.optJSONArray("games");
        try {
            for(int i = 0; array != null && i < array.length(); i++){
                JSONObject entry = array.getJSONObject(i);
                SteamGame game = new SteamGame(entry.getInt("appid"), entry.optString("name", ""));
                game.SetPlaytimeMinutes(entry.has("playtime_forever") ? entry.getInt("playtime_forever") : null);
                game.SetLastPlayed(entry.has("rtime_last_played") ? entry.getLong("rtime_last_played") : null);
                game.SetIconHash(entry.has("img_icon_url") ? entry.getString("img_icon_url") : null);
                games.add(game);
            }
        } catch (JSONException e) {
            throw new SteamException("GetOwnedGames had a game entry that couldn't be read");
        }
        return new OwnedGamesResult(games, false);
    }
}
