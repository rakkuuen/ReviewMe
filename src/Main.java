import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import javax.swing.JFrame;
import javax.swing.JPanel;

class Main extends JFrame{
    class App extends JPanel implements MouseListener {

        boolean stageBuilt = false;

        public App() {
            setPreferredSize(new Dimension(1024, 720));
            this.addMouseListener(this);
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
