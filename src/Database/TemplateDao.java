package Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import Model.Template;
import Model.TemplateField;

public class TemplateDao {

    // Template and its fields go in together or not at all. Existing ids are left untouched
    public static void InsertTemplate(String url, Template template){
        String insertTemplateSQL = "INSERT OR IGNORE INTO Template (id, name) VALUES (?, ?);";
        String insertFieldSQL = """
            INSERT OR IGNORE INTO TemplateField (templateId, fieldKey, heading, displayOrder, kind, required, placeholder)
            VALUES (?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);
            try (PreparedStatement templateStmt = conn.prepareStatement(insertTemplateSQL);
                 PreparedStatement fieldStmt = conn.prepareStatement(insertFieldSQL)) {

                templateStmt.setString(1, template.GetId());
                templateStmt.setString(2, template.GetName());
                templateStmt.executeUpdate();

                for(TemplateField field : template.GetFields()){
                    fieldStmt.setString(1, template.GetId());
                    fieldStmt.setString(2, field.GetFieldKey());
                    fieldStmt.setString(3, field.GetHeading());
                    fieldStmt.setInt(4, field.GetDisplayOrder());
                    fieldStmt.setString(5, field.GetKind().name());
                    fieldStmt.setInt(6, field.IsRequired() ? 1 : 0);
                    fieldStmt.setString(7, field.GetPlaceholder());
                    fieldStmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting Template " + template.GetId() + ": " + e.getMessage());
        }
    }
}
