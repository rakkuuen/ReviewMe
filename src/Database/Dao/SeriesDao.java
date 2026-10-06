package Database.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Model.Reviews.Series;

// Writes return true if the database was changed (an existing id or name makes an insert return false)
public class SeriesDao {

    public static boolean InsertSeries(Series series){
        String insertSQL = "INSERT INTO Series (id, name, templateId, notes) VALUES (?, ?, ?, ?);";

        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, series.GetId());
            pstmt.setString(2, series.GetName());
            pstmt.setString(3, series.GetTemplateId());
            pstmt.setString(4, series.GetNotes());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting Series " + series.GetId() + ": " + e.getMessage());
            return false;
        }
    }

    // The id never changes, only name, template and notes
    public static boolean UpdateSeries(Series series){
        String updateSQL = "UPDATE Series SET name = ?, templateId = ?, notes = ? WHERE id = ?;";

        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setString(1, series.GetName());
            pstmt.setString(2, series.GetTemplateId());
            pstmt.setString(3, series.GetNotes());
            pstmt.setString(4, series.GetId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating Series " + series.GetId() + ": " + e.getMessage());
            return false;
        }
    }

    // Null if there's no series with that id
    public static Series GetSeries(String id){
        String query = "SELECT id, name, templateId, notes FROM Series WHERE id = ?;";

        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? ReadSeries(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving Series: " + e.getMessage());
            return null;
        }
    }

    public static List<Series> GetAllSeries(){
        List<Series> allSeries = new ArrayList<>();
        String query = "SELECT id, name, templateId, notes FROM Series ORDER BY name;";

        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while(rs.next()){
                allSeries.add(ReadSeries(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving Series: " + e.getMessage());
        }
        return allSeries;
    }

    private static Series ReadSeries(ResultSet rs) throws SQLException {
        return new Series(rs.getString("id"), rs.getString("name"), rs.getString("templateId"), rs.getString("notes"));
    }
}
