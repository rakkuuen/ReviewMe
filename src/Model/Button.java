package Model;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;


public class Button extends Rectangle{
    int sizeX;
    int sizeY;
    int archWAndH;
    String text;
    boolean enabled = true;

    public Button(int sizeX, int sizeY, int locX, int locY, int archWAndH, String text){
        super(0, 0, sizeX, sizeY);
        this.x = locX;
        this.y = locY;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.archWAndH = archWAndH;
        this.text = text;
    }

    public void SetEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public boolean IsEnabled(){
        return enabled;
    }

    // Disabled buttons aren't clickable - callers checking contains() for hit-testing
    // get this for free without needing their own enabled checks
    @Override
    public boolean contains(Point p){
        return enabled && super.contains(p);
    }

    public void paint(Graphics g, Point mousePos){
        ButtonTheme theme = Theme.Current.GetButton();

        // Hover Colour - uses super.contains() since our own contains() gates on enabled
        if(!enabled){
            g.setColor(theme.GetDisabled());
        } else if(super.contains(mousePos)){
            g.setColor(theme.GetHover());
        } else {
            g.setColor(theme.GetMain());
        }
        g.fillRoundRect(x, y, sizeX, sizeY, archWAndH, archWAndH);

        // Button border
        g.setColor(theme.GetBorder());
        g.drawRoundRect(x, y, sizeX, sizeY, archWAndH, archWAndH);

         // Draw button text, centered on the actual glyph ink rather than the font's
         // abstract ascent/descent metrics - different fonts (e.g. Consolas vs Arial)
         // reserve very different proportions of metric space for accents/diacritics
         // that aren't in play here, so metric-based centering looks off font to font
         Font font = theme.GetFont();
         g.setColor(theme.GetText());
         g.setFont(font);
         Graphics2D g2 = (Graphics2D) g;
         FontRenderContext frc = g2.getFontRenderContext();
         Rectangle2D ink = font.createGlyphVector(frc, text).getVisualBounds();

         double buttonCenterX = x + sizeX / 2.0;
         double buttonCenterY = y + sizeY / 2.0;
         int textX = (int) Math.round(buttonCenterX - ink.getWidth() / 2 - ink.getX());
         int textY = (int) Math.round(buttonCenterY - ink.getHeight() / 2 - ink.getY());
         g.drawString(text, textX, textY);
    }
}
