package Controller;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import Database.Dao.GameReviewDao;
import Screens.FrontPage;
import Screens.GameInfoScreen;
import Screens.MainView.MainViewScreen;
import Screens.MainView.SeriesActions;
import Screens.MainView.SyncActions;
import Controller.BackEnd.SyncRun.RealSyncSteps;
import Controller.BackEnd.SyncRun.SyncRunner;
import Controller.BackEnd.SyncRun.SyncSnapshot;
import Controller.BackEnd.SyncRun.SyncState;
import Controller.BackEnd.SyncRun.SyncWords;
import Screens.SeriesView.SeriesViewScreen;
import Screens.Library.LibraryScreen;
import Screens.GameDetail.GameDetailScreen;
import Screens.Achievements.AchievementsScreen;
import Screens.Settings.SettingsScreen;
import Controller.BackEnd.LocalConfig;
import Controller.BackEnd.AchievementView.AchievementViewData;
import Controller.BackEnd.AchievementView.AchievementViewLoader;
import Controller.BackEnd.GameDetail.GameDetailData;
import Controller.BackEnd.GameDetail.GameDetailLoader;
import Database.Dao.SeriesDao;
import Controller.BackEnd.MainView.LibraryGameLoader;
import Controller.BackEnd.SeriesView.SeriesViewData;
import Controller.BackEnd.SeriesView.SeriesViewLoader;
import Controller.BackEnd.MainView.MainViewData;
import Controller.BackEnd.MainView.MainViewLoader;
import Controller.BackEnd.Reviews.LoadedReview;
import Controller.BackEnd.Reviews.ReviewLoader;
import Controller.ScreenFlow.Screen;
import Model.Layout.Layout;
import Model.Layout.LayoutException;
import Model.Layout.LayoutLoader;
import Model.Layout.WindowLayout;
import Model.Library.LibraryGame;
import Model.Widgets.GameCell;
import Model.Themes.Theme;



class Main extends JFrame{
    private static final String layoutPath = "Resources/Themes/layout.properties";
    private static final String startTheme = "Resources/Themes/default.properties";   // Its layout.* lines (if any) are laid over the layout file

    App canvas;
    Dimension windowDimension;
    Point mousePos;

    class App extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, KeyListener {
        private final Layout layout;
        private MainViewScreen myMainView;
        private FrontPage myFrontPage;
        private GameInfoScreen myGameInfoScreen;   // The old page: now only the editor, opened from the new page's Edit
        private GameDetailScreen myGameDetail;
        private AchievementsScreen myAchievements;
        private SettingsScreen mySettings;
        private SettingsController settingsController;
        private boolean editingGame;
        private volatile SyncSnapshot syncStatus = SyncWords.Idle();   // Written by the sync thread, read when painting
        private SeriesViewScreen mySeriesView;
        private LibraryScreen myLibrary;
        private final ScreenFlow flow = new ScreenFlow();
        private final RowActionHandler rowActions = new RowActionHandler(this, this::ReloadAndRepaint, this::OpenGameByTitle);

        // What the current press landed on, so release only acts if it's still on the same target
        private GameCell pressedCell;
        private boolean pressedOnEditButton;
        private boolean pressedOnSaveButton;
        private boolean pressedOnCancelButton;
        private boolean pressedOnSectionsButton;

        public App(Layout layout, MainViewData data) {
            this.layout = layout;
            WindowLayout window = layout.GetWindow();
            setPreferredSize(new Dimension(window.GetStartWidth(), window.GetStartHeight()));
            setLayout(null); // Absolute positioning, to match GameCell/Button's coordinate style
            setFocusable(true); // So key events (the theme-toggle test hook) can actually reach this panel
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
            this.addMouseWheelListener(this);
            this.addComponentListener(this);
            this.addKeyListener(this);

            myMainView = new MainViewScreen(data, layout, this::OpenLibrary, this::OpenGameFromMainView, rowActions::Run, (game, items) -> rowActions.ShowMenu(game, items, mousePos),
                    new SeriesActions(this::OpenSeries, this::NewSeries), new SyncActions(() -> syncStatus, this::StartSync, this::OpenSettings));

            // The panel isn't packed/realized yet, so getSize() would still read (0,0) here -
            // use the preferred size as the initial layout estimate. ReflowAll() (called from
            // Main's constructor once the real windowDimension is known after pack()) corrects
            // it to the actual final size.
            myFrontPage = new FrontPage(getPreferredSize());
        }

