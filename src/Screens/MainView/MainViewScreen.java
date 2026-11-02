package Screens.MainView;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import Controller.BackEnd.MainView.ViewTarget;
import Controller.BackEnd.MainView.HeaderText;
import Controller.BackEnd.MainView.MainViewData;
import Controller.BackEnd.MainView.MainViewLabels;
import Controller.BackEnd.MainView.MainViewModel;
import Controller.BackEnd.MainView.MainViewState;
import Controller.BackEnd.MainView.MainTab;
import Controller.BackEnd.MainView.StateItem;
import Controller.BackEnd.MainView.TabItem;
import Controller.BackEnd.Overview.OverviewStats;
import Model.Layout.Layout;
import Model.Layout.LayoutMath;
import Model.Layout.ShellLayout;
import Model.Library.LibraryGame;
import Model.Themes.TextStyle;
import Model.Themes.Theme;

// The app's home screen: the header, the State row, the tab bar, and whatever the open tab shows underneath. It draws what the
// view model says and passes clicks back to the view state; every rule (which tabs exist, what falls back where) is the backend's
public class MainViewScreen {
    private static final String appName = "ReviewMe";
    private static final String stateCaption = "State";
    private static final String syncKey = "action:sync";
    private static final String libraryKey = "action:library";

    private MainViewData data;
    private final Layout layout;
    private final Runnable onLibrary;
    private final MainViewState state = new MainViewState();
    private final HitMap hits = new HitMap();
    private final NavStrip strip;
    private final OverviewBody overviewBody;
    private MainViewModel model;
    private String pressedKey;
    private int scroll;           // How far the body's content is scrolled up
    private int contentHeight;    // How tall the body's content was when last painted
    private int viewportHeight;   // How tall the space it is shown in was

    // onLibrary is what the header's Library button does
    public MainViewScreen(MainViewData data, Layout layout, Runnable onLibrary){
        this(data, layout, onLibrary, game -> { });
    }

    // onOpenGame is what clicking a game's row does
    public MainViewScreen(MainViewData data, Layout layout, Runnable onLibrary, Consumer<LibraryGame> onOpenGame){
        this.data = data;
        this.layout = layout;
        this.onLibrary = onLibrary;
        this.strip = new NavStrip(layout.GetShell());
        this.overviewBody = new OverviewBody(data.GetOverview(), layout, this::OpenTarget, onOpenGame);
        Refresh();
    }

    // Shows newer data (after a game was edited, say) and stays where it is: the same state, tab and scroll, series still folded
    public void Reload(MainViewData fresh){
        this.data = fresh;
        overviewBody.Reload(fresh.GetOverview());
        Rebuild();
    }

    // Goes to a state and tab ("View all" on the Overview)
    private void OpenTarget(ViewTarget target){
        state.Open(target);
        Refresh();
    }

    public MainViewState GetState(){ return state; }

