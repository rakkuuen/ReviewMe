package Screens.MainView;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import Model.Layout.LayoutMath;
import Model.Layout.ShellLayout;
import Model.Themes.NavTheme;
import Model.Themes.Theme;

// A row of buttons, each with a label and an optional count in a small badge, optionally led by a caption. Used for the State row,
// the tab bar and the header's actions. Knows nothing about what the buttons mean: it is given them, draws them, and reports where
// each one went (HitMap). Sizes come from the layout, looks from the theme at the moment of painting. A row that doesn't fit the
// width it is given carries on underneath, lined up under its first button
public class NavStrip {
    private final ShellLayout layout;

    public NavStrip(ShellLayout layout){
        this.layout = layout;
    }

    // How wide the strip is if it all sits on one line, caption included. caption may be null
    public int Width(Graphics2D g, String caption, List<NavEntry> entries){
        NavTheme theme = Theme.Current.GetNav();
        int width = 0;
        if(caption != null){
            width += g.getFontMetrics(Theme.Current.GetHeading().GetFont()).stringWidth(caption) + layout.GetItemGap();
        }
        for(int i = 0; i < entries.size(); i++){
            width += EntryWidth(g, theme, entries.get(i));
            width += i + 1 < entries.size() ? layout.GetItemGap() : 0;
        }
        return width;
    }

    // Draws the strip with its top left corner at (x, y), every button the given height, wrapping to a new line when a button would
    // go past maxWidth (Integer.MAX_VALUE for never). Returns the height all of it took
    public int Paint(Graphics2D g, int x, int y, int height, int maxWidth, String caption, List<NavEntry> entries, Point mouse, HitMap hits){
        NavTheme theme = Theme.Current.GetNav();
        int left = x;
        if(caption != null){
            Font captionFont = Theme.Current.GetHeading().GetFont();
            FontMetrics captionMetrics = g.getFontMetrics(captionFont);
            g.setFont(captionFont);
            g.setColor(Theme.Current.GetHeading().GetColour());
            g.drawString(caption, left, Baseline(y, height, captionMetrics));
            left += captionMetrics.stringWidth(caption) + layout.GetItemGap();
        }

        int lineStart = left;
        int top = y;
        for(NavEntry entry : entries){
            int width = EntryWidth(g, theme, entry);
            if(left > lineStart && left - x + width > maxWidth){
                top += height + layout.GetItemGap();
                left = lineStart;
            }
            Rectangle box = new Rectangle(left, top, width, height);
            PaintEntry(g, theme, entry, box, mouse);
            if(entry.IsEnabled() && entry.GetAction() != null){
                hits.Add(entry.GetKey(), box, entry.GetAction());
            }
            left += width + layout.GetItemGap();
        }
        return top + height - y;
    }

    private int EntryWidth(Graphics2D g, NavTheme theme, NavEntry entry){
        FontMetrics metrics = g.getFontMetrics(theme.GetFont());
        int width = layout.GetItemPaddingX() + metrics.stringWidth(entry.GetLabel()) + layout.GetItemPaddingX();
        if(entry.GetCount() != null){
            width += layout.GetItemGap() + BadgeWidth(metrics, entry);
        }
        return width;
    }

    private int BadgeWidth(FontMetrics metrics, NavEntry entry){
        int sidePadding = LayoutMath.Half(layout.GetItemPaddingX());
        return sidePadding + metrics.stringWidth(String.valueOf(entry.GetCount())) + sidePadding;
    }

    // The outline is drawn inside the box, so the last pixel row and column of the button aren't cut off by a clip at the box's edge
    private void PaintEntry(Graphics2D g, NavTheme theme, NavEntry entry, Rectangle box, Point mouse){
        boolean hovered = entry.IsEnabled() && box.contains(mouse);
        int width = box.width - 1;
        int height = box.height - 1;
        g.setColor(entry.IsSelected() ? theme.GetSelected() : hovered ? theme.GetHover() : theme.GetMain());
        g.fillRoundRect(box.x, box.y, width, height, theme.GetCornerArc(), theme.GetCornerArc());
        g.setColor(theme.GetBorder());
        g.drawRoundRect(box.x, box.y, width, height, theme.GetCornerArc(), theme.GetCornerArc());

        FontMetrics metrics = g.getFontMetrics(theme.GetFont());
        g.setFont(theme.GetFont());
        g.setColor(TextColour(theme, entry));
        int baseline = Baseline(box.y, box.height, metrics);
        g.drawString(entry.GetLabel(), box.x + layout.GetItemPaddingX(), baseline);

        if(entry.GetCount() != null){
            int badgeWidth = BadgeWidth(metrics, entry);
            int badgeHeight = metrics.getHeight();
            int badgeX = box.x + layout.GetItemPaddingX() + metrics.stringWidth(entry.GetLabel()) + layout.GetItemGap();
            int badgeY = box.y + LayoutMath.Centre(box.height, badgeHeight);
            int badgeArc = theme.GetCornerArc() == 0 ? 0 : badgeHeight;
            g.setColor(theme.GetCountBackground());
            g.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, badgeArc, badgeArc);
            g.setColor(theme.GetCountText());
            g.drawString(String.valueOf(entry.GetCount()), badgeX + LayoutMath.Half(layout.GetItemPaddingX()), baseline);
        }
    }

    private Color TextColour(NavTheme theme, NavEntry entry){
        if(!entry.IsEnabled()){
            return theme.GetDisabledText();
        }
        return entry.IsSelected() ? theme.GetSelectedText() : theme.GetText();
    }

    // The line text sits on, so a line of this font is centred in a box of the given height
    private int Baseline(int top, int height, FontMetrics metrics){
        return top + LayoutMath.Centre(height, metrics.getHeight()) + metrics.getAscent();
    }
}
