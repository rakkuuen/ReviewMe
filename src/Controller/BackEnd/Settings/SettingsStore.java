package Controller.BackEnd.Settings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Reads and changes single settings in the local properties file (the Steam key and ID, the folders, the theme). A setting that is
// there is changed in place; a new one goes on the end. Comments, blank lines and every other setting are left exactly as they were
public class SettingsStore {
    public static final String defaultPath = "Resources/local.properties";
    public static final String apiKeyName = "steam.apiKey";
    public static final String steamIdName = "steam.id";
    public static final String vaultName = "vault.path";
    public static final String exportName = "export.path";
    public static final String themeName = "theme.path";

    private final Path file;

    public SettingsStore(){
        this(Paths.get(defaultPath));
    }

    public SettingsStore(Path file){
        this.file = file;
    }

    // The setting's value (trimmed), or an empty string when it isn't set or the file isn't there
    public String Get(String name){
        for(String line : Lines()){
            if(IsSetting(line, name)){
                return line.substring(line.indexOf('=') + 1).trim();
            }
        }
        return "";
    }

    // Sets a setting. Newlines in the value are not allowed
    public void Set(String name, String value) throws IOException {
        if(value.contains("\n") || value.contains("\r")){
            throw new IOException("A setting can't hold a line break");
        }
        List<String> lines = Lines();
        boolean found = false;
        for(int i = 0; i < lines.size(); i++){
            if(IsSetting(lines.get(i), name)){
                lines.set(i, name + " = " + value);
                found = true;
                break;
            }
        }
        if(!found){
            lines.add(name + " = " + value);
        }
        if(file.getParent() != null){
            Files.createDirectories(file.getParent());
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    private List<String> Lines(){
        try {
            return new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    // A line that sets this name: "name = value" or "name=value", not a comment
    private static boolean IsSetting(String line, String name){
        String trimmed = line.trim();
        if(trimmed.startsWith("#") || trimmed.startsWith("!")){
            return false;
        }
        int equals = trimmed.indexOf('=');
        return equals > 0 && trimmed.substring(0, equals).trim().equals(name);
    }
}
