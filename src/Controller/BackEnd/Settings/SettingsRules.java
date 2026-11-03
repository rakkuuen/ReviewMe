package Controller.BackEnd.Settings;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

// What a setting may be, checked before it is saved. Each check gives the problem in words, or null when it is fine
public class SettingsRules {
    private static final int steamIdLength = 17;
    private static final char maskChar = (char) 0x2022;
    private static final int maskedLength = 12;

    // A Steam Web API key: one word of letters and digits (Steam's are 32 characters; any length is accepted so a changed format still works)
    public static String CheckApiKey(String key){
        String value = key == null ? "" : key.trim();
        if(value.isEmpty()){
            return "Paste your Steam API key";
        }
        for(int i = 0; i < value.length(); i++){
            if(!Character.isLetterOrDigit(value.charAt(i))){
                return "A Steam API key is only letters and numbers";
            }
        }
        return null;
    }

    // The 17-digit number that identifies a Steam profile
    public static String CheckSteamId(String id){
        String value = id == null ? "" : id.trim();
        if(value.length() != steamIdLength){
            return "A SteamID is " + steamIdLength + " digits";
        }
        for(int i = 0; i < value.length(); i++){
            if(!Character.isDigit(value.charAt(i))){
                return "A SteamID is " + steamIdLength + " digits";
            }
        }
        return null;
    }

    // A folder that exists (a game's reviews live in the vault; exports are saved in the export folder)
    public static String CheckFolder(String path){
        String value = path == null ? "" : path.trim();
        if(value.isEmpty()){
            return "Choose a folder";
        }
        try {
            if(!Files.isDirectory(Paths.get(value))){
                return "That folder doesn't exist";
            }
        } catch (InvalidPathException e) {
            return "That isn't a folder path";
        }
        return null;
    }

    // The key hidden behind dots (always the same length, so it doesn't give its size away), or a note when there is none
    public static String Masked(String key){
        if(key == null || key.isEmpty()){
            return "Not set";
        }
        return String.valueOf(maskChar).repeat(maskedLength);
    }

    // Paths are stored with forward slashes
    public static String TidyPath(String path){
        return path.trim().replace('\\', '/');
    }
}
