package Model.Themes;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

// The look of the buttons that move around the app: the State row, the tabs, and the header's actions. Each shows a label and
// optionally a count in a small badge. The selected one stands out, and one that can't be used yet is muted
public class NavTheme {
    private Color main, hover, selected, text, selectedText, disabledText, border, countBackground, countText;
    private int cornerArc;   // Arc diameter in px, 0 = square corners
    private Font font;

    public NavTheme(Color main, Color hover, Color selected, Color text, Color selectedText, Color disabledText, Color border,
                    Color countBackground, Color countText, int cornerArc, Font font){
        this.main = main;
        this.hover = hover;
        this.selected = selected;
        this.text = text;
        this.selectedText = selectedText;
        this.disabledText = disabledText;
        this.border = border;
        this.countBackground = countBackground;
        this.countText = countText;
        this.cornerArc = cornerArc;
        this.font = font;
    }

    public Color GetMain(){ return main; }
    public void SetMain(Color c){ main = c; }

    public Color GetHover(){ return hover; }
    public void SetHover(Color c){ hover = c; }

    public Color GetSelected(){ return selected; }
    public void SetSelected(Color c){ selected = c; }

    public Color GetText(){ return text; }
    public void SetText(Color c){ text = c; }

    public Color GetSelectedText(){ return selectedText; }
    public void SetSelectedText(Color c){ selectedText = c; }

    public Color GetDisabledText(){ return disabledText; }
    public void SetDisabledText(Color c){ disabledText = c; }

    public Color GetBorder(){ return border; }
    public void SetBorder(Color c){ border = c; }

    public Color GetCountBackground(){ return countBackground; }
    public void SetCountBackground(Color c){ countBackground = c; }

    public Color GetCountText(){ return countText; }
    public void SetCountText(Color c){ countText = c; }

    public int GetCornerArc(){ return cornerArc; }
    public void SetCornerArc(int a){ cornerArc = a; }

    public Font GetFont(){ return font; }
    public void SetFont(Font f){ font = f; }

    public List<Color> GetAllColours(){
        return Arrays.asList(main, hover, selected, text, selectedText, disabledText, border, countBackground, countText);
    }
}
