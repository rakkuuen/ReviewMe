package Screens.Achievements;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.AchievementView.AchievementFilter;
import Controller.BackEnd.AchievementView.AchievementItem;
import Controller.BackEnd.AchievementView.AchievementSort;
import Controller.BackEnd.AchievementView.AchievementViewBuilder;
import Controller.BackEnd.AchievementView.AchievementViewData;
import Model.Layout.Layout;
import Model.Layout.LayoutMath;
import Model.Layout.ListLayout;
import Model.Layout.OverviewLayout;
import Model.Layout.ShellLayout;
import Model.Themes.NavTheme;
import Model.Themes.TextStyle;
import Model.Themes.Theme;
import Screens.MainView.CardBox;
import Screens.MainView.HitMap;
import Screens.MainView.NavEntry;
import Screens.MainView.NavStrip;
import Screens.MainView.TextFit;
import Screens.MainView.TextLine;

// A game's achievements: Back, the game's name, a summary card (unlocked of total, percent, a bar, locked and hidden counts, last
// unlocked and rarest), All / Unlocked / Locked buttons with counts, an order button, then every achievement with its words, how rare
// it is and when it was unlocked. Draws what AchievementViewBuilder worked out; keeps only the filter and order picked. The page scrolls
public class AchievementsScreen {
    public static final String backKey = "achievements:back";
    public static final String sortKey = "achievements:sort";
    public static final String backWords = "Back";
    public static final String caption = "Achievements";
    public static final String unlockedCaption = "Unlocked";
    public static final String lastUnlockedWords = "Last unlocked";
    public static final String rarestWords = "Rarest";
    public static final String emptyWords = "No achievements to show.";
    public static final String lockedMark = "locked";
    public static final String unlockedWords = "unlocked";

    public static String FilterKey(AchievementFilter filter){ return "achievements:filter:" + filter.name(); }
    public static String RowKey(int index){ return "achievements:row:" + index; }

    private AchievementViewData data;
    private final Layout layout;
    private final Runnable onBack;
    private final HitMap hits = new HitMap();
    private final NavStrip strip;
    private AchievementFilter filter = AchievementFilter.ALL;
    private AchievementSort sort = AchievementSort.UNLOCK_DATE;
    private String pressedKey;
    private int scroll;
    private int contentHeight;
    private int viewportHeight;

    public AchievementsScreen(AchievementViewData data, Layout layout, Runnable onBack){
        this.data = data;
        this.layout = layout;
        this.onBack = onBack;
        this.strip = new NavStrip(layout.GetShell());
    }

    // Newer figures; the filter and order stay as picked
    public void Reload(AchievementViewData fresh){
        this.data = fresh;
    }

    public AchievementFilter GetFilter(){ return filter; }
    public AchievementSort GetSort(){ return sort; }

