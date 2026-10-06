package Database.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import Model.Reviews.FieldStatus;
import Model.Reviews.FieldValue;

// The per-review values of template fields. Writes return true if the database was changed
public class ReviewFieldDao {

    // Updates keep their row position, so fields stay in the order they were first saved
    private static final String upsertSQL = """
        INSERT INTO ReviewField (reviewId, fieldKey, value, comment, status)
        VALUES (?, ?, ?, ?, ?)
        ON CONFLICT (reviewId, fieldKey) DO UPDATE SET
            value = excluded.value, comment = excluded.comment, status = excluded.status;
    """;

    public static Map<String, FieldValue> GetFieldValues(int reviewId){
        Map<String, FieldValue> values = new LinkedHashMap<>();
        String query = "SELECT fieldKey, value, comment, status FROM ReviewField WHERE reviewId = ? ORDER BY rowid;";

        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, reviewId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()){
                    values.put(rs.getString("fieldKey"), new FieldValue(rs.getString("value"), rs.getString("comment"),
                            FieldStatus.valueOf(rs.getString("status"))));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving review fields: " + e.getMessage());
        }
        return values;
    }

    public static boolean SaveFieldValue(int reviewId, String fieldKey, FieldValue value){
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(upsertSQL)) {
            Bind(pstmt, reviewId, fieldKey, value);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving review field " + fieldKey + ": " + e.getMessage());
            return false;
        }
    }

    // All or nothing. Fields not in the map are left as they are
    public static boolean SaveFieldValues(int reviewId, Map<String, FieldValue> values){
        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(upsertSQL)) {
                for(Map.Entry<String, FieldValue> entry : values.entrySet()){
                    Bind(pstmt, reviewId, entry.getKey(), entry.getValue());
                    pstmt.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error saving review fields: " + e.getMessage());
            return false;
        }
    }

    public static boolean DeleteFieldValue(int reviewId, String fieldKey){
        return Delete("DELETE FROM ReviewField WHERE reviewId = ? AND fieldKey = ?;", reviewId, fieldKey);
    }

    public static boolean DeleteFieldValues(int reviewId){
        return Delete("DELETE FROM ReviewField WHERE reviewId = ?;", reviewId, null);
    }

    private static void Bind(PreparedStatement pstmt, int reviewId, String fieldKey, FieldValue value) throws SQLException {
        pstmt.setInt(1, reviewId);
        pstmt.setString(2, fieldKey);
        pstmt.setString(3, value.GetText());
        pstmt.setString(4, value.GetComment());
        pstmt.setString(5, value.GetStatus().name());
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
            System.err.println("Error deleting review fields: " + e.getMessage());
            return false;
        }
    }
}
