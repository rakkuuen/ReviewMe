package Screens;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import Model.GameReview;

public class GameInfoScreen{
    GameReview gameReview;
    public GameInfoScreen(GameReview gameReview){
        this.gameReview = gameReview;
    }

    public void paint(Graphics g, Point mousePos, Dimension windowDimension){
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
        int textX = 300;
        int textY = 200; // Adjust for baseline
        g.drawString(title, textX, textY);
    }
}