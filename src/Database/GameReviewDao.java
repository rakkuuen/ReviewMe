package Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import Model.GameReview;


public class GameReviewDao {
    private static final String dbPath = "/Resources/Databases/GameReview.db";

    public static void Setup(){
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Database created or opened successfully.");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Create table in database
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS GameReview (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                gameplay TEXT,
                story TEXT,
                setting TEXT,
                music TEXT,
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
    }
    
    public static void InsertGameReview(GameReview review, String dbPath){
        String url = "jdbc:sqlite:" + dbPath;
        String insertSQL = """
            INSERT INTO GameReview (gameplay, story, setting, music, achievements, 
                                    replayability, alternateTitles, finalRating, conclusion) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, review.GetGameplay());
            pstmt.setString(2, review.GetStory());
            pstmt.setString(3, review.GetSetting());
            pstmt.setString(4, review.GetMusic());
            pstmt.setString(5, review.GetAchievements());
            pstmt.setString(6, review.GetReplayability());
            pstmt.setString(7, review.GetAlternateTitles());
            pstmt.setInt(8, review.GetFinalRating());
            pstmt.setString(9, review.GetConclusion());

            pstmt.executeUpdate();
            System.out.println("GameReview inserted successfully.");
        } catch (SQLException e) {
            System.err.println("Error inserting GameReview: " + e.getMessage());
        }
    }

    public static void InsertGameReviews(List<GameReview> reviews, String dbPath) {
        for (GameReview review : reviews) {
            InsertGameReview(review, dbPath);
        }
    }
}
