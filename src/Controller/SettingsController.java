package Controller;

import java.awt.Component;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.filechooser.FileNameExtensionFilter;

import Controller.BackEnd.LocalConfig;
import Controller.BackEnd.Settings.BackupManager;
import Controller.BackEnd.Settings.SettingsRules;
import Controller.BackEnd.Settings.SettingsStore;
import Controller.BackEnd.Settings.SettingsView;
import Database.Dao.Db;
import Model.Themes.Theme;
import Model.Themes.ThemeLoader;
import Screens.Settings.SettingsActions;

// What the Settings buttons do: ask for the new value in a window or folder picker, check it, save it, and tell Main what changed. The
// checks are SettingsRules, the saving is SettingsStore and the backups are BackupManager; this is only the asking
class SettingsController implements SettingsActions {
    public static final Path settingsFile = Paths.get(SettingsStore.defaultPath);
    public static final Path themesFolder = Paths.get("Resources/Themes");
    public static final Path backupFolder = Paths.get("Resources/Databases/backups");

    private static final String windowTitle = "Settings";
    private static final int keyColumns = 32;

    private final Component parent;
    private final SettingsStore store = new SettingsStore(settingsFile);
    private final Runnable onBack;
    private final Runnable onChanged;
    private final Runnable onThemeChanged;
    private final Runnable onRestored;

    // onChanged: a setting was saved (read the screen again). onThemeChanged: Theme.Current was swapped. onRestored: the database was replaced
    SettingsController(Component parent, Runnable onBack, Runnable onChanged, Runnable onThemeChanged, Runnable onRestored){
        this.parent = parent;
        this.onBack = onBack;
        this.onChanged = onChanged;
        this.onThemeChanged = onThemeChanged;
        this.onRestored = onRestored;
    }

    SettingsView Read(){
        return SettingsView.Read(store, themesFolder, backupFolder);
    }

    // The theme saved last time, or null to keep the built-in one
    static Theme SavedTheme(){
        String path = new SettingsStore(settingsFile).Get(SettingsStore.themeName);
        if(path.isEmpty()){
            return null;
        }
        try {
            return ThemeLoader.Load(path);
        } catch (IOException | FontFormatException e) {
            System.err.println("Couldn't load the saved theme " + path + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public void Back(){
        onBack.run();
    }

    @Override
    public void ChangeApiKey(){
        JPasswordField field = new JPasswordField(keyColumns);
        String problem = null;
        while(true){
            field.setText("");
            Object[] message = problem == null ? new Object[] { "Paste your Steam API key:", field } : new Object[] { problem, field };
            if(JOptionPane.showConfirmDialog(parent, message, "Steam API key", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION){
                return;
            }
            String key = new String(field.getPassword());
            problem = SettingsRules.CheckApiKey(key);
            if(problem == null){
                Save(SettingsStore.apiKeyName, key.trim());
                return;
            }
        }
    }

    @Override
    public void ChangeSteamId(){
        String current = store.Get(SettingsStore.steamIdName);
        String problem = null;
        while(true){
            String text = problem == null ? "Your 17-digit SteamID:" : problem;
            Object answer = JOptionPane.showInputDialog(parent, text, "SteamID", JOptionPane.PLAIN_MESSAGE, null, null, current);
            if(answer == null){
                return;
            }
            current = answer.toString().trim();
            problem = SettingsRules.CheckSteamId(current);
            if(problem == null){
                Save(SettingsStore.steamIdName, current);
                return;
            }
        }
    }

    @Override
    public void BrowseVault(){
        BrowseFolder(SettingsStore.vaultName, "Vault folder");
    }

    @Override
    public void BrowseExport(){
        BrowseFolder(SettingsStore.exportName, "Export folder");
    }

    private void BrowseFolder(String name, String title){
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String current = store.Get(name);
        if(!current.isEmpty() && SettingsRules.CheckFolder(current) == null){
            chooser.setCurrentDirectory(new File(current));
        }
        if(chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION){
            return;
        }
        String folder = SettingsRules.TidyPath(chooser.getSelectedFile().getPath());
        String problem = SettingsRules.CheckFolder(folder);
        if(problem != null){
            JOptionPane.showMessageDialog(parent, problem, windowTitle, JOptionPane.WARNING_MESSAGE);
            return;
        }
        Save(name, folder);
    }

    private void Save(String name, String value){
        try {
            store.Set(name, value);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Couldn't save that: " + e.getMessage(), windowTitle, JOptionPane.ERROR_MESSAGE);
            return;
        }
        LocalConfig.Reload();
        onChanged.run();
    }

    @Override
    public void ChooseTheme(String path){
        Theme theme;
        try {
            theme = ThemeLoader.Load(path);
        } catch (IOException | FontFormatException e) {
            JOptionPane.showMessageDialog(parent, "Couldn't load that theme: " + e.getMessage(), windowTitle, JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            store.Set(SettingsStore.themeName, path);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "The theme is on for now, but couldn't be remembered: " + e.getMessage(), windowTitle, JOptionPane.WARNING_MESSAGE);
        }
        Theme.Current = theme;
        onThemeChanged.run();
    }

    @Override
    public void BackUpDatabase(){
        try {
            Path copy = BackupManager.Backup(Paths.get(Db.filePath), backupFolder, LocalDateTime.now());
            onChanged.run();
            JOptionPane.showMessageDialog(parent, "Backed up to " + copy.toString().replace('\\', '/'), windowTitle, JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Couldn't back up: " + e.getMessage(), windowTitle, JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void RestoreBackup(){
        JFileChooser chooser = new JFileChooser(backupFolder.toFile());
        chooser.setDialogTitle("Restore backup");
        chooser.setFileFilter(new FileNameExtensionFilter("Database files (*.db)", "db"));
        if(chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION){
            return;
        }
        Path backup = chooser.getSelectedFile().toPath();
        String problem = BackupManager.CheckBackup(backup);
        if(problem != null){
            JOptionPane.showMessageDialog(parent, problem, windowTitle, JOptionPane.WARNING_MESSAGE);
            return;
        }
        String question = "Replace the database with " + backup.getFileName() + "?\nWhat is there now is backed up first.";
        if(JOptionPane.showConfirmDialog(parent, question, "Restore backup", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION){
            return;
        }
        try {
            BackupManager.Restore(Paths.get(Db.filePath), backup, backupFolder, LocalDateTime.now());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Couldn't restore: " + e.getMessage(), windowTitle, JOptionPane.ERROR_MESSAGE);
            return;
        }
        onRestored.run();
        JOptionPane.showMessageDialog(parent, "Restored from " + backup.getFileName(), windowTitle, JOptionPane.INFORMATION_MESSAGE);
    }
}
