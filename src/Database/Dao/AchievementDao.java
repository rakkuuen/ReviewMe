package Database.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Model.Library.Achievement;

// A sync rebuilds a game's whole achievement list in one go (schema, your unlocks and rarity merged first)
public class AchievementDao {

    private static final String insertSQL = """
        INSERT INTO Achievement (appId, apiName, title, description, hidden, iconUrl, iconGrayUrl, globalPercent, unlocked, unlockedAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
    """;

    // Swaps every stored achievement of this game for the new list. All or nothing
    public static boolean ReplaceAchievements(int appId, List<Achievement> achievements){
        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
            try {
                ReplaceIn(conn, appId, achievements);
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error saving achievements for " + appId + ": " + e.getMessage());
            return false;
        }
    }

    // What one game's achievement sync learned, saved as a single step: the new achievement list, and on the game itself its
    // counts, when it was synced and its playtime then. If the game isn't stored, or anything fails, nothing at all is saved
    public static boolean SaveGameSync(int appId, List<Achievement> achievements, int unlockedCount, long syncedAt, Integer playtimeAtSync){
        String updateSQL = "UPDATE SteamGame SET totalAchievements = ?, unlockedAchievements = ?, achievementsSyncedAt = ?, playtimeAtAchievementSync = ? WHERE appId = ?;";

        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
            try {
                ReplaceIn(conn, appId, achievements);
                try (PreparedStatement update = conn.prepareStatement(updateSQL)) {
                    update.setInt(1, achievements.size());
                    update.setInt(2, unlockedCount);
                    update.setLong(3, syncedAt);
                    Db.SetNullableInt(update, 4, playtimeAtSync);
                    update.setInt(5, appId);
                    if(update.executeUpdate() != 1){
                        throw new SQLException("game " + appId + " is not stored");
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error saving achievement sync for " + appId + ": " + e.getMessage());
            return false;
        }
    }

    private static void ReplaceIn(Connection conn, int appId, List<Achievement> achievements) throws SQLException {
        try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM Achievement WHERE appId = ?;");
             PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {

            deleteStmt.setInt(1, appId);
            deleteStmt.executeUpdate();

            for(Achievement achievement : achievements){
                insertStmt.setInt(1, appId);
                insertStmt.setString(2, achievement.GetApiName());
                insertStmt.setString(3, achievement.GetTitle());
                insertStmt.setString(4, achievement.GetDescription());
                insertStmt.setInt(5, achievement.IsHidden() ? 1 : 0);
                insertStmt.setString(6, achievement.GetIconUrl());
                insertStmt.setString(7, achievement.GetIconGrayUrl());
                if(achievement.GetGlobalPercent() == null){
                    insertStmt.setNull(8, java.sql.Types.REAL);
                } else {
                    insertStmt.setDouble(8, achievement.GetGlobalPercent());
                }
                insertStmt.setInt(9, achievement.IsUnlocked() ? 1 : 0);
                Db.SetNullableLong(insertStmt, 10, achievement.GetUnlockedAt());
                insertStmt.executeUpdate();
            }
        }
    }

    // In the order Steam listed them
    public static List<Achievement> GetAchievements(int appId){
        List<Achievement> achievements = new ArrayList<>();
        String query = "SELECT * FROM Achievement WHERE appId = ? ORDER BY rowid;";

        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, appId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()){
                    Achievement achievement = new Achievement(rs.getInt("appId"), rs.getString("apiName"));
                    achievement.SetTitle(rs.getString("title"));
                    achievement.SetDescription(rs.getString("description"));
                    achievement.SetHidden(rs.getInt("hidden") == 1);
                    achievement.SetIconUrl(rs.getString("iconUrl"));
                    achievement.SetIconGrayUrl(rs.getString("iconGrayUrl"));
                    achievement.SetGlobalPercent(Db.GetNullableDouble(rs, "globalPercent"));
                    achievement.SetUnlocked(rs.getInt("unlocked") == 1);
                    achievement.SetUnlockedAt(Db.GetNullableLong(rs, "unlockedAt"));
                    achievements.add(achievement);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving achievements: " + e.getMessage());
        }
        return achievements;
    }

    public static boolean DeleteAchievements(int appId){
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Achievement WHERE appId = ?;")) {
            pstmt.setInt(1, appId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting achievements: " + e.getMessage());
            return false;
        }
    }
}
