package Database.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import Model.Library.SteamGameStatus;
import Model.Reviews.CompletionSource;

// The shelved / completed-by-hand record of Steam games that have no review. Writes return true if the database was changed
public class SteamGameStatusDao {

    // Null if nothing is recorded for the game
    public static SteamGameStatus Get(int appId){
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM SteamGameStatus WHERE appId = ?;")) {
            pstmt.setInt(1, appId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? Read(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving Steam game status " + appId + ": " + e.getMessage());
            return null;
        }
    }

    public static Map<Integer, SteamGameStatus> GetAll(){
        Map<Integer, SteamGameStatus> all = new HashMap<>();
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM SteamGameStatus;");
             ResultSet rs = pstmt.executeQuery()) {
            while(rs.next()){
                SteamGameStatus status = Read(rs);
                all.put(status.GetAppId(), status);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving Steam game statuses: " + e.getMessage());
        }
        return all;
    }

    // Adds or replaces the record. Fails (false) when the Steam game isn't stored
    public static boolean Save(SteamGameStatus status){
        String sql = """
            INSERT INTO SteamGameStatus (appId, shelved, shelvedReason, completed, completedOn, completedOnSource)
            SELECT ?, ?, ?, ?, ?, ? WHERE EXISTS (SELECT 1 FROM SteamGame WHERE appId = ?)
            ON CONFLICT(appId) DO UPDATE SET shelved = excluded.shelved, shelvedReason = excluded.shelvedReason, completed = excluded.completed,
                completedOn = excluded.completedOn, completedOnSource = excluded.completedOnSource;
        """;
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, status.GetAppId());
            pstmt.setInt(2, status.IsShelved() ? 1 : 0);
            pstmt.setString(3, status.GetShelvedReason());
            pstmt.setInt(4, status.IsCompleted() ? 1 : 0);
            pstmt.setString(5, status.GetCompletedOn());
            pstmt.setString(6, status.GetCompletedOnSource() == null ? null : status.GetCompletedOnSource().name());
            pstmt.setInt(7, status.GetAppId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving Steam game status " + status.GetAppId() + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean Delete(int appId){
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM SteamGameStatus WHERE appId = ?;")) {
            pstmt.setInt(1, appId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting Steam game status " + appId + ": " + e.getMessage());
            return false;
        }
    }

    // True if some review is linked to the Steam game, so its shelved and completed flags live on that review
    public static boolean ReviewExistsFor(int appId){
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM GameReview WHERE steamAppId = ?;")) {
            pstmt.setInt(1, appId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking reviews for Steam game " + appId + ": " + e.getMessage());
            return false;
        }
    }

    private static SteamGameStatus Read(ResultSet rs) throws SQLException {
        String source = rs.getString("completedOnSource");
        return new SteamGameStatus(rs.getInt("appId"), rs.getInt("shelved") == 1, rs.getString("shelvedReason"), rs.getInt("completed") == 1,
                rs.getString("completedOn"), source == null ? null : CompletionSource.valueOf(source));
    }
}
