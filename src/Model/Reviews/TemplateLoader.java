package Model.Reviews;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Builds Templates from .template files. The file name is the template id
public class TemplateLoader {
    private static final String extension = ".template";

    public static List<Template> LoadAll(String folderPath) throws IOException {
        File[] files = new File(folderPath).listFiles((dir, fileName) -> fileName.endsWith(extension));
        if(files == null){
            throw new IOException("Template folder not found: " + folderPath);
        }
        Arrays.sort(files);

        List<Template> templates = new ArrayList<>();
        for(File file : files){
            templates.add(Load(file.getPath()));
        }
        return templates;
    }

    public static Template Load(String filePath) throws IOException {
        String fileName = new File(filePath).getName();
        String id = fileName.substring(0, fileName.length() - extension.length());

        List<String> lines = Files.readAllLines(new File(filePath).toPath(), StandardCharsets.UTF_8);
        String name = null;
        List<TemplateField> fields = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        for(int i = 0; i < lines.size(); i++){
            String line = lines.get(i).trim();
            if(line.isEmpty() || line.startsWith("#")){
                continue;
            }
            if(line.startsWith("name =")){
                name = line.substring("name =".length()).trim();
                continue;
            }

            TemplateField field = ParseField(line, fields.size() + 1, filePath, i + 1);
            if(!seenKeys.add(field.GetFieldKey())){
                throw new IOException(filePath + " line " + (i + 1) + ": duplicate field key " + field.GetFieldKey());
            }
            fields.add(field);
        }

        if(name == null){
            throw new IOException(filePath + ": missing 'name =' line");
        }

        Template template = new Template(id, name);
        for(TemplateField field : fields){
            template.AddField(field);
        }
        return template;
    }

    // fieldKey | Heading | placeholder (optional) | flags (optional)
    private static TemplateField ParseField(String line, int displayOrder, String filePath, int lineNumber) throws IOException {
        String[] parts = line.split("\\|", -1);
        if(parts.length < 2){
            throw new IOException(filePath + " line " + lineNumber + ": expected 'fieldKey | Heading | ...'");
        }

        String fieldKey = parts[0].trim();
        String heading = parts[1].trim();
        String placeholder = parts.length > 2 && !parts[2].trim().isEmpty() ? parts[2].trim() : null;

        FieldKind kind = FieldKind.TEXT;
        boolean required = false, allowsSubsections = false;
        if(parts.length > 3){
            for(String flag : parts[3].split(",")){
                switch(flag.trim().toLowerCase()){
                    case "rating-or-unknown": kind = FieldKind.RATING_OR_UNKNOWN; break;
                    case "required": required = true; break;
                    case "subsections": allowsSubsections = true; break;
                    case "": break;
                    default: throw new IOException(filePath + " line " + lineNumber + ": unknown flag '" + flag.trim() + "'");
                }
            }
        }

        return new TemplateField(fieldKey, heading, placeholder, kind, required, displayOrder, allowsSubsections);
    }
}
