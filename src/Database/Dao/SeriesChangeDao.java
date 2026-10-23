package Database.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import Model.Reviews.Series;
import Model.Reviews.SeriesChange;
import Model.Reviews.SeriesMembership;

// Saves a whole SeriesChange as one transaction: if any part fails or touches the wrong number of rows, nothing is saved
public class SeriesChangeDao {

    public static boolean Apply(SeriesChange change){
        if(change.IsEmpty()){
            return true;
        }
        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
            try {
                if(change.GetToInsert() != null){
                    InsertSeries(conn, change.GetToInsert());
                }
                if(change.GetToUpdate() != null){
                    UpdateSeries(conn, change.GetToUpdate());
                }
                for(SeriesMembership membership : change.GetMemberships()){
                    UpdateMembership(conn, membership);
                }
                if(change.GetIdToDelete() != null){
                    DeleteSeries(conn, change.GetIdToDelete());
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error saving series change: " + e.getMessage());
            return false;
        }
    }

    private static void InsertSeries(Connection conn, Series series) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Series (id, name, templateId, notes) VALUES (?, ?, ?, ?);")) {
            pstmt.setString(1, series.GetId());
            pstmt.setString(2, series.GetName());
            pstmt.setString(3, series.GetTemplateId());
            pstmt.setString(4, series.GetNotes());
            ExpectOneRow(pstmt.executeUpdate(), "add series " + series.GetId());
        }
    }

    private static void UpdateSeries(Connection conn, Series series) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE Series SET name = ?, templateId = ?, notes = ? WHERE id = ?;")) {
            pstmt.setString(1, series.GetName());
            pstmt.setString(2, series.GetTemplateId());
            pstmt.setString(3, series.GetNotes());
            pstmt.setString(4, series.GetId());
            ExpectOneRow(pstmt.executeUpdate(), "update series " + series.GetId());
        }
    }

    private static void UpdateMembership(Connection conn, SeriesMembership membership) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE GameReview SET seriesId = ?, seriesPosition = ?, templateId = ? WHERE id = ?;")) {
            pstmt.setString(1, membership.GetSeriesId());
            Db.SetNullableInt(pstmt, 2, membership.GetPosition());
            pstmt.setString(3, membership.GetTemplateId());
            pstmt.setInt(4, membership.GetReviewId());
            ExpectOneRow(pstmt.executeUpdate(), "move review " + membership.GetReviewId());
        }
    }

    private static void DeleteSeries(Connection conn, String id) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Series WHERE id = ?;")) {
            pstmt.setString(1, id);
            ExpectOneRow(pstmt.executeUpdate(), "delete series " + id);
        }
    }

    // A statement that changed no row (or several) means the change no longer matches the database, so it's abandoned
    private static void ExpectOneRow(int rows, String what) throws SQLException {
        if(rows != 1){
            throw new SQLException("couldn't " + what + " (" + rows + " rows changed)");
        }
    }
}
