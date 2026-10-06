package Database.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import Model.Reviews.TagKind;

// Genre and metadata tags per review. Tags come back in the order they were saved
public class ReviewTagDao {

    public static List<String> GetTags(int reviewId, TagKind kind){
        List<String> tags = new ArrayList<>();
        String query = "SELECT tag FROM ReviewTag WHERE reviewId = ? AND kind = ? ORDER BY rowid;";

        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, reviewId);
            pstmt.setString(2, kind.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()){
                    tags.add(rs.getString("tag"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving tags: " + e.getMessage());
        }
        return tags;
    }

    // Swaps this kind's tags for the new list (duplicates dropped). All or nothing
    public static boolean ReplaceTags(int reviewId, TagKind kind, List<String> tags){
        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM ReviewTag WHERE reviewId = ? AND kind = ?;");
                 PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO ReviewTag (reviewId, tag, kind) VALUES (?, ?, ?);")) {

                deleteStmt.setInt(1, reviewId);
                deleteStmt.setString(2, kind.name());
                deleteStmt.executeUpdate();

                for(String tag : new LinkedHashSet<>(tags)){
                    insertStmt.setInt(1, reviewId);
                    insertStmt.setString(2, tag);
                    insertStmt.setString(3, kind.name());
                    insertStmt.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error saving tags: " + e.getMessage());
            return false;
        }
    }
}
