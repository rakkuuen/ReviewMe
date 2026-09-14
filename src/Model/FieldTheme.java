package Model;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

public class FieldTheme {
    private Color background, readOnly, border, borderFocused, placeholder;
    private Color caret, selection, selectedText, text;
    private Font font;

    public FieldTheme(Color background, Color readOnly, Color border, Color borderFocused, Color placeholder,
            Color caret, Color selection, Color selectedText, Color text, Font font){
        this.background = background;
        this.readOnly = readOnly;
        this.border = border;
        this.borderFocused = borderFocused;
        this.placeholder = placeholder;
        this.caret = caret;
        this.selection = selection;
        this.selectedText = selectedText;
        this.text = text;
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

    public Color GetCaret(){ return caret; }
    public void SetCaret(Color c){ caret = c; }

    public Color GetSelection(){ return selection; }
    public void SetSelection(Color c){ selection = c; }

    public Color GetSelectedText(){ return selectedText; }
    public void SetSelectedText(Color c){ selectedText = c; }

    public Color GetText(){ return text; }
    public void SetText(Color c){ text = c; }

    public Font GetFont(){ return font; }
    public void SetFont(Font f){ font = f; }

    public List<Color> GetAllColours(){
        return Arrays.asList(background, readOnly, border, borderFocused, placeholder, caret, selection, selectedText, text);
    }
}