    // Draws the whole screen into the given size. Call it again whenever anything changes: it keeps nothing it can work out again
    public void Paint(Graphics graphics, Point mouse, Dimension size){
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            hits.Clear();

            ShellLayout shell = layout.GetShell();
            Rectangle area = new Rectangle(shell.GetPadding(), shell.GetPadding(),
                    Math.max(0, size.width - shell.GetPadding() - shell.GetPadding()), Math.max(0, size.height - shell.GetPadding() - shell.GetPadding()));
            g.setClip(area);

            int y = area.y;
            PaintHeader(g, area.x, y, area.width, mouse);
            y += shell.GetHeaderHeight() + shell.GetGap();
            y += strip.Paint(g, area.x, y, shell.GetStateRowHeight(), area.width, stateCaption, StateEntries(), mouse, hits) + shell.GetGap();
            y += strip.Paint(g, area.x, y, shell.GetTabBarHeight(), area.width, null, TabEntries(), mouse, hits) + shell.GetGap();

            PaintBody(g, new Rectangle(area.x, y, area.width, Math.max(0, area.y + area.height - y)), mouse);
        } finally {
            g.dispose();
        }
    }

    // Moves the body's content up or down by that many wheel notches (positive is down). Stops at the top and the bottom
    public void Scroll(int wheelRotation){
        scroll = ClampScroll(scroll + wheelRotation * layout.GetList().GetScrollStep());
    }

    // The body paints in its own coordinates; this shifts them up by the scroll and cuts them to the space under the tab bar
    private void PaintBody(Graphics2D g, Rectangle body, Point mouse){
        viewportHeight = body.height;
        scroll = ClampScroll(scroll);
        int scrolledTo = scroll;
        int mark = hits.Size();
        PaintBodyAt(g, body, mouse);
        if(scroll != scrolledTo){
            // The content turned out shorter than the scroll allowed (newer data, say): wipe that attempt and paint from where it can be
            hits.TruncateTo(mark);
            g.setColor(Theme.Current.GetBackground());
            g.fillRect(body.x, body.y, body.width, body.height);
            PaintBodyAt(g, body, mouse);
        }
    }

    // One go at painting the body at the current scroll. Afterwards the scroll is held to what the content allows
    private void PaintBodyAt(Graphics2D g, Rectangle body, Point mouse){
        Graphics2D inBody = (Graphics2D) g.create();
        try {
            inBody.clipRect(body.x, body.y, body.width, body.height);
            inBody.translate(body.x, body.y - scroll);
            hits.SetFrame(body.x, body.y - scroll, body);
            Point inContent = new Point(mouse.x - body.x, mouse.y - body.y + scroll);
            contentHeight = BodyFor(state.GetTab()).Paint(inBody, new Rectangle(0, 0, body.width, body.height), inContent, hits);
        } finally {
            hits.ClearFrame();
            inBody.dispose();
        }
        scroll = ClampScroll(scroll);
    }

    private int ClampScroll(int value){
        return Math.max(0, Math.min(value, Math.max(0, contentHeight - viewportHeight)));
    }

    // Remembers what was pressed. A click only happens if the release is on the same thing
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

    // Where a button was last drawn, or null (for tests and for anything that needs to point at a button)
    public Rectangle BoundsOf(String key){
        return hits.BoundsOf(key);
    }

    public static String StateKey(String label){ return "state:" + label; }
    public static String TabKey(MainTab tab){ return "tab:" + tab.name(); }

    // Rebuilds what is drawn for the current state, and starts the new tab at the top
    private void Refresh(){
        Rebuild();
        scroll = 0;
    }

    private void Rebuild(){
        model = MainViewModel.Build(data.GetGames(), data.GetSeriesIds(), state);
    }

    private List<NavEntry> StateEntries(){
        List<NavEntry> entries = new ArrayList<>();
        for(StateItem item : model.GetStateItems()){
            entries.add(new NavEntry(StateKey(item.GetLabel()), item.GetLabel(), item.GetCount(), item.IsSelected(), true, () -> {
                state.SelectState(item.GetSelection());
                Refresh();
            }));
        }
        return entries;
    }

    private List<NavEntry> TabEntries(){
        List<NavEntry> entries = new ArrayList<>();
        for(TabItem item : model.GetTabItems()){
            entries.add(new NavEntry(TabKey(item.GetTab()), item.GetLabel(), item.GetCount(), item.IsSelected(), true, () -> {
                state.SelectTab(item.GetTab());
                Refresh();
            }));
        }
        return entries;
    }

    // Sync can't be used from here yet; Library goes where onLibrary says
    private List<NavEntry> HeaderActions(){
        List<NavEntry> actions = new ArrayList<>();
        actions.add(new NavEntry(syncKey, "Sync", null, false, false, null));
        actions.add(new NavEntry(libraryKey, "Library", null, false, true, onLibrary));
        return actions;
    }

    private BodyPainter BodyFor(MainTab tab){
        if(tab == MainTab.OVERVIEW){
            return overviewBody;
        }
        return new PlaceholderBody(MainViewLabels.TabLabel(tab) + " is not built yet");
    }

    // The app's name with its summary line under it on the left; the last sync time and the header's actions on the right
    private void PaintHeader(Graphics2D g, int x, int y, int width, Point mouse){
        ShellLayout shell = layout.GetShell();
        TextStyle title = Theme.Current.GetTitle();
        TextStyle line = Theme.Current.GetHeading();
        FontMetrics titleMetrics = g.getFontMetrics(title.GetFont());
        FontMetrics lineMetrics = g.getFontMetrics(line.GetFont());
        OverviewStats stats = data.GetOverview().GetStats();

        String summary = HeaderText.Summary(stats.GetOwnedCount(), stats.GetNewAchievementsTotal());
        int top = y + LayoutMath.Centre(shell.GetHeaderHeight(), titleMetrics.getHeight() + lineMetrics.getHeight());
        g.setFont(title.GetFont());
        g.setColor(title.GetColour());
        g.drawString(appName, x, top + titleMetrics.getAscent());
        g.setFont(line.GetFont());
        g.setColor(line.GetColour());
        g.drawString(summary, x, top + titleMetrics.getHeight() + lineMetrics.getAscent());

        List<NavEntry> actions = HeaderActions();
        int actionsX = x + width - strip.Width(g, null, actions);
        strip.Paint(g, actionsX, y + LayoutMath.Centre(shell.GetHeaderHeight(), shell.GetStateRowHeight()), shell.GetStateRowHeight(), Integer.MAX_VALUE, null, actions, mouse, hits);

        String synced = HeaderText.LastSynced(stats.GetLastSyncedAt(), Instant.now().getEpochSecond(), ZoneId.systemDefault());
        int syncedX = actionsX - shell.GetItemGap() - lineMetrics.stringWidth(synced);
        int leftBlockEnd = x + Math.max(titleMetrics.stringWidth(appName), lineMetrics.stringWidth(summary)) + shell.GetGap();
        if(syncedX > leftBlockEnd){
            g.drawString(synced, syncedX, y + LayoutMath.Centre(shell.GetHeaderHeight(), lineMetrics.getHeight()) + lineMetrics.getAscent());
        }
    }
}
