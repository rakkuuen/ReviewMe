package Model;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;



public class GameCell extends Rectangle{
    public static final int width = 500;
    public static final int height = 70;

    int archWAndH;
    //String text;
    Color mainColor;
    Color hoverColor;
    BufferedImage myPicture;

    public GameCell(int locX, int locY, int archWAndH, String myPicture){
        super(locX, locY, width, height);
        this.x = locX;
        this.y = locY;
        this.archWAndH = archWAndH;
        mainColor = Color.WHITE;
        hoverColor = Color.GRAY;
        LoadImage(myPicture);
    }

    public void paint(Graphics g, Point mousePos){

        // Hover Colour
        if(contains(mousePos)){
            g.setColor(hoverColor);
        } else {
            g.setColor(mainColor);
        }
        g.fillRoundRect(x, y, width, height, archWAndH, archWAndH);

        // Button border
        g.setColor(Color.BLACK);
        g.drawRoundRect(x, y, width, height, archWAndH, archWAndH);

        // Draw button text
        // g.setColor(Color.WHITE);
        // g.setFont(new Font("Arial", Font.BOLD, 18));
        // FontMetrics fm = g.getFontMetrics();
        // int textWidth = fm.stringWidth(text);
        // int textHeight = fm.getAscent();
        // int textX = x + (width - textWidth) / 2;
        // int textY = y + (height + textHeight) / 2 - 4; // Adjust for baseline
        // g.drawString(text, textX, textY);

        // Draw the image icon to the right of the rectangle
        if (myPicture != null) {
            int iconX = x + 10; // 10 pixels padding to the right of the rectangle
            int iconY = y + (height/6); // Center vertically
            g.drawImage(myPicture, iconX, iconY, 50, 50, null);
        }

    }

    void LoadImage(String myPicture){
        try{
            File file = new File(myPicture);
            if(!file.exists()){
                throw new IOException("File does not exist: " + file.getAbsolutePath());
            }
            else if (!file.canRead()){
                throw new IOException("Cannot read file: " + file.getAbsolutePath());
            }            
            this.myPicture = ImageIO.read(file);


        }
        catch(IOException e){
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public BufferedImage GetIcon(){
        return this.myPicture;
    }
    
}
