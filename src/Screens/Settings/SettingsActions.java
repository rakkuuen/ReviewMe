package Screens.Settings;

// What the Settings screen's buttons ask its owner to do. The owner asks the user for the new value (a dialog, a folder picker), checks
// it, saves it, and hands the screen the settings as they are now
public interface SettingsActions {
    void Back();
    void ChangeApiKey();
    void ChangeSteamId();
    void BrowseVault();
    void BrowseExport();

    // The theme file chosen
    void ChooseTheme(String path);

    void BackUpDatabase();
    void RestoreBackup();
}
