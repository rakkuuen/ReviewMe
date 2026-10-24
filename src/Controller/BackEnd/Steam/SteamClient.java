package Controller.BackEnd.Steam;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import Controller.BackEnd.LocalConfig;

// Talks to Steam's web API (read-only). Calls are spaced out so a big first sync doesn't hammer Steam, and a busy or failing
// answer is retried with a growing wait. The API key goes in the URL, so it is scrubbed from anything that could be shown
public class SteamClient {
    private static final String steamHost = "https://api.steampowered.com/";
    private static final int maxAttempts = 4;
    private static final long defaultRetryWaitMillis = 1500;

    private String host, apiKey, steamId;
    private long minGapMillis, firstRetryWaitMillis;
    private long lastCallAt = 0;
    private HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public SteamClient(String apiKey, String steamId, long minGapMillis){
        this(steamHost, apiKey, steamId, minGapMillis, defaultRetryWaitMillis);
    }

    // host (ending in /) and the first retry wait can be changed so the retry and spacing rules can be tested against a local server
    public SteamClient(String host, String apiKey, String steamId, long minGapMillis, long firstRetryWaitMillis){
        this.host = host;
        this.apiKey = apiKey;
        this.steamId = steamId;
        this.minGapMillis = minGapMillis;
        this.firstRetryWaitMillis = firstRetryWaitMillis;
    }

    // Key and id come from Resources/local.properties (steam.apiKey, steam.id)
    public static SteamClient FromConfig(long minGapMillis) throws IOException {
        return new SteamClient(LocalConfig.Get("steam.apiKey"), LocalConfig.Get("steam.id"), minGapMillis);
    }

    public SteamResponse GetOwnedGames() throws SteamException {
        return Send("GetOwnedGames", "IPlayerService/GetOwnedGames/v0001/",
                "key=" + Encode(apiKey) + "&steamid=" + Encode(steamId) + "&include_appinfo=1&include_played_free_games=1&format=json");
    }

    public SteamResponse GetPlayerAchievements(int appId) throws SteamException {
        return Send("GetPlayerAchievements", "ISteamUserStats/GetPlayerAchievements/v0001/",
                "appid=" + appId + "&key=" + Encode(apiKey) + "&steamid=" + Encode(steamId) + "&l=english");
    }

    public SteamResponse GetSchemaForGame(int appId) throws SteamException {
        return Send("GetSchemaForGame", "ISteamUserStats/GetSchemaForGame/v2/", "key=" + Encode(apiKey) + "&appid=" + appId + "&l=english");
    }

    // How rare each achievement is across all players. Needs no key
    public SteamResponse GetGlobalAchievementPercentages(int appId) throws SteamException {
        return Send("GetGlobalAchievementPercentagesForApp", "ISteamUserStats/GetGlobalAchievementPercentagesForApp/v0002/", "gameid=" + appId + "&format=json");
    }

    // Retries on a network error, 429 (slow down) and 5xx. Anything else is the answer, whatever its status
    private SteamResponse Send(String callName, String path, String query) throws SteamException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(host + path + "?" + query)).timeout(Duration.ofSeconds(30)).GET().build();
        String lastProblem = "no answer";
        long wait = firstRetryWaitMillis;
        for(int attempt = 1; attempt <= maxAttempts; attempt++){
            WaitForGap();
            try {
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                lastCallAt = System.currentTimeMillis();
                int status = response.statusCode();
                if(status != 429 && status < 500){
                    return new SteamResponse(status, response.body());
                }
                lastProblem = "HTTP " + status;
            } catch (IOException e) {
                lastCallAt = System.currentTimeMillis();
                lastProblem = Scrub(e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SteamException(callName + " was interrupted");
            }
            if(attempt < maxAttempts){
                Sleep(wait);
                wait *= 2;
            }
        }
        throw new SteamException(callName + " failed after " + maxAttempts + " tries (" + lastProblem + ")");
    }

    private void WaitForGap() throws SteamException {
        long wait = lastCallAt + minGapMillis - System.currentTimeMillis();
        if(wait > 0){
            Sleep(wait);
        }
    }

    private void Sleep(long millis) throws SteamException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SteamException("interrupted while waiting");
        }
    }

    // Takes the key out of any text before it can be shown or logged
    public String Scrub(String text){
        if(text == null || apiKey.isEmpty()){
            return text;
        }
        return text.replace(apiKey, "<key>").replace(Encode(apiKey), "<key>");
    }

    private static String Encode(String value){
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
