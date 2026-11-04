package Controller.BackEnd.RichText;

import java.util.Objects;

// How a piece of text looks: bold, italic, a colour and a font (each the palette key, like "amber" or "serif", never a hex colour) and
// a link address. Null means none. Immutable: each With... gives a changed copy
public class RunStyle {
    public static final RunStyle plain = new RunStyle(false, false, null, null, null);

    private final boolean bold;
    private final boolean italic;
    private final String colour;
    private final String font;
    private final String link;

    private RunStyle(boolean bold, boolean italic, String colour, String font, String link){
        this.bold = bold;
        this.italic = italic;
        this.colour = colour;
        this.font = font;
        this.link = link;
    }

    public boolean IsBold(){ return bold; }
    public boolean IsItalic(){ return italic; }
    public String GetColour(){ return colour; }
    public String GetFont(){ return font; }
    public String GetLink(){ return link; }

    public boolean IsPlain(){ return equals(plain); }

    public RunStyle WithBold(boolean value){ return new RunStyle(value, italic, colour, font, link); }
    public RunStyle WithItalic(boolean value){ return new RunStyle(bold, value, colour, font, link); }
    public RunStyle WithColour(String value){ return new RunStyle(bold, italic, value, font, link); }
    public RunStyle WithFont(String value){ return new RunStyle(bold, italic, colour, value, link); }
    public RunStyle WithLink(String value){ return new RunStyle(bold, italic, colour, font, value); }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof RunStyle)){
            return false;
        }
        RunStyle that = (RunStyle) other;
        return bold == that.bold && italic == that.italic && Objects.equals(colour, that.colour) && Objects.equals(font, that.font) && Objects.equals(link, that.link);
    }

    @Override
    public int hashCode(){
        return Objects.hash(bold, italic, colour, font, link);
    }

    @Override
    public String toString(){
        return "RunStyle[" + (bold ? "bold " : "") + (italic ? "italic " : "") + (colour != null ? "colour=" + colour + " " : "") + (font != null ? "font=" + font + " " : "") + (link != null ? "link=" + link : "") + "]";
    }
}
