package Database.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import Model.Reviews.SteamLinkChange;

// Saves title and Steam-link changes for reviews, all together or not at all. Nothing else about a review is touched
public class ReviewLinkDao {

    // If any change fails or doesn't hit exactly one review, none of them are saved
    public static boolean Apply(List<SteamLinkChange> changes){
        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
            try {
                for(SteamLinkChange change : changes){
                    if(change.GetNewTitle() != null){
                        SetTitle(conn, change);
                    }
                    if(change.SetsLink()){
                        SetLink(conn, change);
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error saving review links: " + e.getMessage());
            return false;
        }
    }

    private static void SetTitle(Connection conn, SteamLinkChange change) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE GameReview SET title = ? WHERE id = ?;")) {
            pstmt.setString(1, change.GetNewTitle());
            pstmt.setInt(2, change.GetReviewId());
            ExpectOneRow(pstmt.executeUpdate(), "rename review " + change.GetReviewId());
        }
    }

    private static void SetLink(Connection conn, SteamLinkChange change) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE GameReview SET steamAppId = ? WHERE id = ?;")) {
            Db.SetNullableInt(pstmt, 1, change.GetSteamAppId());
            pstmt.setInt(2, change.GetReviewId());
            ExpectOneRow(pstmt.executeUpdate(), "link review " + change.GetReviewId());
        }
    }

    private static void ExpectOneRow(int rows, String what) throws SQLException {
        if(rows != 1){
            throw new SQLException("couldn't " + what + " (" + rows + " rows changed)");
        }
    }
}
