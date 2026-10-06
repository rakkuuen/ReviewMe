package Database.Setup;
import java.io.IOException;

import Model.Reviews.Template;
import Model.Reviews.TemplateLoader;
import Database.Dao.TemplateDao;

// Loads every .template file into the DB. Safe to run on every launch (existing templates are left alone)
public class TemplateSeeder {
    private static final String templateFolder = "Resources/Templates";

    public static void Seed(){
        try {
            for(Template template : TemplateLoader.LoadAll(templateFolder)){
                TemplateDao.InsertTemplate(template);
            }
            System.out.println("Templates seeded successfully.");
        } catch (IOException e) {
            System.err.println("Error loading templates: " + e.getMessage());
        }
    }
}
