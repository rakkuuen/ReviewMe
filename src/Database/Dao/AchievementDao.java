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

    // Swaps every stored achievement of this game for the new list. All or nothing
    public static boolean ReplaceAchievements(int appId, List<Achievement> achievements){
        String insertSQL = """
            INSERT INTO Achievement (appId, apiName, title, description, hidden, iconUrl, iconGrayUrl, globalPercent, unlocked, unlockedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
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
                    if(achievement.GetUnlockedAt() == null){
                        insertStmt.setNull(10, java.sql.Types.INTEGER);
                    } else {
                        insertStmt.setLong(10, achievement.GetUnlockedAt());
                    }
                    insertStmt.executeUpdate();
                }
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
