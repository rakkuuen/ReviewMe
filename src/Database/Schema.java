package Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Tables for series, templates and per-review field values, plus the extra GameReview columns.
public class Schema {

    public static void Upgrade(String url){
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            // id is the series tag without the #, e.g. Yakuza
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Series (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL UNIQUE,
                    templateId INTEGER,
                    notes TEXT
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Template (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                );
            """);

            // kind is TEXT, RATING (whole number only) or RATING_OR_UNKNOWN (number or ?)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS TemplateField (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    templateId INTEGER NOT NULL,
                    fieldKey TEXT NOT NULL,
                    heading TEXT NOT NULL,
                    displayOrder INTEGER NOT NULL,
                    kind TEXT NOT NULL,
                    required INTEGER NOT NULL DEFAULT 0,
                    UNIQUE (templateId, fieldKey)
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

            AddColumnIfMissing(conn, "GameReview", "seriesId", "TEXT");
            AddColumnIfMissing(conn, "GameReview", "templateId", "INTEGER");
            AddColumnIfMissing(conn, "GameReview", "sourcePath", "TEXT");
            AddColumnIfMissing(conn, "GameReview", "inProgress", "INTEGER NOT NULL DEFAULT 0");
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
