package Database.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Model.Reviews.Subsection;

// The groups and characters inside a field. Two levels only: a group holds characters, a character holds nothing.
// Writes return true if the database was changed
public class ReviewSubsectionDao {

    // In file order. Empty if the field has none
    public static List<Subsection> GetSubsections(int reviewId, String fieldKey){
        List<Subsection> found = GetAllSubsections(reviewId).get(fieldKey);
        return found == null ? new ArrayList<>() : found;
    }

    // Every field of one review that has subsections, keyed by field key
    public static Map<String, List<Subsection>> GetAllSubsections(int reviewId){
        Map<String, List<Subsection>> byField = new LinkedHashMap<>();
        String query = """
            SELECT fieldKey, groupPosition, characterPosition, name, text
            FROM ReviewSubsection WHERE reviewId = ?
            ORDER BY fieldKey, groupPosition, characterPosition;
        """;

        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, reviewId);
            try (ResultSet rs = pstmt.executeQuery()) {
                Subsection currentGroup = null;
                while(rs.next()){
                    Subsection row = new Subsection(rs.getString("name"), rs.getString("text"));
                    List<Subsection> groups = byField.computeIfAbsent(rs.getString("fieldKey"), key -> new ArrayList<>());
                    if(rs.getInt("characterPosition") == 0){
                        groups.add(row);
                        currentGroup = row;
                    } else {
                        currentGroup.AddChild(row);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving subsections: " + e.getMessage());
        }
        return byField;
    }

    // Swaps this field's groups and characters for the new list. All or nothing. Nested deeper than a character is refused
    public static boolean ReplaceSubsections(int reviewId, String fieldKey, List<Subsection> groups){
        if(!FitsTwoLevels(groups)){
            System.err.println("Error saving subsections for " + fieldKey + ": more than two levels (group > character)");
            return false;
        }

        String insertSQL = """
            INSERT INTO ReviewSubsection (reviewId, fieldKey, groupPosition, characterPosition, name, text)
            VALUES (?, ?, ?, ?, ?, ?);
        """;
        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM ReviewSubsection WHERE reviewId = ? AND fieldKey = ?;");
                 PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {

                deleteStmt.setInt(1, reviewId);
                deleteStmt.setString(2, fieldKey);
                deleteStmt.executeUpdate();

                for(int g = 0; g < groups.size(); g++){
                    InsertRow(insertStmt, reviewId, fieldKey, g + 1, 0, groups.get(g));
                    List<Subsection> characters = groups.get(g).GetChildren();
                    for(int c = 0; c < characters.size(); c++){
                        InsertRow(insertStmt, reviewId, fieldKey, g + 1, c + 1, characters.get(c));
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error saving subsections for " + fieldKey + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean DeleteSubsections(int reviewId, String fieldKey){
        return Delete("DELETE FROM ReviewSubsection WHERE reviewId = ? AND fieldKey = ?;", reviewId, fieldKey);
    }

    public static boolean DeleteAllSubsections(int reviewId){
        return Delete("DELETE FROM ReviewSubsection WHERE reviewId = ?;", reviewId, null);
    }

    // A group may hold characters, but a character may not hold anything
    private static boolean FitsTwoLevels(List<Subsection> groups){
        for(Subsection group : groups){
            for(Subsection character : group.GetChildren()){
                if(!character.GetChildren().isEmpty()){
                    return false;
                }
            }
        }
        return true;
    }

    private static void InsertRow(PreparedStatement stmt, int reviewId, String fieldKey, int groupPosition, int characterPosition,
                                  Subsection row) throws SQLException {
        stmt.setInt(1, reviewId);
        stmt.setString(2, fieldKey);
        stmt.setInt(3, groupPosition);
        stmt.setInt(4, characterPosition);
        stmt.setString(5, row.GetName());
        stmt.setString(6, row.GetText());
        stmt.executeUpdate();
    }

    private static boolean Delete(String sql, int reviewId, String fieldKey){
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reviewId);
            if(fieldKey != null){
                pstmt.setString(2, fieldKey);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting subsections: " + e.getMessage());
            return false;
        }
    }
}
