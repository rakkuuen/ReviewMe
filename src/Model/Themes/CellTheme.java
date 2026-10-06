package Model.Themes;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

public class CellTheme {
    private Color main, hover, text, border;
    private int cornerArc;
    private Font font;

    public CellTheme(Color main, Color hover, Color text, Color border, int cornerArc, Font font){
        this.main = main;
        this.hover = hover;
        this.text = text;
        this.border = border;
        this.cornerArc = cornerArc;
        this.font = font;
    }

    public Color GetMain(){ return main; }
    public void SetMain(Color c){ main = c; }

    public Color GetHover(){ return hover; }
    public void SetHover(Color c){ hover = c; }

    public Color GetText(){ return text; }
    public void SetText(Color c){ text = c; }

    public Color GetBorder(){ return border; }
    public void SetBorder(Color c){ border = c; }

    public int GetCornerArc(){ return cornerArc; }
    public void SetCornerArc(int a){ cornerArc = a; }

    public Font GetFont(){ return font; }
    public void SetFont(Font f){ font = f; }

    public List<Color> GetAllColours(){
        return Arrays.asList(main, hover, text, border);
    }
}
