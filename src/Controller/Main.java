package Controller;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import Database.GameReviewDao;
import Screens.FrontPage;
import Screens.GameInfoScreen;
import Model.GameReview;

import java.time.Duration;
import java.time.Instant;



class Main extends JFrame{
    App canvas;
    Dimension windowDimension;
    Point mousePos;

    // This will track my screens to switch through
    enum Screen{
        FRONT_PAGE,
        GAME_INFO_SCREEN
    }

    class App extends JPanel implements MouseListener {
        private FrontPage myFrontPage = new FrontPage();
        private GameInfoScreen myGameInfoScreen;
        private Screen currentScreen = Screen.FRONT_PAGE;
    
        public App() {
            setPreferredSize(new Dimension(1024, 720));
            this.addMouseListener(this);

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
            // If mouse pos is contained in any button element do somehting here
            switch (currentScreen) {
                case FRONT_PAGE:
                    GameReview clickedReview = myFrontPage.CheckWhichCellWasClicked(mousePos);
                    if(clickedReview != null){
                        myGameInfoScreen = new GameInfoScreen(clickedReview);
                        currentScreen = Screen.GAME_INFO_SCREEN;
                    }
                    break;
                case GAME_INFO_SCREEN:
                    if(myGameInfoScreen.WasBackClicked(mousePos)){
                        currentScreen = Screen.FRONT_PAGE;
                    }
                default:
                    break;
            }
            myFrontPage.CheckWhichCellWasClicked(mousePos);
        }
    
        @Override
        public void mousePressed(MouseEvent e) {
            System.out.println("You pressed!!!!");

        }
    
        @Override
        public void mouseReleased(MouseEvent e) {
            System.out.println("You Released!!!!");
        }
    
        @Override
        public void mouseEntered(MouseEvent e) {
            System.out.println("You Entered!!!!");

        }
    
        @Override
        public void mouseExited(MouseEvent e) {
            System.out.println("You Exited!!!!");

        }

    }

    public static void main(String[] args) throws Exception {
        // Setup Dao (future have a class to do all setup features simultaniously)
        GameReviewDao.Setup();

        MarkdownProcessor myMdFilesProcessed = new MarkdownProcessor();

        //GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Main window = new Main();
        window.run();

    }

    private Main() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas = new App();
        this.setContentPane(canvas);
        this.pack();
        this.setVisible(true);
        // Get dimension of window to pass through and use
        windowDimension = canvas.getSize();
    }
    
    public void run() {
        while (true) {
            Instant startTime = Instant.now();
            this.repaint();
            // Get mouse position while app is running
            mousePos = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(mousePos, canvas);

            Instant endTime = Instant.now();
            long howLong = Duration.between(startTime, endTime).toMillis();
            try {
                Thread.sleep(20L - howLong);
            } catch (InterruptedException e) {
                System.out.println("thread was interrupted, but who cares?");
            } catch (IllegalArgumentException e) {
                System.out.println("application can't keep up with framerate");
            }
        }
    }

    // Get mouse position
    public Point GetMousePos(){
        return mousePos;
    }

}
