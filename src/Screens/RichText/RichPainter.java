package Screens.RichText;

import Controller.BackEnd.Emoji.EmojiRegistry;
import Controller.BackEnd.Emoji.EmojiSvg;
import Model.Layout.LayoutMath;
import Model.Layout.RichSizes;
import Model.Themes.RichTheme;
import Model.Themes.Theme;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;

// Draws a laid-out text block: letters, emoji sprites, bullets and numbers. A link is underlined, and the link under the mouse more
// heavily and in its full colour
final class RichPainter {
    private static final String xHeightSample = "x";

    private RichPainter(){
    }

    // x and y are where the block's top left goes. mouse is in the same space as x and y, or null
    static void Paint(Graphics2D g, RichLayout layout, int x, int y, Point mouse, RichSizes sizes, EmojiRegistry registry, EmojiPainter emoji){
        RichTheme rich = Theme.Current.GetRich();
        int hovered = mouse == null ? RichFragment.noLink : layout.LinkIdAt(mouse.x - x, mouse.y - y);
        for(RichFragment fragment : layout.GetFragments()){
            switch(fragment.GetKind()){
                case EMOJI:
                    PaintEmoji(g, fragment, x, y, registry, emoji, sizes);
                    break;
                case BULLET:
                    PaintBullet(g, fragment, x, y);
                    break;
                default:
                    PaintText(g, fragment, x, y);
                    break;
            }
            if(fragment.IsLink()){
                boolean over = fragment.GetLinkId() == hovered;
                g.setColor(over ? rich.GetLink() : rich.GetLinkUnderline());
                g.fillRect(x + fragment.GetX(), y + fragment.GetBaseline() + 1, fragment.GetWidth(), over ? sizes.GetLinkUnderlineHover() : sizes.GetLinkUnderline());
            }
        }
    }

    private static void PaintText(Graphics2D g, RichFragment fragment, int x, int y){
        g.setFont(fragment.GetFont());
        g.setColor(fragment.GetColour());
        g.drawString(fragment.GetText(), x + fragment.GetX(), y + fragment.GetBaseline());
    }

    // The sprite's box reaches the theme's distance below the baseline, so its top is a whole sprite above that
    private static void PaintEmoji(Graphics2D g, RichFragment fragment, int x, int y, EmojiRegistry registry, EmojiPainter emoji, RichSizes sizes){
        EmojiSvg svg = registry.Get(fragment.GetText());
        if(svg == null){
            return;
        }
        int below = sizes.GetEmojiBelowBaseline() * fragment.GetScale();
        emoji.Paint(g, svg, x + fragment.GetX(), y + fragment.GetBaseline() + below - EmojiPainter.SizeAt(fragment.GetScale()), fragment.GetScale());
    }

    // A round dot, centred on the middle of a lower case letter
    private static void PaintBullet(Graphics2D g, RichFragment fragment, int x, int y){
        Font font = fragment.GetFont();
        GlyphVector glyph = font.createGlyphVector(g.getFontRenderContext(), xHeightSample);
        int middle = LayoutMath.Half((int) Math.round(glyph.getVisualBounds().getHeight()));
        Object before = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(fragment.GetColour());
        g.fillOval(x + fragment.GetX(), y + fragment.GetBaseline() - middle - LayoutMath.Half(fragment.GetWidth()), fragment.GetWidth(), fragment.GetWidth());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, before == null ? RenderingHints.VALUE_ANTIALIAS_DEFAULT : before);
    }
}
