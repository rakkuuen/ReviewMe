package Database.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Model.Reviews.FieldKind;
import Model.Reviews.Template;
import Model.Reviews.TemplateField;

public class TemplateDao {

    // Template and its fields go in together or not at all. A template that already exists is left completely
    // alone (no fields added or changed), since new fields could clash with the display order already stored
    public static void InsertTemplate(Template template){
        String insertTemplateSQL = "INSERT OR IGNORE INTO Template (id, name) VALUES (?, ?);";
        String insertFieldSQL = """
            INSERT INTO TemplateField (templateId, fieldKey, heading, displayOrder, kind, required, placeholder, subsections)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = Db.OpenConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement templateStmt = conn.prepareStatement(insertTemplateSQL);
                 PreparedStatement fieldStmt = conn.prepareStatement(insertFieldSQL)) {

                templateStmt.setString(1, template.GetId());
                templateStmt.setString(2, template.GetName());
                boolean isNew = templateStmt.executeUpdate() > 0;

                if(isNew){
                    for(TemplateField field : template.GetFields()){
                        fieldStmt.setString(1, template.GetId());
                        fieldStmt.setString(2, field.GetFieldKey());
                        fieldStmt.setString(3, field.GetHeading());
                        fieldStmt.setInt(4, field.GetDisplayOrder());
                        fieldStmt.setString(5, field.GetKind().name());
                        fieldStmt.setInt(6, field.IsRequired() ? 1 : 0);
                        fieldStmt.setString(7, field.GetPlaceholder());
                        fieldStmt.setInt(8, field.AllowsSubsections() ? 1 : 0);
                        fieldStmt.executeUpdate();
                    }
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

    // Null if there's no template with that id
    public static Template GetTemplate(String id){
        List<Template> found = QueryTemplates(id);
        return found.isEmpty() ? null : found.get(0);
    }

    public static List<Template> GetAllTemplates(){
        return QueryTemplates(null);
    }

    // Fields come back in display order. id null means every template
    private static List<Template> QueryTemplates(String id){
        String query = """
            SELECT t.id AS templateId, t.name AS templateName,
                   f.fieldKey, f.heading, f.displayOrder, f.kind, f.required, f.placeholder, f.subsections
            FROM Template t
            LEFT JOIN TemplateField f ON f.templateId = t.id
        """ + (id == null ? "" : " WHERE t.id = ?") + " ORDER BY t.id, f.displayOrder;";

        Map<String, Template> templates = new LinkedHashMap<>();
        try (Connection conn = Db.OpenConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if(id != null){
                pstmt.setString(1, id);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()){
                    String templateId = rs.getString("templateId");
                    Template template = templates.get(templateId);
                    if(template == null){
                        template = new Template(templateId, rs.getString("templateName"));
                        templates.put(templateId, template);
                    }

                    String fieldKey = rs.getString("fieldKey");
                    if(fieldKey != null){
                        template.AddField(new TemplateField(fieldKey, rs.getString("heading"), rs.getString("placeholder"),
                                FieldKind.valueOf(rs.getString("kind")), rs.getInt("required") == 1, rs.getInt("displayOrder"),
                                rs.getInt("subsections") == 1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving Templates: " + e.getMessage());
        }
        return new ArrayList<>(templates.values());
    }
}
