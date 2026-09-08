package Controller;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import Database.GameReviewDao;
import Screens.FrontPage;
import Screens.GameInfoScreen;
import Model.GameReview;
import Model.GameCell;



class Main extends JFrame{
    App canvas;
    Dimension windowDimension;
    Point mousePos;

    // This will track my screens to switch through
    enum Screen{
        FRONT_PAGE,
        GAME_INFO_SCREEN
    }

    class App extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
        private FrontPage myFrontPage = new FrontPage();
        private GameInfoScreen myGameInfoScreen;
        private Screen currentScreen = Screen.FRONT_PAGE;

        // What the current press landed on, so release only acts if it's still on the same target
        private GameCell pressedCell;
        private boolean pressedOnBackButton;

        public App() {
            setPreferredSize(new Dimension(1024, 720));
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
            this.addMouseWheelListener(this);

            // fallout = new GameCell("fallout", 500, 300, "Resources/Images/fallout-4-icon-6.png");
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Clears the panel before repainting
            // This paints front page for now but will change when I have a screen manager
            //myFrontPage.paint(g, mousePos);
            
            switch(currentScreen){
                case FRONT_PAGE:
                    myFrontPage.paint(g, mousePos);
                    break;
                case GAME_INFO_SCREEN:
                    if(myGameInfoScreen != null){
                        myGameInfoScreen.paint(g, mousePos, windowDimension);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected Screen: " + currentScreen);
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
            switch (currentScreen) {
                case FRONT_PAGE:
                    pressedCell = myFrontPage.GetCellAt(mousePos);
                    break;
                case GAME_INFO_SCREEN:
                    pressedOnBackButton = myGameInfoScreen.WasBackClicked(mousePos);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            switch (currentScreen) {
                case FRONT_PAGE:
                    if(pressedCell != null && pressedCell == myFrontPage.GetCellAt(mousePos)){
                        GameReview clickedReview = myFrontPage.CheckWhichCellWasClicked(mousePos);
                        if(clickedReview != null){
                            myGameInfoScreen = new GameInfoScreen(clickedReview);
                            currentScreen = Screen.GAME_INFO_SCREEN;
                        }
                    }
                    break;
                case GAME_INFO_SCREEN:
                    if(pressedOnBackButton && myGameInfoScreen.WasBackClicked(mousePos)){
                        currentScreen = Screen.FRONT_PAGE;
                    }
                    break;
                default:
                    break;
            }
            pressedCell = null;
            pressedOnBackButton = false;
            repaint(); // Reflect any state change immediately, don't wait for the next mouseMoved
        }
    
        @Override
        public void mouseEntered(MouseEvent e) {
            System.out.println("You Entered!!!!");

        }
    
        @Override
        public void mouseExited(MouseEvent e) {
            System.out.println("You Exited!!!!");

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
            // GameInfoScreen scrolls itself via its own JScrollPane; only FrontPage's
            // custom-painted cells need scrolling driven manually here
            if(currentScreen == Screen.FRONT_PAGE){
                myFrontPage.Scroll(e.getWheelRotation());
                repaint();
            }
        }

    }

    public static void main(String[] args) throws Exception {
        // Setup Dao (future have a class to do all setup features simultaniously)
        GameReviewDao.Setup();

        MarkdownProcessor myMdFilesProcessed = new MarkdownProcessor();

        // Swing GUI work (creation, events, painting) all belongs on the EDT
        SwingUtilities.invokeLater(() -> new Main());
    }

    private Main() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mousePos = new Point(-1, -1); // Off-screen default so contains(mousePos) doesn't NPE before the first mouseMoved
        canvas = new App();
        this.setContentPane(canvas);
        this.pack();
        this.setVisible(true);
        // Get dimension of window to pass through and use
        windowDimension = canvas.getSize();
    }

    // Get mouse position
    public Point GetMousePos(){
        return mousePos;
    }

}
