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

        // Create table in database if one does not already exist
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS GameReview (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                gameplay TEXT,
                story TEXT,
                setting TEXT,
                music TEXT,
                voiceActing TEXT,
                achievements TEXT,
                replayability TEXT,
                alternateTitles TEXT,
                finalRating INTEGER,
                conclusion TEXT
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

    public static void InsertGameReview(GameReview review){
        String url = Db.url;
        String insertSQL = """
            INSERT OR IGNORE INTO GameReview (title, gameplay, story, setting, music, voiceActing, achievements,
                                    replayability, alternateTitles, finalRating, conclusion)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, review.GetTitle());
            pstmt.setString(2, review.GetGameplay());
            pstmt.setString(3, review.GetStory());
            pstmt.setString(4, review.GetSetting());
            pstmt.setString(5, review.GetMusic());
            pstmt.setString(6, review.GetVoiceActing());
            pstmt.setString(7, review.GetAchievements());
            pstmt.setString(8, review.GetReplayability());
            pstmt.setString(9, review.GetAlternateTitles());
            pstmt.setInt(10, review.GetFinalRating());
            pstmt.setString(11, review.GetConclusion());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting GameReview: " + e.getMessage());
        }
    }

    public static void InsertGameReviews(List<GameReview> reviews) {
        for (GameReview review : reviews) {
            InsertGameReview(review);
        }
    }

    public static void UpdateGameReview(GameReview review){
        String url = Db.url;
        String updateSQL = """
            UPDATE GameReview
            SET gameplay = ?, story = ?, setting = ?, music = ?, voiceActing = ?, achievements = ?,
                replayability = ?, alternateTitles = ?, finalRating = ?, conclusion = ?
            WHERE title = ?;
        """;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setString(1, review.GetGameplay());
            pstmt.setString(2, review.GetStory());
            pstmt.setString(3, review.GetSetting());
            pstmt.setString(4, review.GetMusic());
            pstmt.setString(5, review.GetVoiceActing());
            pstmt.setString(6, review.GetAchievements());
            pstmt.setString(7, review.GetReplayability());
            pstmt.setString(8, review.GetAlternateTitles());
            pstmt.setInt(9, review.GetFinalRating());
            pstmt.setString(10, review.GetConclusion());
            pstmt.setString(11, review.GetTitle());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.err.println("No GameReview found with the title: " + review.GetTitle());
            }
        } catch (SQLException e) {
            System.err.println("Error updating GameReview: " + e.getMessage());
        }
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
    // null if the insert failed. The section text lives in ReviewField now, so the old fixed columns are left empty
    public static Integer InsertImportedReview(GameReview review){
        String insertSQL = """
            INSERT INTO GameReview (title, templateId, seriesId, seriesPosition, sourcePath, steamAppId, inProgress, completed,
                                    completedOn, completedOnSource, achievementsAtCompletion, dismissedAtTotal,
                                    manualPlaytimeMinutes, topPickSlot)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
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
            Db.SetNullableInt(pstmt, 12, review.GetDismissedAtTotal());
            Db.SetNullableInt(pstmt, 13, review.GetManualPlaytimeMinutes());
            Db.SetNullableInt(pstmt, 14, review.GetTopPickSlot());
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting imported review " + review.GetTitle() + ": " + e.getMessage());
            return null;
        }
    }

    // One row to one GameReview: the old fixed sections plus the new series/Steam/state columns
    private static GameReview ReadGameReview(ResultSet rs) throws SQLException {
        GameReview review = new GameReview();
        review.SetId(rs.getInt("id"));
        review.SetTitle(rs.getString("title"));
        review.SetGameplay(rs.getString("gameplay"));
        review.SetStory(rs.getString("story"));
        review.SetSetting(rs.getString("setting"));
        review.SetMusic(rs.getString("music"));
        review.SetVoiceActing(rs.getString("voiceActing"));
        review.SetAchievements(rs.getString("achievements"));
        review.SetReplayability(rs.getString("replayability"));
        review.SetAlternateTitles(rs.getString("alternateTitles"));
        review.SetFinalRating(rs.getInt("finalRating"));
        review.SetConclusion(rs.getString("conclusion"));

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
        review.SetDismissedAtTotal(Db.GetNullableInt(rs, "dismissedAtTotal"));
        review.SetManualPlaytimeMinutes(Db.GetNullableInt(rs, "manualPlaytimeMinutes"));
        review.SetTopPickSlot(Db.GetNullableInt(rs, "topPickSlot"));
        return review;
    }
}
