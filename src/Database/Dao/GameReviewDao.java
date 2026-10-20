package Database.Dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Model.Reviews.CompletionSource;
import Model.Reviews.GameReview;
import Database.Setup.Schema;
import Database.Setup.TemplateSeeder;


public class GameReviewDao {

    public static void Setup(){
        // Connect to sql database (or create one if one does not exist)
        String url = Db.url;
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Database created or opened successfully.");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Create table in database if one does not already exist. Everything else about a review (sections, series, Steam
        // link, state) is added by Schema.Upgrade
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS GameReview (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT
            );
        """;

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("GameReview table created successfully.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Enforce one row per title (a title is a game)
        String createUniqueIndexSQL = "CREATE UNIQUE INDEX IF NOT EXISTS idx_gamereview_title ON GameReview(title);";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUniqueIndexSQL);
        } catch (SQLException e) {
            System.err.println("Error creating unique title index: " + e.getMessage());
        }
        // Adding the schema table for series/templates/per review stuff
        Schema.Upgrade();
        TemplateSeeder.Seed();
    }

    public static List<GameReview> GetAllGameReviews(){
        List<GameReview> allGameReviews = new ArrayList<>();
        String url = Db.url;
        String query = "SELECT * FROM GameReview";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                allGameReviews.add(ReadGameReview(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving GameReviews: " + e.getMessage());
        }


        return allGameReviews;
    }

    public static GameReview GetGameReview(String gameTitle){
        String url = Db.url;
        String query = "SELECT * FROM GameReview WHERE title = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, gameTitle); // Set the string parameter
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return ReadGameReview(rs);
            } else {
                System.out.println("No GameReview found with the title: " + gameTitle);
                return null;
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving GameReview: " + e.getMessage());
            return null;
        }
    }

    // Adds a review exactly as imported, no silent skip if the title exists (that is an error). Returns the new id, or
    // null if the insert failed. The sections live in ReviewField, not here
    public static Integer InsertImportedReview(GameReview review){
        String insertSQL = """
            INSERT INTO GameReview (title, templateId, seriesId, seriesPosition, sourcePath, steamAppId, inProgress, completed,
                                    completedOn, completedOnSource, achievementsAtCompletion, manualPlaytimeMinutes,
                                    topPickSlot, reviewIncomplete, shelved, shelvedReason, addedToLibraryOn)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DriverManager.getConnection(Db.url);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, review.GetTitle());
            pstmt.setString(2, review.GetTemplateId());
            pstmt.setString(3, review.GetSeriesId());
            Db.SetNullableInt(pstmt, 4, review.GetSeriesPosition());
            pstmt.setString(5, review.GetSourcePath());
            Db.SetNullableInt(pstmt, 6, review.GetSteamAppId());
            pstmt.setInt(7, review.IsInProgress() ? 1 : 0);
            pstmt.setInt(8, review.IsCompleted() ? 1 : 0);
            pstmt.setString(9, review.GetCompletedOn());
            pstmt.setString(10, review.GetCompletedOnSource() == null ? null : review.GetCompletedOnSource().name());
            Db.SetNullableInt(pstmt, 11, review.GetAchievementsAtCompletion());
            Db.SetNullableInt(pstmt, 12, review.GetManualPlaytimeMinutes());
            Db.SetNullableInt(pstmt, 13, review.GetTopPickSlot());
            pstmt.setInt(14, review.IsReviewIncompleteFlagged() ? 1 : 0);
            pstmt.setInt(15, review.IsShelved() ? 1 : 0);
            pstmt.setString(16, review.GetShelvedReason());
            pstmt.setString(17, review.GetAddedToLibraryOn());
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting imported review " + review.GetTitle() + ": " + e.getMessage());
            return null;
        }
    }

    // Saves the review's own columns (series, Steam link, state, completion, top pick, manual flags, shelving, added date). Not its title, template or source file,
    // and not its sections, which live in ReviewField. False if it has no id, no such row, or the top-pick slot is taken
    public static boolean UpdateReviewDetails(GameReview review){
        if(review.GetId() == null){
            System.err.println("Error updating review details: " + review.GetTitle() + " has no id");
            return false;
        }

        String updateSQL = """
            UPDATE GameReview
            SET seriesId = ?, seriesPosition = ?, steamAppId = ?, inProgress = ?, completed = ?, completedOn = ?,
                completedOnSource = ?, achievementsAtCompletion = ?, manualPlaytimeMinutes = ?, topPickSlot = ?,
                reviewIncomplete = ?, shelved = ?, shelvedReason = ?, addedToLibraryOn = ?
            WHERE id = ?;
        """;
        try (Connection conn = DriverManager.getConnection(Db.url);
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setString(1, review.GetSeriesId());
            Db.SetNullableInt(pstmt, 2, review.GetSeriesPosition());
            Db.SetNullableInt(pstmt, 3, review.GetSteamAppId());
            pstmt.setInt(4, review.IsInProgress() ? 1 : 0);
            pstmt.setInt(5, review.IsCompleted() ? 1 : 0);
            pstmt.setString(6, review.GetCompletedOn());
            pstmt.setString(7, review.GetCompletedOnSource() == null ? null : review.GetCompletedOnSource().name());
            Db.SetNullableInt(pstmt, 8, review.GetAchievementsAtCompletion());
            Db.SetNullableInt(pstmt, 9, review.GetManualPlaytimeMinutes());
            Db.SetNullableInt(pstmt, 10, review.GetTopPickSlot());
            pstmt.setInt(11, review.IsReviewIncompleteFlagged() ? 1 : 0);
            pstmt.setInt(12, review.IsShelved() ? 1 : 0);
            pstmt.setString(13, review.GetShelvedReason());
            pstmt.setString(14, review.GetAddedToLibraryOn());
            pstmt.setInt(15, review.GetId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating review details for " + review.GetTitle() + ": " + e.getMessage());
            return false;
        }
    }

    // One row to one GameReview: its own columns only. Sections, groups and tags are attached by ReviewLoader
    private static GameReview ReadGameReview(ResultSet rs) throws SQLException {
        GameReview review = new GameReview();
        review.SetId(rs.getInt("id"));
        review.SetTitle(rs.getString("title"));
        review.SetTemplateId(rs.getString("templateId"));
        review.SetSeriesId(rs.getString("seriesId"));
        review.SetSeriesPosition(Db.GetNullableInt(rs, "seriesPosition"));
        review.SetSourcePath(rs.getString("sourcePath"));
        review.SetSteamAppId(Db.GetNullableInt(rs, "steamAppId"));
        review.SetInProgress(rs.getInt("inProgress") == 1);
        review.SetCompleted(rs.getInt("completed") == 1);
        review.SetCompletedOn(rs.getString("completedOn"));
        String completedOnSource = rs.getString("completedOnSource");
        review.SetCompletedOnSource(completedOnSource == null ? null : CompletionSource.valueOf(completedOnSource));
        review.SetAchievementsAtCompletion(Db.GetNullableInt(rs, "achievementsAtCompletion"));
        review.SetManualPlaytimeMinutes(Db.GetNullableInt(rs, "manualPlaytimeMinutes"));
        review.SetTopPickSlot(Db.GetNullableInt(rs, "topPickSlot"));
        review.SetReviewIncompleteFlagged(rs.getInt("reviewIncomplete") == 1);
        review.SetShelved(rs.getInt("shelved") == 1);
        review.SetShelvedReason(rs.getString("shelvedReason"));
        review.SetAddedToLibraryOn(rs.getString("addedToLibraryOn"));
        return review;
    }
}
