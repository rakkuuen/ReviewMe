package Controller;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.List;

import Database.GameReviewDao;
import Factory.GameCellFactory;
import Model.Button;
import Model.GameCell;

import java.io.File;
import java.time.Duration;
import java.time.Instant;



class Main extends JFrame{
    App canvas;
    Point mousePos;
    class App extends JPanel implements MouseListener {

        boolean stageBuilt = false;
        Button mainButton;
        Button anotherButton;
        JButton button;
        GameCell fallout;
        GameCellFactory myFactoryOfCells = new GameCellFactory();
        List<GameCell> myCellsToPaint = myFactoryOfCells.GetAllGameCells();


        public App() {
            setPreferredSize(new Dimension(1024, 720));
            this.addMouseListener(this);
            // I should have a frontpage class made here that will set up where all elements are on the page
            // and allow the user to move onto other pages
            // I.e., I dont want to initiliase any buttons here like im doing below!!!!

            mainButton = new Button(200, 50, 100, 100, 10, "Hi there");
            anotherButton = new Button(300, 100, 400, 100, 30,"Wazzaaaappp");
            fallout = new GameCell("fallout", 500, 300, "Resources/Images/fallout-4-icon-6.png");

            

        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Clears the panel before repainting
            // Paint all of the cells to screen after setting them up in cell factory
            for(GameCell cell : myCellsToPaint){
                if(cell != null){
                    cell.paint(g, mousePos);
                }
            }

        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // If mouse pos is contained in any button element do somehting here
            // System.out.println("You clicked!!!!");
            if(mainButton.contains(mousePos));
            for(GameCell cell : myCellsToPaint){
                if(cell.contains(mousePos)){
                    System.out.println("You clicked: " + cell.gameTitle);
                }
            }
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
