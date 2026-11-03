package Screens.Settings;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.Settings.SettingsRules;
import Controller.BackEnd.Settings.SettingsView;
import Model.Layout.Layout;
import Model.Layout.LayoutMath;
import Model.Layout.OverviewLayout;
import Model.Layout.ShellLayout;
import Model.Themes.TextStyle;
import Model.Themes.Theme;
import Screens.MainView.CardBox;
import Screens.MainView.HitMap;
import Screens.MainView.NavEntry;
import Screens.MainView.NavStrip;
import Screens.MainView.TextFit;
import Screens.MainView.TextLine;

// Settings: the Steam key and ID, the vault and export folders, which theme is used, and backing up or restoring the database. Draws
// what SettingsView read and hands every button back through SettingsActions; the only thing it keeps is whether the key is shown.
// The page scrolls
public class SettingsScreen {
    public static final String backKey = "settings:back";
    public static final String showKey = "settings:key:show";
    public static final String changeKeyKey = "settings:key:change";
    public static final String changeIdKey = "settings:id:change";
    public static final String vaultKey = "settings:vault:browse";
    public static final String exportKey = "settings:export:browse";
    public static final String backupKey = "settings:backup";
    public static final String restoreKey = "settings:restore";
    public static final String rebuildKey = "settings:rebuild";

    public static final String title = "Settings";
    public static final String caption = "ReviewMe";
    public static final String steamHeading = "Steam";
    public static final String foldersHeading = "Folders";
    public static final String appearanceHeading = "Appearance";
    public static final String dataHeading = "Data";
    public static final String keyLabel = "Steam API key";
    public static final String keyNote = "Stored on this computer only (in Resources/local.properties).";
    public static final String idLabel = "SteamID";
    public static final String idNote = "17-digit ID of the profile whose library is synced.";
    public static final String vaultLabel = "Vault folder";
    public static final String vaultNote = "The folder your review files live in.";
    public static final String exportLabel = "Export folder";
    public static final String exportNote = "Where Export saves its files.";
    public static final String notSet = "Not set";
    public static final String showWords = "Show";
    public static final String hideWords = "Hide";
    public static final String changeWords = "Change";
    public static final String browseWords = "Browse";
    public static final String backupWords = "Back up database";
    public static final String restoreWords = "Restore backup";
    public static final String restoreNote = "Replaces the database with a backup file (what is there now is backed up first)";
    public static final String rebuildWords = "Rebuild from vault";
    public static final String rebuildNote = "Not available yet: reviews made in the app are not written to the vault until Export is built, so a rebuild would lose them.";

    public static String ThemeKey(String path){ return "settings:theme:" + path; }

    private SettingsView view;
    private final Layout layout;
    private final SettingsActions actions;
    private final HitMap hits = new HitMap();
    private final NavStrip strip;
    private boolean keyShown;
    private String pressedKey;
    private int scroll;
    private int contentHeight;
    private int viewportHeight;

    public SettingsScreen(SettingsView view, Layout layout, SettingsActions actions){
        this.view = view;
        this.layout = layout;
        this.actions = actions;
        this.strip = new NavStrip(layout.GetShell());
    }

    // Shows the settings as they are now (after something was changed)
    public void Reload(SettingsView fresh){
        this.view = fresh;
    }

    public boolean IsKeyShown(){ return keyShown; }