        // Re-lays out every screen against the current windowDimension - called once after
        // the window is packed to its real size, and again on every resize. The main view lays itself out each time it paints
        public void ReflowAll(){
            myFrontPage.Reflow(windowDimension);
            if(myGameInfoScreen != null){
                myGameInfoScreen.Reflow(windowDimension);
            }
        }

        // A game's row was clicked on the main view
        private void OpenGameFromMainView(LibraryGame game){
            OpenGameByTitle(game.GetTitle());
        }

        private void OpenGameByTitle(String title){
            GameDetailData page = GameDetailLoader.Load(title);
            if(page == null){
                return;
            }
            myGameDetail = new GameDetailScreen(page, layout, this::CloseGameDetail, this::EditGame, this::OpenSeriesFromGame, this::OpenAchievements);
            editingGame = false;
            flow.OpenGame();
        }

        // Edit on a game's page: the old editor opens in edit mode. When it is closed the page shows what was saved
        private void EditGame(){
            LoadedReview review = ReviewLoader.Load(myGameDetail.GetTitle());
            if(review == null){
                return;
            }
            myGameInfoScreen = new GameInfoScreen(review, windowDimension, this::EndEditing);
            myGameInfoScreen.AddComponentsTo(this);
            myGameInfoScreen.EnterEditMode();
            editingGame = true;
        }

        private void EndEditing(){
            boolean saved = myGameInfoScreen.WasSaved();
            myGameInfoScreen.RemoveComponentsFrom(this);
            editingGame = false;
            GameDetailData fresh = GameDetailLoader.Load(myGameDetail.GetTitle());
            if(fresh == null){
                CloseGameDetail();
            } else {
                myGameDetail.Reload(fresh);
                if(saved){
                    ShowGameToast("Saved");
                }
            }
        }

        // A short message over the game's page that goes away by itself
        private void ShowGameToast(String message){
            myGameDetail.ShowToast(message);
            javax.swing.Timer timer = new javax.swing.Timer(layout.GetShell().GetToastMillis(), e -> {
                if(myGameDetail != null && message.equals(myGameDetail.GetToast())){
                    myGameDetail.ClearToast();
                    repaint();
                }
            });
            timer.setRepeats(false);
            timer.start();
        }

        // The series link on a game's page: leaves the game and opens the series
        private void OpenSeriesFromGame(String seriesId){
            AfterGameClosed(flow.CloseGame());
            OpenSeries(seriesId);
        }

        // View all on a game's page: its achievements, with filter and order. Back returns to the game
        private void OpenAchievements(){
            AchievementViewData page = AchievementViewLoader.Load(myGameDetail.GetTitle());
            if(page == null){
                return;
            }
            myAchievements = new AchievementsScreen(page, layout, this::CloseAchievements);
            flow.OpenAchievements();
        }

        private void CloseAchievements(){
            flow.CloseAchievements();
        }

        // Back from a game's page
        private void CloseGameDetail(){
            AfterGameClosed(flow.CloseGame());
        }

        // The Library button: every game, with search, filters and order. Back returns to the main view
        private void OpenLibrary(){
            myLibrary = new LibraryScreen(LibraryGameLoader.Load(), SeriesDao.GetAllSeries(), layout, this::CloseLibrary, this::OpenGameFromMainView, rowActions::Run,
                    (game, items) -> rowActions.ShowMenu(game, items, mousePos), new LibraryPopupMenus(this, () -> mousePos, this::repaint, this::AddGameManually));
            flow.ShowLibrary();
        }

        private void CloseLibrary(){
            flow.ShowMainView();
            ReloadMainView();
        }

        // Opens a series' page (from the Series tab). Back returns to the main view
        private void OpenSeries(String seriesId){
            SeriesViewData page = SeriesViewLoader.Load(seriesId);
            if(page == null){
                return;
            }
            mySeriesView = new SeriesViewScreen(page, layout, this::CloseSeries, this::NewSeries, this::EditSeries, this::AssignGames,
                    this::OpenGameFromMainView);
            flow.OpenSeries();
        }

        private void CloseSeries(){
            flow.CloseSeries();
            ReloadMainView();
        }

        // The Sync button: the library and then the achievements, on a thread of its own so the window stays alive. The screen shows how
        // far it is and, when it stops, why. Whatever it saved is read in again when it ends
        private void StartSync(){
            if(syncStatus.GetState() == SyncState.RUNNING){
                return;
            }
            syncStatus = SyncWords.Running(0, 0);
            repaint();
            Thread worker = new Thread(() -> {
                SyncRunner.Run(new RealSyncSteps(), snapshot -> {
                    syncStatus = snapshot;
                    SwingUtilities.invokeLater(this::repaint);
                });
                SwingUtilities.invokeLater(this::ReloadAfterSync);
            }, "steam-sync");
            worker.setDaemon(true);
            worker.start();
        }

