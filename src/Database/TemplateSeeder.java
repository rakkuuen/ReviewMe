package Database;
import java.io.IOException;

import Model.Template;
import Model.TemplateLoader;

// Loads every .template file into the DB. Safe to run on every launch (existing templates are left alone)
public class TemplateSeeder {
    private static final String templateFolder = "Resources/Templates";

    public static void Seed(String url){
        try {
            for(Template template : TemplateLoader.LoadAll(templateFolder)){
                TemplateDao.InsertTemplate(url, template);
            }
            System.out.println("Templates seeded successfully.");
        } catch (IOException e) {
            System.err.println("Error loading templates: " + e.getMessage());
        }
    }
}
