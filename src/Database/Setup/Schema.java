package Database.Setup;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Tables for series, templates, per-review field values and Steam data, plus the extra GameReview columns.
public class Schema {

    public static void Upgrade(String url){
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            // id is the series tag without the #, e.g. Yakuza
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Series (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL UNIQUE,
                    templateId TEXT,
                    notes TEXT
                );
            """);

            // id is a readable key, e.g. Standard or Yakuza
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Template (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL UNIQUE
                );
            """);

            // kind is TEXT or RATING_OR_UNKNOWN (whole number or ?)
            // required means it counts as missing even when the review has no entry for it
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS TemplateField (
                    templateId TEXT NOT NULL,
                    fieldKey TEXT NOT NULL,
                    heading TEXT NOT NULL,
                    displayOrder INTEGER NOT NULL,
                    kind TEXT NOT NULL,
                    required INTEGER NOT NULL DEFAULT 0,
                    placeholder TEXT,
                    PRIMARY KEY (templateId, fieldKey)
                );
            """);

            // status is FILLED or NA; a missing section is an empty value, not a status
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ReviewField (
                    reviewId INTEGER NOT NULL,
                    fieldKey TEXT NOT NULL,
                    value TEXT,
                    status TEXT NOT NULL DEFAULT 'FILLED',
                    PRIMARY KEY (reviewId, fieldKey)
                );
            """);

            // kind is GENRE or METADATA
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ReviewTag (
                    reviewId INTEGER NOT NULL,
                    tag TEXT NOT NULL,
                    kind TEXT NOT NULL,
                    PRIMARY KEY (reviewId, tag)
                );
            """);

            // Mirror of the Steam API, safe for a sync to overwrite. Timestamps are Steam's raw epoch seconds
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS SteamGame (
                    appId INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    playtimeMinutes INTEGER,
                    lastPlayed INTEGER,
                    iconHash TEXT,
                    totalAchievements INTEGER,
                    unlockedAchievements INTEGER,
                    syncedAt INTEGER
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Achievement (
                    appId INTEGER NOT NULL,
                    apiName TEXT NOT NULL,
                    title TEXT,
                    description TEXT,
                    hidden INTEGER NOT NULL DEFAULT 0,
                    iconUrl TEXT,
                    iconGrayUrl TEXT,
                    globalPercent REAL,
                    unlocked INTEGER NOT NULL DEFAULT 0,
                    unlockedAt INTEGER,
                    PRIMARY KEY (appId, apiName)
                );
            """);

            AddColumnIfMissing(conn, "GameReview", "seriesId", "TEXT");
            AddColumnIfMissing(conn, "GameReview", "templateId", "TEXT");
            AddColumnIfMissing(conn, "GameReview", "sourcePath", "TEXT");
            // Links to SteamGame.appId; NULL for non-Steam games
            AddColumnIfMissing(conn, "GameReview", "steamAppId", "INTEGER");
            AddColumnIfMissing(conn, "GameReview", "seriesPosition", "INTEGER");
            AddColumnIfMissing(conn, "GameReview", "inProgress", "INTEGER NOT NULL DEFAULT 0");
            // completed is for games Steam can't tell us about (no achievements, non-Steam)
            AddColumnIfMissing(conn, "GameReview", "completed", "INTEGER NOT NULL DEFAULT 0");
            // completedOn is a date like 2025-03-02; completedOnSource is STEAM or MANUAL
            AddColumnIfMissing(conn, "GameReview", "completedOn", "TEXT");
            AddColumnIfMissing(conn, "GameReview", "completedOnSource", "TEXT");
            // Achievement totals when completed / when a not-100% suggestion was dismissed
            AddColumnIfMissing(conn, "GameReview", "achievementsAtCompletion", "INTEGER");
            AddColumnIfMissing(conn, "GameReview", "dismissedAtTotal", "INTEGER");
            AddColumnIfMissing(conn, "GameReview", "manualPlaytimeMinutes", "INTEGER");
            AddColumnIfMissing(conn, "GameReview", "topPickSlot", "INTEGER CHECK (topPickSlot BETWEEN 1 AND 5)");

            // One game per Top Picks slot
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_gamereview_toppick ON GameReview(topPickSlot) WHERE topPickSlot IS NOT NULL;");

            System.out.println("Schema upgraded successfully.");
        } catch (SQLException e) {
            System.err.println("Error upgrading schema: " + e.getMessage());
        }
    }

    private static void AddColumnIfMissing(Connection conn, String table, String column, String definition) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table, column)) {
            if(rs.next()){
                return;
            }
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }
}