        private void ReloadAfterSync(){
            ReloadMainView();
            if(flow.Current() == Screen.LIBRARY){
                myLibrary.Reload(LibraryGameLoader.Load(), SeriesDao.GetAllSeries());
            }
            repaint();
        }

        // The header's Settings button, and Open settings on a failed sync: the Steam key and ID, folders, theme and backups. Back returns to the main view
        private void OpenSettings(){
            settingsController = new SettingsController(this, this::CloseSettings, this::ShowSettingsChange, this::ShowThemeChange, this::ShowRestoredDatabase);
            mySettings = new SettingsScreen(settingsController.Read(), layout, settingsController);
            flow.ShowSettings();
            repaint();
        }

        private void CloseSettings(){
            flow.ShowMainView();
            repaint();
        }

        private void ShowSettingsChange(){
            mySettings.Reload(settingsController.Read());
            repaint();
        }

        private void ShowThemeChange(){
            if(myGameInfoScreen != null){
                myGameInfoScreen.ReapplyTheme();
            }
            ShowSettingsChange();
        }

        // A backup was put in place of the database, so everything on show is read in again
        private void ShowRestoredDatabase(){
            ReloadAndRepaint();
            ShowSettingsChange();
        }

        // + Add game manually on the Library: a review for a game that isn't on Steam, then its page
        private void AddGameManually(){
            NewReviewDialog.Show(this, null, title -> {
                ReloadAndRepaint();
                OpenGameByTitle(title);
            });
        }

        // Asks for a name and template, makes the series, and opens its (empty) page
        private void NewSeries(){
            NewSeriesDialog.Show(this, id -> {
                ReloadMainView();
                OpenSeries(id);
                repaint();
            });
        }

        // Opens the Edit series window; afterwards the page and the main view show what was saved
        private void EditSeries(String seriesId){
            EditSeriesDialog.Show(this, seriesId, () -> ShowSeriesChanges(seriesId));
        }

        // Opens the assign picker; afterwards the page and the main view show the games that were added
        private void AssignGames(String seriesId){
            AssignGamesDialog.Show(this, seriesId, () -> ShowSeriesChanges(seriesId));
        }

        private void ShowSeriesChanges(String seriesId){
            ReloadMainView();
            SeriesViewData fresh = SeriesViewLoader.Load(seriesId);
            if(fresh != null){
                mySeriesView.Reload(fresh);
            }
            repaint();
        }

        // Opens a game's page. Back returns to wherever it was opened from
        private void ShowGame(LoadedReview review){
            flow.OpenGame();
            myGameInfoScreen = new GameInfoScreen(review, windowDimension, this::CloseGame);
            myGameInfoScreen.AddComponentsTo(this);
        }

        // Back from a game's page. The main view re-reads the database first, since the game may have been changed
        private void CloseGame(){
            myGameInfoScreen.RemoveComponentsFrom(this);
            AfterGameClosed(flow.CloseGame());
        }

        // Whatever the game was opened from re-reads the database, since the game may have been changed
        private void AfterGameClosed(Screen back){
            if(back == Screen.MAIN_VIEW){
                ReloadMainView();
            } else if(back == Screen.LIBRARY){
                myLibrary.Reload(LibraryGameLoader.Load(), SeriesDao.GetAllSeries());
            } else if(back == Screen.SERIES_VIEW){
                SeriesViewData fresh = SeriesViewLoader.Load(mySeriesView.GetSeriesId());
                if(fresh == null){
                    CloseSeries();
                } else {
                    mySeriesView.Reload(fresh);
                }
            }
        }

        private void ReloadMainView(){
            myMainView.Reload(MainViewLoader.Load(layout.GetOverview().GetRowsPerSection(), layout.GetOverview().GetTopPickSlots()));
        }

