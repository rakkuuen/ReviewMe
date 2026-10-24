package Controller.BackEnd.Steam;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Reads GetGlobalAchievementPercentagesForApp: achievement api name -> percent of all players who unlocked it. A game with no
// stats, or an answer that isn't the usual JSON, gives an empty map. Percent can arrive as a number or as text
public class GlobalPercentParser {

    public static Map<String, Double> Parse(String body){
        Map<String, Double> percents = new HashMap<>();
        JSONArray array;
        try {
            array = new JSONObject(body).getJSONObject("achievementpercentages").getJSONArray("achievements");
        } catch (JSONException e) {
            return percents;
        }
        for(int i = 0; i < array.length(); i++){
            JSONObject entry = array.getJSONObject(i);
            if(entry.has("name") && entry.has("percent")){
                try {
                    percents.put(entry.getString("name"), entry.getDouble("percent"));
                } catch (JSONException e) {
                    // an entry whose percent isn't a number is skipped
                }
            }
        }
        return percents;
    }
}
