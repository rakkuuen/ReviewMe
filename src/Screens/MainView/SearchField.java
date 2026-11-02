package Screens.MainView;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import Model.Layout.LayoutMath;
import Model.Layout.ShellLayout;
import Model.Themes.FieldTheme;
import Model.Themes.Theme;

// A box to type in, drawn from the theme's field look: the words so far (or a hint when there are none), an outline that changes
// while it has the keys, and a caret after the last letter. When the words are wider than the box the end of them shows
public class SearchField {
    private final ShellLayout layout;

    public SearchField(ShellLayout layout){
        this.layout = layout;
    }

    public void Paint(Graphics2D g, Rectangle box, String text, boolean focused, String hint){
        FieldTheme field = Theme.Current.GetField();
        int width = box.width - 1;
        int height = box.height - 1;
        g.setColor(field.GetBackground());
        g.fillRoundRect(box.x, box.y, width, height, field.GetCornerArc(), field.GetCornerArc());
        g.setColor(focused ? field.GetBorderFocused() : field.GetBorder());
        g.drawRoundRect(box.x, box.y, width, height, field.GetCornerArc(), field.GetCornerArc());

        FontMetrics metrics = g.getFontMetrics(field.GetFont());
        int pad = layout.GetItemPaddingX();
        int room = box.width - pad - pad;
        int baseline = TextLine.Baseline(box.y, box.height, metrics);

        Graphics2D inside = (Graphics2D) g.create();
        try {
            inside.clipRect(box.x + pad, box.y, Math.max(0, room), box.height);
            inside.setFont(field.GetFont());
            if(text.isEmpty()){
                inside.setColor(field.GetPlaceholder());
                inside.drawString(hint, box.x + pad, baseline);
            } else {
                inside.setColor(field.GetText());
                inside.drawString(text, box.x + pad - Overflow(metrics, text, room), baseline);
            }
            if(focused){
                int caretX = box.x + pad + Math.max(0, Math.min(metrics.stringWidth(text), room - 1));
                inside.setColor(field.GetCaret());
                inside.drawLine(caretX, box.y + LayoutMath.Centre(box.height, metrics.getHeight()), caretX, box.y + LayoutMath.Centre(box.height, metrics.getHeight()) + metrics.getHeight());
            }
        } finally {
            inside.dispose();
        }
    }

    // How far the words have to slide left so their end is in the box
    private int Overflow(FontMetrics metrics, String text, int room){
        return Math.max(0, metrics.stringWidth(text) - room);
    }
}
