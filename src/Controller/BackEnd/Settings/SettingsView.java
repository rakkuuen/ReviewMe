package Controller.BackEnd.Settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

// Everything the Settings screen shows, read from the settings file, the themes folder and the backups folder. Immutable: it is read
// again after any change
public class SettingsView {
    private static final String themeSuffix = ".properties";
    private static final String layoutFile = "layout.properties";
    private static final String baseThemeFile = "default.properties";
    private static final String baseThemeName = "Base";

    // One theme to choose between: the name shown and the file it comes from. selected is the one in use
    public static class ThemeChoice {
        private final String name;
        private final String path;
        private final boolean selected;

        private ThemeChoice(String name, String path, boolean selected){
            this.name = name;
            this.path = path;
            this.selected = selected;
        }

        public String GetName(){ return name; }
        public String GetPath(){ return path; }
        public boolean IsSelected(){ return selected; }
    }

    private final String apiKey;
    private final String steamId;
    private final String vaultFolder;
    private final String exportFolder;
    private final List<ThemeChoice> themes;
    private final String lastBackup;

    private SettingsView(String apiKey, String steamId, String vaultFolder, String exportFolder, List<ThemeChoice> themes, String lastBackup){
        this.apiKey = apiKey;
        this.steamId = steamId;
        this.vaultFolder = vaultFolder;
        this.exportFolder = exportFolder;
        this.themes = Collections.unmodifiableList(themes);
        this.lastBackup = lastBackup;
    }

    // currentTheme is the theme file in use (what the store holds, or the base one when none was chosen)
    public static SettingsView Read(SettingsStore store, Path themesFolder, Path backupFolder){
        String chosen = store.Get(SettingsStore.themeName);
        List<ThemeChoice> choices = new ArrayList<>();
        List<Path> files = new ArrayList<>();
        if(Files.isDirectory(themesFolder)){
            try (Stream<Path> list = Files.list(themesFolder)) {
                list.filter(f -> f.getFileName().toString().endsWith(themeSuffix) && !f.getFileName().toString().equals(layoutFile)).forEach(files::add);
            } catch (IOException e) {
                files.clear();
            }
        }
        files.sort((a, b) -> NameOf(a).compareToIgnoreCase(NameOf(b)));
        Path chosenPath = chosen.isEmpty() ? null : Paths.get(chosen).normalize();
        boolean anyChosen = false;
        for(Path file : files){
            boolean isChosen = chosenPath != null && file.normalize().equals(chosenPath);
            anyChosen |= isChosen;
            choices.add(new ThemeChoice(NameOf(file), file.toString().replace('\\', '/'), isChosen));
        }
        if(!anyChosen){
            for(int i = 0; i < choices.size(); i++){
                if(choices.get(i).GetPath().endsWith(baseThemeFile)){
                    ThemeChoice base = choices.get(i);
                    choices.set(i, new ThemeChoice(base.GetName(), base.GetPath(), true));
                }
            }
        }
        return new SettingsView(store.Get(SettingsStore.apiKeyName), store.Get(SettingsStore.steamIdName), store.Get(SettingsStore.vaultName), store.Get(SettingsStore.exportName),
                choices, BackupManager.LastBackupText(backupFolder));
    }

    // default.properties is the Base theme; any other is named after its file ("dark.properties" is "Dark")
    private static String NameOf(Path file){
        String name = file.getFileName().toString();
        if(name.equals(baseThemeFile)){
            return baseThemeName;
        }
        String stem = name.substring(0, name.length() - themeSuffix.length());
        return stem.isEmpty() ? name : Character.toUpperCase(stem.charAt(0)) + stem.substring(1);
    }

    public String GetApiKey(){ return apiKey; }
    public String GetSteamId(){ return steamId; }
    public String GetVaultFolder(){ return vaultFolder; }
    public String GetExportFolder(){ return exportFolder; }
    public List<ThemeChoice> GetThemes(){ return themes; }

    // "Last backup 3 Oct 2026, 14:20" or "No backups yet"
    public String GetLastBackup(){ return lastBackup; }
}
