package Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Model.GameReview;


public class GameReviewDao {
    private static final String dbPath = "Resources/Databases/GameReview.db";

    public static void Setup(){
        // Connect to sql database (or create one if one does not exist)
        String url = "jdbc:sqlite:" + dbPath;
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
    }
    
    public static void InsertGameReview(GameReview review){
        String url = "jdbc:sqlite:" + dbPath;
        String insertSQL = """
            INSERT INTO GameReview (title, gameplay, story, setting, music, voiceActing, achievements, 
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

    public static List<GameReview> GetAllGameReviews(){
        List<GameReview> allGameReviews = new ArrayList<>();
        String url = "jdbc:sqlite:" + dbPath;
        String query = "SELECT * FROM GameReview";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                GameReview review = new GameReview();
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

                allGameReviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving GameReviews: " + e.getMessage());
        }


        return allGameReviews;
    }

    public static GameReview GetGameReview(String gameTitle){
        GameReview myGameReview = new GameReview();
        String url = "jdbc:sqlite:" + dbPath;
        String query = "SELECT * FROM GameReview WHERE title = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, gameTitle); // Set the string parameter
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Extract data from the result set
                myGameReview.SetTitle(rs.getString("title"));
                myGameReview.SetGameplay(rs.getString("gameplay"));
                myGameReview.SetStory(rs.getString("story"));
                myGameReview.SetSetting(rs.getString("setting"));
                myGameReview.SetMusic(rs.getString("music"));
                myGameReview.SetVoiceActing(rs.getString("voiceActing"));
                myGameReview.SetAchievements(rs.getString("achievements"));
                myGameReview.SetReplayability(rs.getString("replayability"));
                myGameReview.SetAlternateTitles(rs.getString("alternateTitles"));
                myGameReview.SetFinalRating(rs.getInt("finalRating"));
                myGameReview.SetConclusion(rs.getString("conclusion"));

                return myGameReview;
                
            } else {
                System.out.println("No GameReview found with the title: " + gameTitle);
                return null;
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving GameReview: " + e.getMessage());
            return null;
        }
    }
}
