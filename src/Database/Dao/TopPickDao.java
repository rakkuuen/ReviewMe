package Database.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import Controller.BackEnd.TopPicks.TopPickChange;

// Who holds each Top picks slot. The slot lives on the review (one game per slot, enforced by the database)
public class TopPickDao {

    // Slot to review id, lowest slot first
    public static Map<Integer, Integer> GetPicks(){
        Map<Integer, Integer> picks = new TreeMap<>();
        String query = "SELECT id, topPickSlot FROM GameReview WHERE topPickSlot IS NOT NULL;";
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while(rs.next()){
                picks.put(rs.getInt("topPickSlot"), rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving Top picks: " + e.getMessage());
        }
        return picks;
    }

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

    // Applies the changes together or not at all. Every slot involved is emptied first, so two games swapping never share a slot
    // for a moment. False (and nothing changed) if a review isn't found or a slot is refused
    public static boolean Apply(List<TopPickChange> changes){
        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement clear = conn.prepareStatement("UPDATE GameReview SET topPickSlot = NULL WHERE id = ?;");
                 PreparedStatement place = conn.prepareStatement("UPDATE GameReview SET topPickSlot = ? WHERE id = ?;")) {
                for(TopPickChange change : changes){
                    clear.setInt(1, change.GetReviewId());
                    ExpectOne(clear.executeUpdate(), change.GetReviewId());
                }
                for(TopPickChange change : changes){
                    if(change.GetSlot() != null){
                        place.setInt(1, change.GetSlot());
                        place.setInt(2, change.GetReviewId());
                        place.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error saving Top picks: " + e.getMessage());
            return false;
        }
    }

    private static void ExpectOne(int rows, int reviewId) throws SQLException {
        if(rows != 1){
            throw new SQLException("review " + reviewId + " not found");
        }
    }
}