    public void Paint(Graphics graphics, Point mouse, Dimension size){
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            hits.Clear();
            ShellLayout shell = layout.GetShell();
            Rectangle area = new Rectangle(shell.GetPadding(), shell.GetPadding(), Math.max(0, size.width - shell.GetPadding() - shell.GetPadding()),
                    Math.max(0, size.height - shell.GetPadding() - shell.GetPadding()));
            viewportHeight = area.height;
            scroll = ClampScroll(scroll);
            int mark = hits.Size();
            int scrolledTo = scroll;
            PaintPage(g, area, mouse);
            if(scroll != scrolledTo){
                // The page turned out shorter than the scroll allowed: wipe that attempt and paint from where it can be
                hits.TruncateTo(mark);
                g.setClip(null);
                g.setColor(Theme.Current.GetBackground());
                g.fillRect(0, 0, size.width, size.height);
                PaintPage(g, area, mouse);
            }
        } finally {
            g.dispose();
        }
    }

    private void PaintPage(Graphics2D g, Rectangle area, Point mouse){
        Graphics2D inPage = (Graphics2D) g.create();
        try {
            inPage.clipRect(area.x, area.y, area.width, area.height);
            inPage.translate(area.x, area.y - scroll);
            hits.SetFrame(area.x, area.y - scroll, area);
            contentHeight = PaintContent(inPage, area.width, new Point(mouse.x - area.x, mouse.y - area.y + scroll));
        } finally {
            hits.ClearFrame();
            inPage.dispose();
        }
        scroll = ClampScroll(scroll);
    }

    private int PaintContent(Graphics2D g, int width, Point mouse){
        ShellLayout shell = layout.GetShell();
        int y = 0;
        List<NavEntry> back = new ArrayList<>();
        back.add(new NavEntry(backKey, backWords, null, false, true, onBack));
        y += strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, null, back, mouse, hits) + shell.GetGap();
        y += PaintTitle(g, y, width) + shell.GetGap();
        y += PaintSummary(g, y, width) + shell.GetGap();
        y += PaintControls(g, y, width, mouse) + shell.GetGap();
        y += PaintRows(g, y, width);
        return y + layout.GetList().GetBottomPadding();
    }

    private int PaintTitle(Graphics2D g, int top, int width){
        TextStyle small = Theme.Current.GetHeading(), title = Theme.Current.GetTitle();
        FontMetrics smallMetrics = g.getFontMetrics(small.GetFont()), titleMetrics = g.getFontMetrics(title.GetFont());
        g.setFont(small.GetFont());
        g.setColor(small.GetColour());
        g.drawString(caption, 0, top + smallMetrics.getAscent());
        g.setFont(title.GetFont());
        g.setColor(title.GetColour());
        g.drawString(TextFit.Fit(titleMetrics, data.GetTitle(), width), 0, top + smallMetrics.getHeight() + titleMetrics.getAscent());
        return smallMetrics.getHeight() + titleMetrics.getHeight();
    }

    // The summary card: the big "49 / 52", then the percent and counts, the bar, and last unlocked and rarest under it
    private int PaintSummary(Graphics2D g, int top, int width){
        OverviewLayout overview = layout.GetOverview();
        int pad = overview.GetCardPadding();
        NavTheme nav = Theme.Current.GetNav();
        TextStyle big = Theme.Current.GetTitle(), small = Theme.Current.GetHeading();
        Font fine = Theme.Current.GetField().GetFont();
        FontMetrics bigMetrics = g.getFontMetrics(big.GetFont()), smallMetrics = g.getFontMetrics(small.GetFont()), fineMetrics = g.getFontMetrics(fine);
        int height = pad + bigMetrics.getHeight() + smallMetrics.getHeight() + fineMetrics.getHeight() + fineMetrics.getHeight() + pad;
        Rectangle box = new Rectangle(0, top, width, height);
        CardBox.Paint(g, box, false);
        Graphics2D inside = (Graphics2D) g.create();
        try {
            inside.clipRect(box.x, box.y, box.width, box.height);
            int x = box.x + pad, y = box.y + pad;
            String figure = String.valueOf(data.GetUnlocked());
            inside.setFont(big.GetFont());
            inside.setColor(big.GetColour());
            inside.drawString(figure, x, y + bigMetrics.getAscent());
            inside.setFont(fine);
            inside.setColor(small.GetColour());
            inside.drawString("/ " + data.GetTotal() + "  " + unlockedCaption, x + bigMetrics.stringWidth(figure) + layout.GetShell().GetItemGap(), y + bigMetrics.getAscent());
            y += bigMetrics.getHeight();
            inside.setFont(small.GetFont());
            inside.setColor(small.GetColour());
            inside.drawString(data.GetPercentText(), x, y + smallMetrics.getAscent());
            String locked = data.GetLockedText();
            inside.drawString(locked, box.x + box.width - pad - smallMetrics.stringWidth(locked), y + smallMetrics.getAscent());
            y += smallMetrics.getHeight();
            int barWidth = box.width - pad - pad, barHeight = LayoutMath.Half(fineMetrics.getHeight());
            inside.setColor(nav.GetCountBackground());
            inside.fillRect(x, y, barWidth, barHeight);
            inside.setColor(nav.GetSelected());
            inside.fillRect(x, y, (int) Math.round(barWidth * data.GetFraction()), barHeight);
            y += fineMetrics.getHeight();
            inside.setFont(fine);
            inside.setColor(small.GetColour());
            String last = lastUnlockedWords + " " + data.GetLastUnlockedText() + "    " + rarestWords + " " + data.GetRarestText();
            inside.drawString(TextFit.Fit(fineMetrics, last, barWidth), x, y + fineMetrics.getAscent());
        } finally {
            inside.dispose();
        }
        return height;
    }

    // All / Unlocked / Locked with counts, and the order button at the right
    private int PaintControls(Graphics2D g, int top, int width, Point mouse){
        ShellLayout shell = layout.GetShell();
        List<NavEntry> filters = new ArrayList<>();
        for(AchievementFilter option : AchievementFilter.values()){
            filters.add(new NavEntry(FilterKey(option), option.GetLabel(), data.Count(option), option == filter, true, () -> filter = option));
        }
        int used = strip.Paint(g, 0, top, shell.GetStateRowHeight(), width, null, filters, mouse, hits);
        List<NavEntry> order = new ArrayList<>();
        order.add(new NavEntry(sortKey, "Sort: " + sort.GetLabel(), null, false, true, () -> sort = sort.Next()));
        int orderWidth = strip.Width(g, null, order);
        int filtersWidth = strip.Width(g, null, filters);
        if(filtersWidth + shell.GetItemGap() + orderWidth <= width){
            strip.Paint(g, width - orderWidth, top, shell.GetStateRowHeight(), width, null, order, mouse, hits);
        } else {
            used += shell.GetItemGap() + strip.Paint(g, 0, top + used + shell.GetItemGap(), shell.GetStateRowHeight(), width, null, order, mouse, hits);
        }
        return used;
    }

    // Each achievement: its words at the left, how rare it is, and when it was unlocked (or a locked mark)
    private int PaintRows(Graphics2D g, int top, int width){
        ListLayout list = layout.GetList();
        OverviewLayout overview = layout.GetOverview();
        int pad = overview.GetCardPadding();
        List<AchievementItem> shown = data.Visible(filter, sort);
        Font titleFont = Theme.Current.GetCell().GetFont(), fine = Theme.Current.GetField().GetFont();
        FontMetrics titleMetrics = g.getFontMetrics(titleFont), fineMetrics = g.getFontMetrics(fine);
        if(shown.isEmpty()){
            Rectangle box = new Rectangle(0, top, width, list.GetRowHeight());
            CardBox.Paint(g, box, false);
            g.setFont(fine);
            g.setColor(Theme.Current.GetNav().GetDisabledText());
            g.drawString(emptyWords, box.x + pad, TextLine.Baseline(box.y, box.height, fineMetrics));
            return box.height;
        }
        int y = top;
        for(AchievementItem item : shown){
            Rectangle box = new Rectangle(0, y, width, list.GetRowHeight());
            CardBox.Paint(g, box, false);
            boolean quiet = !item.IsUnlocked();
            java.awt.Color text = quiet ? Theme.Current.GetNav().GetDisabledText() : Theme.Current.GetCell().GetText();
            Graphics2D inside = (Graphics2D) g.create();
            try {
                inside.clipRect(box.x, box.y, box.width, box.height);
                String rarity = item.GetRarity() == null ? "" : AchievementViewBuilder.Percent(item.GetRarity());
                String rarityWords = AchievementViewBuilder.RarityWords(item);
                String when = item.IsUnlocked() ? item.GetUnlockedText() : lockedMark;
                String whenWords = item.IsUnlocked() ? unlockedWords : "";
                int dateWidth = Math.max(titleMetrics.stringWidth(when), fineMetrics.stringWidth(whenWords));
                int rarityWidth = Math.max(titleMetrics.stringWidth(rarity), fineMetrics.stringWidth(rarityWords));
                int dateX = box.x + box.width - pad - dateWidth;
                int rarityX = dateX - pad - rarityWidth;
                int textWidth = Math.max(0, rarityX - pad - pad);
                int topLine = box.y + LayoutMath.Centre(box.height, titleMetrics.getHeight() + fineMetrics.getHeight());
                int secondLine = topLine + titleMetrics.getHeight() + fineMetrics.getAscent();
                inside.setFont(titleFont);
                inside.setColor(text);
                inside.drawString(TextFit.Fit(titleMetrics, item.GetTitle(), textWidth), box.x + pad, topLine + titleMetrics.getAscent());
                inside.setColor(item.GetRarity() != null && item.GetRarity() < AchievementViewBuilder.rareBelowPercent ? Theme.Current.GetNav().GetSelected() : text);
                inside.drawString(rarity, rarityX, topLine + titleMetrics.getAscent());
                inside.setColor(text);
                inside.drawString(when, dateX, topLine + titleMetrics.getAscent());
                inside.setFont(fine);
                inside.setColor(Theme.Current.GetHeading().GetColour());
                inside.drawString(TextFit.Fit(fineMetrics, item.GetDescription(), textWidth), box.x + pad, secondLine);
                inside.drawString(rarityWords, rarityX, secondLine);
                inside.drawString(whenWords, dateX, secondLine);
            } finally {
                inside.dispose();
            }
            y += list.GetRowHeight() + list.GetRowGap();
        }
        return y - list.GetRowGap() - top;
    }

    public void Scroll(int wheelRotation){
        scroll = ClampScroll(scroll + wheelRotation * layout.GetList().GetScrollStep());
    }

    private int ClampScroll(int value){
        return Math.max(0, Math.min(value, Math.max(0, contentHeight - viewportHeight)));
    }

    public void Press(Point point){
        pressedKey = hits.KeyAt(point);
    }

    // True if that completed a click, in which case the screen has changed and needs painting again
    public boolean Release(Point point){
        String key = hits.KeyAt(point);
        boolean clicked = key != null && key.equals(pressedKey);
        pressedKey = null;
        return clicked && hits.Run(key);
    }

    public Rectangle BoundsOf(String key){
        return hits.BoundsOf(key);
    }
}
