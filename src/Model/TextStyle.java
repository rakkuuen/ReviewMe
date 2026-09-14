package Model;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

// Shared shape for a theme part that's just a colour + font (title, headings, etc.)
public class TextStyle {
    private Color colour;
    private Font font;

    public TextStyle(Color colour, Font font){
        this.colour = colour;
        this.font = font;
    }

    public Color GetColour(){ return colour; }
    public void SetColour(Color colour){ this.colour = colour; }

    public Font GetFont(){ return font; }
    public void SetFont(Font font){ this.font = font; }

    public List<Color> GetAllColours(){
        return Arrays.asList(colour);
    }
}