        private void ReloadAndRepaint(){
            ReloadMainView();
            if(flow.Current() == Screen.LIBRARY){
                myLibrary.Reload(LibraryGameLoader.Load(), SeriesDao.GetAllSeries());
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Clears the panel before repainting
            g.setColor(Theme.Current.GetBackground());
            g.fillRect(0, 0, getWidth(), getHeight());

            switch(flow.Current()){
                case MAIN_VIEW:
                    myMainView.Paint(g, mousePos, getSize());
                    break;
                case FRONT_PAGE:
                    myFrontPage.paint(g, mousePos);
                    break;
                case SERIES_VIEW:
                    mySeriesView.Paint(g, mousePos, getSize());
                    break;
                case LIBRARY:
                    myLibrary.Paint(g, mousePos, getSize());
                    break;
                case ACHIEVEMENTS:
                    myAchievements.Paint(g, mousePos, getSize());
                    break;
                case SETTINGS:
                    mySettings.Paint(g, mousePos, getSize());
                    break;
                case GAME_INFO_SCREEN:
                    if(editingGame || myGameDetail == null){
                        if(myGameInfoScreen != null){
                            myGameInfoScreen.paint(g, mousePos, windowDimension);
                        }
                    } else {
                        myGameDetail.Paint(g, mousePos, getSize());
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected Screen: " + flow.Current());
            }

        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // Intentionally unused: AWT only synthesizes mouseClicked when press+release
            // happen with zero pixel movement between them, so it's unreliable for real
            // mouse/trackpad input. Hit-testing is handled in mousePressed instead, which
            // always fires the instant the button goes down.
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // Just note what the press landed on; the actual action only fires on release,
            // and only if the release is still on the same target (see mouseReleased)
            switch (flow.Current()) {
                case MAIN_VIEW:
                    myMainView.Press(mousePos);
                    break;
                case FRONT_PAGE:
                    pressedCell = myFrontPage.GetCellAt(mousePos);
                    break;
                case SERIES_VIEW:
                    mySeriesView.Press(mousePos);
                    break;
                case LIBRARY:
                    myLibrary.Press(mousePos);
                    break;
                case ACHIEVEMENTS:
                    myAchievements.Press(mousePos);
                    break;
                case SETTINGS:
                    mySettings.Press(mousePos);
                    break;
                case GAME_INFO_SCREEN:
                    if(!editingGame && myGameDetail != null){
                        myGameDetail.Press(mousePos);
                        break;
                    }
                    myGameInfoScreen.NotifyBackPressed(mousePos);
                    pressedOnEditButton = myGameInfoScreen.WasEditClicked(mousePos);
                    pressedOnSaveButton = myGameInfoScreen.WasSaveClicked(mousePos);
                    pressedOnCancelButton = myGameInfoScreen.WasCancelClicked(mousePos);
                    pressedOnSectionsButton = myGameInfoScreen.WasSectionsClicked(mousePos);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            switch (flow.Current()) {
                case MAIN_VIEW:
                    myMainView.Release(mousePos);
                    break;
                case SERIES_VIEW:
                    mySeriesView.Release(mousePos);
                    break;
                case LIBRARY:
                    myLibrary.Release(mousePos);
                    break;
                case ACHIEVEMENTS:
                    myAchievements.Release(mousePos);
                    break;
                case SETTINGS:
                    mySettings.Release(mousePos);
                    break;
                case FRONT_PAGE:
                    if(pressedCell != null && pressedCell == myFrontPage.GetCellAt(mousePos)){
                        LoadedReview clickedReview = myFrontPage.CheckWhichCellWasClicked(mousePos);
                        if(clickedReview != null){
                            ShowGame(clickedReview);
                        }
                    }
                    break;
                case GAME_INFO_SCREEN:
                    if(!editingGame && myGameDetail != null){
                        myGameDetail.Release(mousePos);
                        break;
                    }
                    myGameInfoScreen.NotifyBackReleased(mousePos);
                    if(pressedOnEditButton && myGameInfoScreen.WasEditClicked(mousePos)){
                        myGameInfoScreen.EnterEditMode();
                    } else if(pressedOnSaveButton && myGameInfoScreen.WasSaveClicked(mousePos)){
                        myGameInfoScreen.SaveChanges();
                    } else if(pressedOnCancelButton && myGameInfoScreen.WasCancelClicked(mousePos)){
                        myGameInfoScreen.CancelChanges();
                    } else if(pressedOnSectionsButton && myGameInfoScreen.WasSectionsClicked(mousePos)){
                        myGameInfoScreen.ChooseSections();
                    }
                    break;
                default:
                    break;
            }
            pressedCell = null;
            pressedOnEditButton = false;
            pressedOnSaveButton = false;
            pressedOnCancelButton = false;
            pressedOnSectionsButton = false;
            repaint(); // Reflect any state change immediately, don't wait for the next mouseMoved
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            mousePos = e.getPoint();
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mousePos = e.getPoint();
            repaint();
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            // GameInfoScreen's field list handles its own mouse wheel internally
            // (see AutoGrowFieldList); the main view and the old list scroll their
            // custom-painted content from here
            if(flow.Current() == Screen.MAIN_VIEW){
                myMainView.Scroll(e.getWheelRotation());
                repaint();
            } else if(flow.Current() == Screen.FRONT_PAGE){
                myFrontPage.Scroll(e.getWheelRotation());
                repaint();
            } else if(flow.Current() == Screen.SERIES_VIEW){
                mySeriesView.Scroll(e.getWheelRotation());
                repaint();
            } else if(flow.Current() == Screen.LIBRARY){
                myLibrary.Scroll(e.getWheelRotation());
                repaint();
            } else if(flow.Current() == Screen.ACHIEVEMENTS){
                myAchievements.Scroll(e.getWheelRotation());
                repaint();
            } else if(flow.Current() == Screen.SETTINGS){
                mySettings.Scroll(e.getWheelRotation());
                repaint();
            } else if(flow.Current() == Screen.GAME_INFO_SCREEN && !editingGame && myGameDetail != null){
                myGameDetail.Scroll(e.getWheelRotation());
                repaint();
            }
        }

        @Override
        public void componentResized(ComponentEvent e) {
            // windowDimension used to be a one-time snapshot taken right after pack();
            // keep it live so screens can reflow against the panel's actual current size
            windowDimension = getSize();
            ReflowAll();
            repaint();
        }

        @Override
        public void componentMoved(ComponentEvent e) { }

        @Override
        public void componentShown(ComponentEvent e) { }

        @Override
        public void componentHidden(ComponentEvent e) { }

        // Escape goes from the old list, the Library, Settings or a series' page back to the main view
        @Override
        public void keyPressed(KeyEvent e) {
            boolean typing = (flow.Current() == Screen.MAIN_VIEW && myMainView.IsTyping()) || (flow.Current() == Screen.LIBRARY && myLibrary.IsTyping());
            if(typing){
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
                    if(flow.Current() == Screen.LIBRARY){
                        myLibrary.StopTyping();
                    } else {
                        myMainView.StopTyping();
                    }
                    repaint();
                }
            } else if(e.getKeyCode() == KeyEvent.VK_ESCAPE && flow.Escape()){
                ReloadMainView();
                repaint();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) { }

        @Override
        public void keyTyped(KeyEvent e) {
            if(flow.Current() == Screen.MAIN_VIEW && myMainView.KeyTyped(e.getKeyChar())){
                repaint();
            } else if(flow.Current() == Screen.LIBRARY && myLibrary.KeyTyped(e.getKeyChar())){
                repaint();
            }
        }

    }

    public static void main(String[] args) throws Exception {
        // Setup Dao (future have a class to do all setup features simultaniously)
        GameReviewDao.Setup();

        // The layout settings have to be right before anything is drawn; say exactly what is wrong if they are not
        Layout layout;
        try {
            layout = LayoutLoader.Load(layoutPath, startTheme);
        } catch (LayoutException e) {
            JOptionPane.showMessageDialog(null, String.join("\n", e.GetProblems()), "ReviewMe: layout settings", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // The theme chosen in Settings last time (the built-in one when none was)
        Theme saved = SettingsController.SavedTheme();
        if(saved != null){
            Theme.Current = saved;
        }

        MainViewData data = MainViewLoader.Load(layout.GetOverview().GetRowsPerSection(), layout.GetOverview().GetTopPickSlots());

        // Swing GUI work (creation, events, painting) all belongs on the EDT
        SwingUtilities.invokeLater(() -> new Main(layout, data));
    }

    private Main(Layout layout, MainViewData data) {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mousePos = new Point(-1, -1); // Off-screen default so contains(mousePos) doesn't NPE before the first mouseMoved
        canvas = new App(layout, data);
        this.setContentPane(canvas);
        this.pack();
        this.setMinimumSize(new Dimension(layout.GetWindow().GetMinWidth(), layout.GetWindow().GetMinHeight()));

        this.setVisible(true);
        // Get dimension of window to pass through and use
        windowDimension = canvas.getSize();
        canvas.ReflowAll(); // Correct the initial estimate (preferred size) to the real packed size
        canvas.requestFocusInWindow(); // So Escape works straight away
    }

    // Get mouse position
    public Point GetMousePos(){
        return mousePos;
    }

}
