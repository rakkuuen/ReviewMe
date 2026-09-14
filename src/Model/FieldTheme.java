package Model;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

public class FieldTheme {
    private Color background, readOnly, border, borderFocused, placeholder;
    private Font font;

    public FieldTheme(Color background, Color readOnly, Color border, Color borderFocused, Color placeholder, Font font){
        this.background = background;
        this.readOnly = readOnly;
        this.border = border;
        this.borderFocused = borderFocused;
        this.placeholder = placeholder;
        this.font = font;
    }

    public Color GetBackground(){ return background; }
    public void SetBackground(Color c){ background = c; }

    public Color GetReadOnly(){ return readOnly; }
    public void SetReadOnly(Color c){ readOnly = c; }

    public Color GetBorder(){ return border; }
    public void SetBorder(Color c){ border = c; }

    public Color GetBorderFocused(){ return borderFocused; }
    public void SetBorderFocused(Color c){ borderFocused = c; }

    public Color GetPlaceholder(){ return placeholder; }
    public void SetPlaceholder(Color c){ placeholder = c; }

    public Font GetFont(){ return font; }
    public void SetFont(Font f){ font = f; }

    public List<Color> GetAllColours(){
        return Arrays.asList(background, readOnly, border, borderFocused, placeholder);
    }
}
