import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.time.Duration;
import java.time.Instant;



class Main extends JFrame{
    class App extends JPanel implements MouseListener {

        boolean stageBuilt = false;
        // Button mainButton;
        // Point mousePos;
        JButton button;

        public App() {
            setPreferredSize(new Dimension(1024, 720));
            this.addMouseListener(this);

            // mainButton = new Button(200, 50, "Hi there");
            // mainButton.x = 100;
            // mainButton.y = 100;

            // // // Initialize mouse position for hover detection
            // mousePos = new Point(-1, -1);



            // Add a button
            button = new JButton("Click Me!");
            button.setFont(new Font("Arial", Font.PLAIN, 20));

            // Add ActionListener to the button
            button.addActionListener(e -> System.out.println("Button Clicked!"));

            // Add the button to this panel
            this.setLayout(new FlowLayout()); // Optional layout manager
            this.add(button);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            System.out.println("You clicked!!!!");
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
        System.out.println("Hello, World!");
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Main window = new Main();
        window.run();

    }

    private Main() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        App canvas = new App();
        this.setContentPane(canvas);
        this.pack();
        this.setVisible(true);
      }
    
      public void run() {
        // while (true) {
        //   Instant startTime = Instant.now();
        //   this.repaint();
        //   Instant endTime = Instant.now();
        //   long howLong = Duration.between(startTime, endTime).toMillis();
        //   try {
        //     Thread.sleep(20L - howLong);
        //   } catch (InterruptedException e) {
        //     System.out.println("thread was interrupted, but who cares?");
        //   } catch (IllegalArgumentException e) {
        //     System.out.println("application can't keep up with framerate");
        //   }
        // }
      }

}
