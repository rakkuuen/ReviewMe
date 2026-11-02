package Controller;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import Database.Dao.GameReviewDao;
import Screens.FrontPage;
import Screens.GameInfoScreen;
import Screens.MainView.MainViewScreen;
import Screens.MainView.SeriesActions;
import Screens.SeriesView.SeriesViewScreen;
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
import Model.Themes.ThemeLoader;



class Main extends JFrame{
    private static final String layoutPath = "Resources/Themes/layout.properties";
    private static final String startTheme = "Resources/Themes/default.properties";   // Its layout.* lines (if any) are laid over the layout file
    private static final String darkThemePath = "Resources/Themes/dark.properties";

    App canvas;
    Dimension windowDimension;
    Point mousePos;

    class App extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener, KeyListener {
        private final Layout layout;
        private MainViewScreen myMainView;
        private FrontPage myFrontPage;
        private GameInfoScreen myGameInfoScreen;
        private SeriesViewScreen mySeriesView;
        private final ScreenFlow flow = new ScreenFlow();
        private final RowActionHandler rowActions = new RowActionHandler(this, this::ReloadAndRepaint);

        // What the current press landed on, so release only acts if it's still on the same target
        private GameCell pressedCell;
        private boolean pressedOnEditButton;
        private boolean pressedOnSaveButton;
        private boolean pressedOnCancelButton;

        // TEMP: proves live theme switching actually works end to end - press T to toggle
        private Theme defaultTheme = Theme.Current;
        private Theme darkTheme;

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

            myMainView = new MainViewScreen(data, layout, flow::ShowFrontPage, this::OpenGameFromMainView, rowActions::Run, (game, items) -> rowActions.ShowMenu(game, items, mousePos),
                    new SeriesActions(this::OpenSeries, this::NewSeries));

            // The panel isn't packed/realized yet, so getSize() would still read (0,0) here -
            // use the preferred size as the initial layout estimate. ReflowAll() (called from
            // Main's constructor once the real windowDimension is known after pack()) corrects
            // it to the actual final size.
            myFrontPage = new FrontPage(getPreferredSize());

            try {
                darkTheme = ThemeLoader.Load(darkThemePath);
            } catch (java.io.IOException | java.awt.FontFormatException e) {
                System.err.println("Could not load dark theme for test toggle: " + e.getMessage());
            }
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
            LoadedReview review = ReviewLoader.Load(game.GetTitle());
            if(review != null){
                ShowGame(review);
            }
        }

        // Opens a series' page (from the Series tab). Back returns to the main view
        private void OpenSeries(String seriesId){
            SeriesViewData page = SeriesViewLoader.Load(seriesId);
            if(page == null){
                return;
            }
            mySeriesView = new SeriesViewScreen(page, layout, this::CloseSeries, this::NewSeries, id -> SeriesEditingComesLater(), id -> SeriesEditingComesLater(),
                    this::OpenGameFromMainView);
            flow.OpenSeries();
        }

        private void CloseSeries(){
            flow.CloseSeries();
            ReloadMainView();
        }

        // Asks for a name and template, makes the series, and opens its (empty) page
        private void NewSeries(){
            NewSeriesDialog.Show(this, id -> {
                ReloadMainView();
                OpenSeries(id);
                repaint();
            });
        }

        // Editing and assigning series comes with the next steps
        private void SeriesEditingComesLater(){
            JOptionPane.showMessageDialog(this, "Editing series and assigning games comes in the next step.", "Series", JOptionPane.INFORMATION_MESSAGE);
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
            Screen back = flow.CloseGame();
            if(back == Screen.MAIN_VIEW){
                ReloadMainView();
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
                case GAME_INFO_SCREEN:
                    if(myGameInfoScreen != null){
                        myGameInfoScreen.paint(g, mousePos, windowDimension);
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
                case GAME_INFO_SCREEN:
                    myGameInfoScreen.NotifyBackPressed(mousePos);
                    pressedOnEditButton = myGameInfoScreen.WasEditClicked(mousePos);
                    pressedOnSaveButton = myGameInfoScreen.WasSaveClicked(mousePos);
                    pressedOnCancelButton = myGameInfoScreen.WasCancelClicked(mousePos);
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
                case FRONT_PAGE:
                    if(pressedCell != null && pressedCell == myFrontPage.GetCellAt(mousePos)){
                        LoadedReview clickedReview = myFrontPage.CheckWhichCellWasClicked(mousePos);
                        if(clickedReview != null){
                            ShowGame(clickedReview);
                        }
                    }
                    break;
                case GAME_INFO_SCREEN:
                    myGameInfoScreen.NotifyBackReleased(mousePos);
                    if(pressedOnEditButton && myGameInfoScreen.WasEditClicked(mousePos)){
                        myGameInfoScreen.EnterEditMode();
                    } else if(pressedOnSaveButton && myGameInfoScreen.WasSaveClicked(mousePos)){
                        myGameInfoScreen.SaveChanges();
                    } else if(pressedOnCancelButton && myGameInfoScreen.WasCancelClicked(mousePos)){
                        myGameInfoScreen.CancelChanges();
                    }
                    break;
                default:
                    break;
            }
            pressedCell = null;
            pressedOnEditButton = false;
            pressedOnSaveButton = false;
            pressedOnCancelButton = false;
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

        // TEMP: proves live theme switching actually works end to end - press T to toggle.
        // Custom-painted stuff (Button/GameCell/field backgrounds) picks up Theme.Current
        // automatically on the next repaint; ReapplyTheme() handles the real Swing text
        // properties that don't work that way. Escape goes from the old list back to the main view
        @Override
        public void keyPressed(KeyEvent e) {
            boolean typing = flow.Current() == Screen.MAIN_VIEW && myMainView.IsTyping();
            if(typing){
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
                    myMainView.StopTyping();
                    repaint();
                }
            } else if(e.getKeyCode() == KeyEvent.VK_T && darkTheme != null){
                Theme.Current = (Theme.Current == defaultTheme) ? darkTheme : defaultTheme;
                if(myGameInfoScreen != null){
                    myGameInfoScreen.ReapplyTheme();
                }
                repaint();
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
        canvas.requestFocusInWindow(); // So the theme-toggle test hook (key T) works immediately
    }

    // Get mouse position
    public Point GetMousePos(){
        return mousePos;
    }

}
