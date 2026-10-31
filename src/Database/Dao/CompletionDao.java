package Database.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import Controller.BackEnd.Steam.CompletionAction;

// Saves the four completion columns of the reviews a completion plan changes, all together or not at all. Nothing else about a review is touched
public class CompletionDao {

    // Only RECORD and FILL actions are written. If any review isn't found, none are saved
    public static boolean Apply(List<CompletionAction> actions){
        String updateSQL = "UPDATE GameReview SET completed = ?, completedOn = ?, completedOnSource = ?, achievementsAtCompletion = ? WHERE id = ?;";

        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                for(CompletionAction action : actions){
                    if(!action.ChangesReview()){
                        continue;
                    }
                    pstmt.setInt(1, action.IsCompleted() ? 1 : 0);
                    pstmt.setString(2, action.GetCompletedOn());
                    pstmt.setString(3, action.GetCompletedOnSource() == null ? null : action.GetCompletedOnSource().name());
                    Db.SetNullableInt(pstmt, 4, action.GetBaseline());
                    pstmt.setInt(5, action.GetReview().GetId());
                    if(pstmt.executeUpdate() != 1){
                        throw new SQLException("review " + action.GetReview().GetId() + " not found");
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error saving completions: " + e.getMessage());
            return false;
        }
    }
}
