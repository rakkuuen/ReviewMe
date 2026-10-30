package Database.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Model.Reviews.ManualAchievement;

// The achievements typed in by hand for a review. A review's positions are always 1 to n with no gaps, kept up by every write here.
// Writes return true if the database was changed
public class ManualAchievementDao {

    public static boolean ReviewExists(int reviewId){
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM GameReview WHERE id = ?;")) {
            pstmt.setInt(1, reviewId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking review " + reviewId + ": " + e.getMessage());
            return false;
        }
    }

    // Null if there's no such achievement
    public static ManualAchievement Get(int id){
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM ManualAchievement WHERE id = ?;")) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? Read(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving manual achievement " + id + ": " + e.getMessage());
            return null;
        }
    }

    // In list order
    public static List<ManualAchievement> GetForReview(int reviewId){
        List<ManualAchievement> found = new ArrayList<>();
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM ManualAchievement WHERE reviewId = ? ORDER BY position;")) {
            pstmt.setInt(1, reviewId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()){
                    found.add(Read(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving manual achievements: " + e.getMessage());
        }
        return found;
    }

    // Goes to the end of the review's list. Returns the new id, or null if it failed (a title already used in that review fails)
    public static Integer Insert(int reviewId, String title, String description){
        String insertSQL = "INSERT INTO ManualAchievement (reviewId, title, description, unlocked, position) VALUES (?, ?, ?, 0, (SELECT COALESCE(MAX(position), 0) + 1 FROM ManualAchievement WHERE reviewId = ?));";
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, reviewId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setInt(4, reviewId);
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error adding manual achievement: " + e.getMessage());
            return null;
        }
    }

    public static boolean UpdateText(int id, String title, String description){
        return Run("UPDATE ManualAchievement SET title = ?, description = ? WHERE id = ?;", title, description, id);
    }

    // unlockedOn is only kept while unlocked
    public static boolean SetUnlocked(int id, boolean unlocked, String unlockedOn){
        return Run("UPDATE ManualAchievement SET unlocked = ?, unlockedOn = ? WHERE id = ?;", unlocked ? 1 : 0, unlocked ? unlockedOn : null, id);
    }

    // Removes it and closes the gap it leaves
    public static boolean Delete(int id){
        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
            try {
                int reviewId;
                int position;
                try (PreparedStatement find = conn.prepareStatement("SELECT reviewId, position FROM ManualAchievement WHERE id = ?;")) {
                    find.setInt(1, id);
                    try (ResultSet rs = find.executeQuery()) {
                        if(!rs.next()){
                            conn.rollback();
                            return false;
                        }
                        reviewId = rs.getInt("reviewId");
                        position = rs.getInt("position");
                    }
                }
                try (PreparedStatement delete = conn.prepareStatement("DELETE FROM ManualAchievement WHERE id = ?;");
                     PreparedStatement close = conn.prepareStatement("UPDATE ManualAchievement SET position = position - 1 WHERE reviewId = ? AND position > ?;")) {
                    delete.setInt(1, id);
                    delete.executeUpdate();
                    close.setInt(1, reviewId);
                    close.setInt(2, position);
                    close.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting manual achievement " + id + ": " + e.getMessage());
            return false;
        }
    }

    // ids must be exactly the review's achievements in the new order. All or nothing
    public static boolean Reorder(int reviewId, List<Integer> idsInOrder){
        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE ManualAchievement SET position = ? WHERE id = ? AND reviewId = ?;")) {
                for(int i = 0; i < idsInOrder.size(); i++){
                    pstmt.setInt(1, i + 1);
                    pstmt.setInt(2, idsInOrder.get(i));
                    pstmt.setInt(3, reviewId);
                    if(pstmt.executeUpdate() != 1){
                        throw new SQLException("achievement " + idsInOrder.get(i) + " isn't in review " + reviewId);
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error reordering manual achievements: " + e.getMessage());
            return false;
        }
    }

    private static ManualAchievement Read(ResultSet rs) throws SQLException {
        return new ManualAchievement(rs.getInt("id"), rs.getInt("reviewId"), rs.getString("title"), rs.getString("description"),
                rs.getInt("unlocked") == 1, rs.getString("unlockedOn"), rs.getInt("position"));
    }

    private static boolean Run(String sql, Object... values){
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for(int i = 0; i < values.length; i++){
                pstmt.setObject(i + 1, values[i]);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving manual achievement: " + e.getMessage());
            return false;
        }
    }
}
