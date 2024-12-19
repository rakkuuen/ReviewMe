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
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(gameReview.GetTitle());
        int textHeight = fm.getAscent();
        double textX = 300;
        double textY = 200; // Adjust for baseline
        g.drawString(gameReview.GetTitle(), (int)textX, (int)textY);
    }
}