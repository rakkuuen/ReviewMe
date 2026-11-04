package Screens.RichText;

import Controller.BackEnd.RichText.RunStyle;
import Model.Themes.RichTheme;
import Model.Themes.Theme;
import java.awt.Color;
import java.awt.Font;

// What a run's style looks like in the current theme: its font (body, serif or mono, with bold and italic on top) and its colour
final class RichStyles {
    private RichStyles(){
    }

    // The font for a run. body is the field's own font; its size is what serif and mono are tuned against by the theme
    static Font FontOf(RunStyle style, Font body){
        RichTheme rich = Theme.Current.GetRich();
        Font base = body;
        if(RichTheme.serifKey.equals(style.GetFont())){
            base = rich.GetSerif();
        } else if(RichTheme.monoKey.equals(style.GetFont())){
            base = rich.GetMono();
        }
        int flags = (style.IsBold() ? Font.BOLD : Font.PLAIN) | (style.IsItalic() ? Font.ITALIC : Font.PLAIN);
        return base.deriveFont(flags);
    }

    // The colour for a run: a picked colour wins, then links, then bold's brighter text, then the plain text colour
    static Color ColourOf(RunStyle style, Color text){
        RichTheme rich = Theme.Current.GetRich();
        Color picked = style.GetColour() == null ? null : rich.GetPaletteColour(style.GetColour());
        if(picked != null){
            return picked;
        }
        if(style.GetLink() != null){
            return rich.GetLink();
        }
        return style.IsBold() ? rich.GetBoldText() : text;
    }
}
