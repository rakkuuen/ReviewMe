package Model;
import java.awt.*;


public class Button extends Rectangle{
    int sizeX;
    int sizeY;
    int archWAndH;
    String text;
    Color mainColor;
    Color hoverColor;

    public Button(int sizeX, int sizeY, int locX, int locY, int archWAndH, String text){
        super(0, 0, sizeX, sizeY);
        this.x = locX;
        this.y = locY;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.archWAndH = archWAndH;
        this.text = text;
        mainColor = Color.BLUE;
        hoverColor = Color.RED;
    }
    
    public void paint(Graphics g, Point mousePos){

        // Hover Colour
        if(contains(mousePos)){
            g.setColor(hoverColor);
        } else {
            g.setColor(mainColor);
        }
        g.fillRoundRect(x, y, sizeX, sizeY, archWAndH, archWAndH);

        // Button border
        g.setColor(Color.BLACK);
        g.drawRoundRect(x, y, sizeX, sizeY, archWAndH, archWAndH);

         // Draw button text
         g.setColor(Color.WHITE);
         g.setFont(new Font("Arial", Font.BOLD, 18));
         FontMetrics fm = g.getFontMetrics();
         int textWidth = fm.stringWidth(text);
         int textHeight = fm.getAscent();
         int textX = x + (sizeX - textWidth) / 2;
         int textY = y + (sizeY + textHeight) / 2 - 4; // Adjust for baseline
         g.drawString(text, textX, textY);
    }
}
