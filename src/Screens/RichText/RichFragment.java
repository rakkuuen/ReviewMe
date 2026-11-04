package Screens.RichText;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

// One drawn piece of laid-out text: a run of characters, an emoji, a bullet or a list number. Positions are from the top left of the
// text block; baseline is where the letters sit. top and height are the whole line it is on, so a link covers its line
public class RichFragment {
    public enum Kind { TEXT, EMOJI, BULLET, NUMBER }

    public static final int noLink = -1;

    private final Kind kind;
    private final String text;
    private final int x;
    private final int baseline;
    private final int width;
    private final int top;
    private final int height;
    private final Font font;
    private final Color colour;
    private final int scale;
    private final String link;
    private final int linkId;

    RichFragment(Kind kind, String text, int x, int baseline, int width, int top, int height, Font font, Color colour, int scale, String link, int linkId){
        this.kind = kind;
        this.text = text;
        this.x = x;
        this.baseline = baseline;
        this.width = width;
        this.top = top;
        this.height = height;
        this.font = font;
        this.colour = colour;
        this.scale = scale;
        this.link = link;
        this.linkId = linkId;
    }

    public Kind GetKind(){ return kind; }

    // The characters; the emoji's name (no colons) for an emoji; the number with its point ("3.") for a list number
    public String GetText(){ return text; }

    public int GetX(){ return x; }
    public int GetBaseline(){ return baseline; }
    public int GetWidth(){ return width; }
    public int GetTop(){ return top; }
    public int GetHeight(){ return height; }
    public Font GetFont(){ return font; }
    public Color GetColour(){ return colour; }

    // Pixels per emoji grid square (emoji only)
    public int GetScale(){ return scale; }

    // The address this belongs to, or null
    public String GetLink(){ return link; }

    // Which link this is part of (fragments of one link on several lines share it), or noLink
    public int GetLinkId(){ return linkId; }

    public boolean IsLink(){ return link != null; }

    public Rectangle Bounds(){
        return new Rectangle(x, top, width, height);
    }
}
