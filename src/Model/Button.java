package Model;
import java.awt.*;


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

         // Draw button text
         g.setColor(theme.GetText());
         g.setFont(theme.GetFont());
         FontMetrics fm = g.getFontMetrics();
         int textWidth = fm.stringWidth(text);
         int textHeight = fm.getAscent();
         int textX = x + (sizeX - textWidth) / 2;
         int textY = y + (sizeY + textHeight) / 2 - 4; // Adjust for baseline
         g.drawString(text, textX, textY);
    }
}
