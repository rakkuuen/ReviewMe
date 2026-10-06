package Database.Dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

// The one place that knows where the database is, plus null-safe column reads
public class Db {
    public static final String url = "jdbc:sqlite:Resources/Databases/GameReview.db";

    public static Connection OpenConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    // rs.getInt and friends return 0 for NULL, these keep the NULL
    public static Integer GetNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    public static Long GetNullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    public static Double GetNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }
}