    public void Paint(Graphics graphics, Point mouse, Dimension size){
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            hits.Clear();
            ShellLayout shell = layout.GetShell();
            Rectangle area = new Rectangle(shell.GetPadding(), shell.GetPadding(), Math.max(0, size.width - shell.GetPadding() - shell.GetPadding()),
                    Math.max(0, size.height - shell.GetPadding() - shell.GetPadding()));
            viewportHeight = area.height;
            scroll = ClampScroll(scroll);
            int mark = hits.Size();
            int scrolledTo = scroll;
            PaintPage(g, area, mouse);
            if(scroll != scrolledTo){
                hits.TruncateTo(mark);
                g.setClip(null);
                g.setColor(Theme.Current.GetBackground());
                g.fillRect(0, 0, size.width, size.height);
                PaintPage(g, area, mouse);
            }
        } finally {
            g.dispose();
        }
    }

    private void PaintPage(Graphics2D g, Rectangle area, Point mouse){
        Graphics2D inPage = (Graphics2D) g.create();
        try {
            inPage.clipRect(area.x, area.y, area.width, area.height);
            inPage.translate(area.x, area.y - scroll);
            hits.SetFrame(area.x, area.y - scroll, area);
            contentHeight = PaintContent(inPage, area.width, new Point(mouse.x - area.x, mouse.y - area.y + scroll));
        } finally {
            hits.ClearFrame();
            inPage.dispose();
        }
        scroll = ClampScroll(scroll);
    }

    private int PaintContent(Graphics2D g, int width, Point mouse){
        ShellLayout shell = layout.GetShell();
        OverviewLayout overview = layout.GetOverview();
        int y = 0;
        List<NavEntry> back = new ArrayList<>();
        back.add(new NavEntry(backKey, "Back", null, false, true, actions::Back));
        y += strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, null, back, mouse, hits) + shell.GetGap();
        y += PaintTitle(g, y, width) + overview.GetSectionGap();

        y += PaintHeading(g, y, width, steamHeading);
        List<NavEntry> keyButtons = new ArrayList<>();
        keyButtons.add(new NavEntry(showKey, keyShown ? hideWords : showWords, null, false, !view.GetApiKey().isEmpty(), () -> keyShown = !keyShown));
        keyButtons.add(new NavEntry(changeKeyKey, changeWords, null, false, true, actions::ChangeApiKey));
        y += PaintSetting(g, y, width, keyLabel, view.GetApiKey().isEmpty() ? notSet : keyShown ? view.GetApiKey() : SettingsRules.Masked(view.GetApiKey()), keyNote, keyButtons, mouse) + shell.GetGap();
        List<NavEntry> idButtons = new ArrayList<>();
        idButtons.add(new NavEntry(changeIdKey, changeWords, null, false, true, actions::ChangeSteamId));
        y += PaintSetting(g, y, width, idLabel, view.GetSteamId().isEmpty() ? notSet : view.GetSteamId(), idNote, idButtons, mouse) + overview.GetSectionGap();

        y += PaintHeading(g, y, width, foldersHeading);
        List<NavEntry> vaultButtons = new ArrayList<>();
        vaultButtons.add(new NavEntry(vaultKey, browseWords, null, false, true, actions::BrowseVault));
        y += PaintSetting(g, y, width, vaultLabel, view.GetVaultFolder().isEmpty() ? notSet : view.GetVaultFolder(), vaultNote, vaultButtons, mouse) + shell.GetGap();
        List<NavEntry> exportButtons = new ArrayList<>();
        exportButtons.add(new NavEntry(exportKey, browseWords, null, false, true, actions::BrowseExport));
        y += PaintSetting(g, y, width, exportLabel, view.GetExportFolder().isEmpty() ? notSet : view.GetExportFolder(), exportNote, exportButtons, mouse) + overview.GetSectionGap();

        y += PaintHeading(g, y, width, appearanceHeading);
        List<NavEntry> themes = new ArrayList<>();
        for(SettingsView.ThemeChoice theme : view.GetThemes()){
            themes.add(new NavEntry(ThemeKey(theme.GetPath()), theme.GetName(), null, theme.IsSelected(), true, () -> actions.ChooseTheme(theme.GetPath())));
        }
        y += strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, null, themes, mouse, hits) + overview.GetSectionGap();

        y += PaintHeading(g, y, width, dataHeading);
        List<NavEntry> data = new ArrayList<>();
        data.add(new NavEntry(backupKey, backupWords, null, false, true, actions::BackUpDatabase));
        data.add(new NavEntry(restoreKey, restoreWords, null, false, true, actions::RestoreBackup));
        data.add(new NavEntry(rebuildKey, rebuildWords, null, false, false, null));
        y += strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, null, data, mouse, hits) + shell.GetItemGap();
        y += PaintNote(g, y, width, view.GetLastBackup()) + shell.GetItemGap();
        y += PaintNote(g, y, width, restoreNote) + shell.GetItemGap();
        y += PaintNote(g, y, width, rebuildNote);
        return y + layout.GetList().GetBottomPadding();
    }

    private int PaintTitle(Graphics2D g, int top, int width){
        TextStyle small = Theme.Current.GetHeading(), big = Theme.Current.GetTitle();
        FontMetrics smallMetrics = g.getFontMetrics(small.GetFont()), bigMetrics = g.getFontMetrics(big.GetFont());
        g.setFont(small.GetFont());
        g.setColor(small.GetColour());
        g.drawString(caption, 0, top + smallMetrics.getAscent());
        g.setFont(big.GetFont());
        g.setColor(big.GetColour());
        g.drawString(title, 0, top + smallMetrics.getHeight() + bigMetrics.getAscent());
        return smallMetrics.getHeight() + bigMetrics.getHeight();
    }

    // A section heading with a rule under it; returns the height taken
    private int PaintHeading(Graphics2D g, int top, int width, String text){
        ShellLayout shell = layout.GetShell();
        TextStyle style = Theme.Current.GetHeading();
        FontMetrics metrics = g.getFontMetrics(style.GetFont());
        g.setFont(style.GetFont());
        g.setColor(style.GetColour());
        g.drawString(text, 0, top + metrics.getAscent());
        int ruleY = top + metrics.getHeight() + LayoutMath.Half(shell.GetItemGap());
        g.setColor(Theme.Current.GetCell().GetBorder());
        g.drawLine(0, ruleY, width, ruleY);
        return metrics.getHeight() + shell.GetItemGap() + shell.GetGap();
    }

    // A label, a box holding the value with its buttons at the right, and a note under it
    private int PaintSetting(Graphics2D g, int top, int width, String label, String value, String note, List<NavEntry> buttons, Point mouse){
        ShellLayout shell = layout.GetShell();
        int pad = layout.GetOverview().GetCardPadding();
        TextStyle small = Theme.Current.GetHeading();
        FontMetrics smallMetrics = g.getFontMetrics(small.GetFont()), valueMetrics = g.getFontMetrics(Theme.Current.GetField().GetFont());
        int y = top;
        g.setFont(small.GetFont());
        g.setColor(small.GetColour());
        g.drawString(label, 0, y + smallMetrics.getAscent());
        y += smallMetrics.getHeight();
        Rectangle box = new Rectangle(0, y, width, Math.max(layout.GetList().GetRowHeight(), shell.GetStateRowHeight() + pad));
        CardBox.Paint(g, box, false);
        int buttonsWidth = strip.Width(g, null, buttons);
        strip.Paint(g, box.x + box.width - pad - buttonsWidth, box.y + LayoutMath.Centre(box.height, shell.GetStateRowHeight()), shell.GetStateRowHeight(), buttonsWidth, null, buttons, mouse, hits);
        g.setFont(Theme.Current.GetField().GetFont());
        g.setColor(value.equals(notSet) ? Theme.Current.GetNav().GetDisabledText() : Theme.Current.GetCell().GetText());
        g.drawString(TextFit.Fit(valueMetrics, value, Math.max(0, box.width - pad - pad - buttonsWidth - shell.GetItemGap())), box.x + pad, TextLine.Baseline(box.y, box.height, valueMetrics));
        y += box.height + LayoutMath.Half(shell.GetItemGap());
        return y + PaintNote(g, y, width, note) - top;
    }

    private int PaintNote(Graphics2D g, int top, int width, String text){
        FontMetrics metrics = g.getFontMetrics(Theme.Current.GetField().GetFont());
        g.setFont(Theme.Current.GetField().GetFont());
        g.setColor(Theme.Current.GetHeading().GetColour());
        g.drawString(TextFit.Fit(metrics, text, width), 0, top + metrics.getAscent());
        return metrics.getHeight();
    }

    public void Scroll(int wheelRotation){
        scroll = ClampScroll(scroll + wheelRotation * layout.GetList().GetScrollStep());
    }

    private int ClampScroll(int value){
        return Math.max(0, Math.min(value, Math.max(0, contentHeight - viewportHeight)));
    }

    public void Press(Point point){
        pressedKey = hits.KeyAt(point);
    }

    // True if that completed a click, in which case the screen has changed and needs painting again
    public boolean Release(Point point){
        String key = hits.KeyAt(point);
        boolean clicked = key != null && key.equals(pressedKey);
        pressedKey = null;
        return clicked && hits.Run(key);
    }

    public Rectangle BoundsOf(String key){
        return hits.BoundsOf(key);
    }
}
