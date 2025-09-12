package Screens;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import Model.GameReview;
import Model.Button;

public class GameInfoScreen{
    private GameReview gameReview;
    private Button backButton;
    public GameInfoScreen(GameReview gameReview){
        this.gameReview = gameReview;
        backButton = new Button(100, 50, 20, 20, 10, "Back");
    }

    public void paint(Graphics g, Point mousePos, Dimension windowDimension){
        // Back button
        backButton.paint(g, mousePos);

        // Draw Title text
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 40));

        String title = gameReview.GetTitle();
        if (title == null || title.isEmpty()) {
            title = "Unknown Game"; // Fallback title
        }

        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(title);
        int textHeight = fm.getAscent();
        int textX = (windowDimension.width - textWidth) / 2; // Center horizontally;
        int textY = (windowDimension.height / 2) + textHeight / 2; // Center vertically
        g.drawString(title, textX, textY);
    }

    public boolean WasBackClicked(Point mousePos){
        System.out.println(backButton.contains(mousePos));
        return backButton.contains(mousePos);
    }
}