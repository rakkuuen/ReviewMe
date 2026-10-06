package Database.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Model.Library.SteamGame;

// Steam data is filled by two separate API calls (owned games, then achievement totals), so there are
// targeted writes that never wipe the other call's columns. Writes return true if the database was changed
public class SteamGameDao {

    private static final String saveSQL = """
        INSERT INTO SteamGame (appId, name, playtimeMinutes, lastPlayed, iconHash, totalAchievements, unlockedAchievements, syncedAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (appId) DO UPDATE SET
            name = excluded.name, playtimeMinutes = excluded.playtimeMinutes, lastPlayed = excluded.lastPlayed,
            iconHash = excluded.iconHash, totalAchievements = excluded.totalAchievements,
            unlockedAchievements = excluded.unlockedAchievements, syncedAt = excluded.syncedAt;
    """;

    // Only what the owned-games call returns. Achievement totals already stored are kept
    private static final String saveOwnedSQL = """
        INSERT INTO SteamGame (appId, name, playtimeMinutes, lastPlayed, iconHash, syncedAt)
        VALUES (?, ?, ?, ?, ?, ?)
        ON CONFLICT (appId) DO UPDATE SET
            name = excluded.name, playtimeMinutes = excluded.playtimeMinutes, lastPlayed = excluded.lastPlayed,
            iconHash = excluded.iconHash, syncedAt = excluded.syncedAt;
    """;

    // Every column, so the game ends up exactly as the object says
    public static boolean SaveSteamGame(SteamGame game){
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(saveSQL)) {
            BindAll(pstmt, game);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving SteamGame " + game.GetAppId() + ": " + e.getMessage());
            return false;
        }
    }

    // One transaction for the whole library. Name, playtime, last played, icon and sync time only
    public static boolean SaveOwnedGames(List<SteamGame> games){
        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(saveOwnedSQL)) {
                for(SteamGame game : games){
                    pstmt.setInt(1, game.GetAppId());
                    pstmt.setString(2, game.GetName());
                    SetNullableInt(pstmt, 3, game.GetPlaytimeMinutes());
                    SetNullableLong(pstmt, 4, game.GetLastPlayed());
                    pstmt.setString(5, game.GetIconHash());
                    SetNullableLong(pstmt, 6, game.GetSyncedAt());
                    pstmt.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error saving owned games: " + e.getMessage());
            return false;
        }
    }

    // Only updates a game that's already stored (false otherwise)
    public static boolean SaveAchievementTotals(int appId, Integer totalAchievements, Integer unlockedAchievements){
        String updateSQL = "UPDATE SteamGame SET totalAchievements = ?, unlockedAchievements = ? WHERE appId = ?;";

        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            SetNullableInt(pstmt, 1, totalAchievements);
            SetNullableInt(pstmt, 2, unlockedAchievements);
            pstmt.setInt(3, appId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving achievement totals for " + appId + ": " + e.getMessage());
            return false;
        }
    }

    // Null if the game is not stored
    public static SteamGame GetSteamGame(int appId){
        String query = "SELECT * FROM SteamGame WHERE appId = ?;";

        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, appId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? ReadSteamGame(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving SteamGame: " + e.getMessage());
            return null;
        }
    }

    public static List<SteamGame> GetAllSteamGames(){
        List<SteamGame> games = new ArrayList<>();
        String query = "SELECT * FROM SteamGame ORDER BY name COLLATE NOCASE;";

        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while(rs.next()){
                games.add(ReadSteamGame(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving SteamGames: " + e.getMessage());
        }
        return games;
    }

    private static void BindAll(PreparedStatement pstmt, SteamGame game) throws SQLException {
        pstmt.setInt(1, game.GetAppId());
        pstmt.setString(2, game.GetName());
        SetNullableInt(pstmt, 3, game.GetPlaytimeMinutes());
        SetNullableLong(pstmt, 4, game.GetLastPlayed());
        pstmt.setString(5, game.GetIconHash());
        SetNullableInt(pstmt, 6, game.GetTotalAchievements());
        SetNullableInt(pstmt, 7, game.GetUnlockedAchievements());
        SetNullableLong(pstmt, 8, game.GetSyncedAt());
    }

    private static SteamGame ReadSteamGame(ResultSet rs) throws SQLException {
        SteamGame game = new SteamGame(rs.getInt("appId"), rs.getString("name"));
        game.SetPlaytimeMinutes(Db.GetNullableInt(rs, "playtimeMinutes"));
        game.SetLastPlayed(Db.GetNullableLong(rs, "lastPlayed"));
        game.SetIconHash(rs.getString("iconHash"));
        game.SetTotalAchievements(Db.GetNullableInt(rs, "totalAchievements"));
        game.SetUnlockedAchievements(Db.GetNullableInt(rs, "unlockedAchievements"));
        game.SetSyncedAt(Db.GetNullableLong(rs, "syncedAt"));
        return game;
    }

    private static void SetNullableInt(PreparedStatement pstmt, int index, Integer value) throws SQLException {
        if(value == null){
            pstmt.setNull(index, java.sql.Types.INTEGER);
        } else {
            pstmt.setInt(index, value);
        }
    }

    private static void SetNullableLong(PreparedStatement pstmt, int index, Long value) throws SQLException {
        if(value == null){
            pstmt.setNull(index, java.sql.Types.INTEGER);
        } else {
            pstmt.setLong(index, value);
        }
    }
}
