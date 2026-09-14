package Model;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

public class ButtonTheme {
    private Color main, hover, disabled, text, border;
    private Font font;

    public ButtonTheme(Color main, Color hover, Color disabled, Color text, Color border, Font font){
        this.main = main;
        this.hover = hover;
        this.disabled = disabled;
        this.text = text;
        this.border = border;
        this.font = font;
    }

    public Color GetMain(){ return main; }
    public void SetMain(Color c){ main = c; }

    public Color GetHover(){ return hover; }
    public void SetHover(Color c){ hover = c; }

    public Color GetDisabled(){ return disabled; }
    public void SetDisabled(Color c){ disabled = c; }

    public Color GetText(){ return text; }
    public void SetText(Color c){ text = c; }

    public Color GetBorder(){ return border; }
    public void SetBorder(Color c){ border = c; }

    public Font GetFont(){ return font; }
    public void SetFont(Font f){ font = f; }

    public List<Color> GetAllColours(){
        return Arrays.asList(main, hover, disabled, text, border);
    }
}
