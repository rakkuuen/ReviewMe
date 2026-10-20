package Database.Setup;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Database.Dao.Db;

// Tables for series, templates, per-review field values and Steam data, plus the extra GameReview columns.
public class Schema {
    // The sections used to be fixed columns on GameReview (they live in ReviewField now). dismissedAtTotal went when Dismiss was dropped
    private static final String[] retiredColumns = {"gameplay", "story", "setting", "music", "voiceActing", "achievements",
            "replayability", "alternateTitles", "finalRating", "conclusion", "dismissedAtTotal"};

    public static void Upgrade(){
        try (Connection conn = Db.OpenConnection();
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
                    comment TEXT,
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

            // Groups and characters inside a field (Voice Acting > Main cast > Tara). Position is the key because names can
            // repeat. characterPosition 0 is the group itself, 1 and up are the characters inside it
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ReviewSubsection (
                    reviewId INTEGER NOT NULL,
                    fieldKey TEXT NOT NULL,
                    groupPosition INTEGER NOT NULL,
                    characterPosition INTEGER NOT NULL DEFAULT 0,
                    name TEXT NOT NULL,
                    text TEXT,
                    PRIMARY KEY (reviewId, fieldKey, groupPosition, characterPosition)
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
                    syncedAt INTEGER,
                    firstSeenAt INTEGER
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

            // comment is the note under a rating (Replay-ability); added for DBs made before it existed
            AddColumnIfMissing(conn, "ReviewField", "comment", "TEXT");

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
            // The game's achievement total when it was completed, so later additions can be noticed
            AddColumnIfMissing(conn, "GameReview", "achievementsAtCompletion", "INTEGER");
            AddColumnIfMissing(conn, "GameReview", "manualPlaytimeMinutes", "INTEGER");
            // Shelving is set by hand, overrides every other game state, and has an optional reason
            AddColumnIfMissing(conn, "GameReview", "shelved", "INTEGER NOT NULL DEFAULT 0");
            AddColumnIfMissing(conn, "GameReview", "shelvedReason", "TEXT");
            // Steam doesn't say when a game was added, so this is a date typed in by hand (like 2025-03-02); otherwise SteamGame.firstSeenAt is used
            AddColumnIfMissing(conn, "GameReview", "addedToLibraryOn", "TEXT");
            // Set by hand when a review is unfinished though nothing is blank (the game's own state is worked out, not stored)
            AddColumnIfMissing(conn, "GameReview", "reviewIncomplete", "INTEGER NOT NULL DEFAULT 0");
            AddColumnIfMissing(conn, "GameReview", "topPickSlot", "INTEGER CHECK (topPickSlot BETWEEN 1 AND 5)");

            // One game per Top Picks slot
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_gamereview_toppick ON GameReview(topPickSlot) WHERE topPickSlot IS NOT NULL;");

            // The day ReviewMe first saw a game in a sync, set once and never overwritten (epoch seconds, like the other Steam times)
            AddColumnIfMissing(conn, "SteamGame", "firstSeenAt", "INTEGER");

            // Columns that no longer exist, so databases made before lose them
            for(String column : retiredColumns){
                DropColumnIfPresent(conn, "GameReview", column);
            }

            System.out.println("Schema upgraded successfully.");
        } catch (SQLException e) {
            System.err.println("Error upgrading schema: " + e.getMessage());
        }
    }

    private static void DropColumnIfPresent(Connection conn, String table, String column) throws SQLException {
        boolean present;
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table, column)) {
            present = rs.next();
        }
        if(present){
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE " + table + " DROP COLUMN " + column);
            }
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
