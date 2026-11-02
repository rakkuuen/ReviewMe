package Screens.MainView;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import Model.Layout.LayoutMath;
import Model.Themes.TextStyle;
import Model.Themes.Theme;

// Stands in for a tab's content until that tab is built: a line of words in the middle of the space
public class PlaceholderBody implements BodyPainter {
    private final String text;

    public PlaceholderBody(String text){
        this.text = text;
    }

    @Override
    public int Paint(Graphics2D g, Rectangle viewport, Point mouse, HitMap hits){
        TextStyle style = Theme.Current.GetHeading();
        FontMetrics metrics = g.getFontMetrics(style.GetFont());
        g.setFont(style.GetFont());
        g.setColor(style.GetColour());
        int x = viewport.x + LayoutMath.Centre(viewport.width, metrics.stringWidth(text));
        int baseline = viewport.y + LayoutMath.Centre(viewport.height, metrics.getHeight()) + metrics.getAscent();
        g.drawString(text, x, baseline);
        return viewport.height;
    }
}
