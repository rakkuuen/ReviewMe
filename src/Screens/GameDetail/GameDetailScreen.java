package Screens.GameDetail;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import Controller.BackEnd.GameDetail.DetailBadge;
import Controller.BackEnd.GameDetail.DetailSection;
import Controller.BackEnd.GameDetail.DetailStat;
import Controller.BackEnd.GameDetail.GameDetailData;
import Model.Layout.Layout;
import Model.Layout.LayoutMath;
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
import Screens.RichText.RichBox;
import Screens.RichText.RichTextKit;

// A game's page, read only: Back, Edit and Export across the top, the title with its series link and badges, the stats strip, tags, and
// every section of the review (missing ones marked, subsections nested). Draws what GameDetailBuilder worked out and hands every click
// back through the callbacks; the whole page scrolls
public class GameDetailScreen {
    public static final String backKey = "detail:back";
    public static final String editKey = "detail:edit";
    public static final String exportKey = "detail:export";
    public static final String seriesKey = "detail:series";
    public static final String achievementsKey = "detail:achievements";
    public static final String backWords = "Back";
    public static final String editWords = "Edit";
    public static final String exportWords = "Export";
    public static final String viewAllWords = "View all";
    public static final String missingWord = "MISSING";
    public static final String missingText = "Nothing written under this heading yet.";
    public static final String emptyText = "Nothing written here.";
    public static final String notApplicableText = "Not applicable";
    public static final String emptyPartText = "(empty)";
    public static final String openMark = "›";

    private GameDetailData data;
    private final Layout layout;
    private final Runnable onBack, onEdit, onViewAchievements;
    private final Consumer<String> onOpenSeries;
    private final HitMap hits = new HitMap();
    private final NavStrip strip;
    private final DetailSectionPainter sections;
    private String pressedKey;
    private String toast;   // A short message shown over the bottom of the window, or null
    private int scroll;
    private int contentHeight;
    private int viewportHeight;

    public GameDetailScreen(GameDetailData data, Layout layout, Runnable onBack, Runnable onEdit, Consumer<String> onOpenSeries, Runnable onViewAchievements, RichTextKit kit, Consumer<String> onOpenLink){
        this.data = data;
        this.layout = layout;
        this.onBack = onBack;
        this.onEdit = onEdit;
        this.onOpenSeries = onOpenSeries;
        this.onViewAchievements = onViewAchievements;
        this.strip = new NavStrip(layout.GetShell());
        this.sections = new DetailSectionPainter(layout, new RichBox(kit, layout.GetRich(), hits, onOpenLink));
    }

    // Shows the game as it is now, staying where it is scrolled to
    public void Reload(GameDetailData fresh){
        this.data = fresh;
    }

    public String GetTitle(){ return data.GetTitle(); }

    // Shows a short message ("Saved") over the bottom of the window until ClearToast
    public void ShowToast(String message){ toast = message; }
    public void ClearToast(){ toast = null; }
    public String GetToast(){ return toast; }

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
            PaintToast(g, size);
        } finally {
            g.dispose();
        }
    }

    // The toast sits in the middle of the bottom of the window, over the page, in the accent colour
    private void PaintToast(Graphics2D g, Dimension size){
        if(toast == null){
            return;
        }
        NavTheme nav = Theme.Current.GetNav();
        Font font = nav.GetFont();
        FontMetrics metrics = g.getFontMetrics(font);
        ShellLayout shell = layout.GetShell();
        int width = metrics.stringWidth(toast) + shell.GetItemPaddingX() + shell.GetItemPaddingX();
        int height = metrics.getHeight() + shell.GetItemGap() + shell.GetItemGap();
        Rectangle box = new Rectangle(LayoutMath.Centre(size.width, width), size.height - shell.GetPadding() - height, width, height);
        g.setClip(null);
        g.setColor(nav.GetSelected());
        g.fillRoundRect(box.x, box.y, box.width - 1, box.height - 1, nav.GetCornerArc(), nav.GetCornerArc());
        g.setFont(font);
        g.setColor(nav.GetSelectedText());
        g.drawString(toast, box.x + shell.GetItemPaddingX(), TextLine.Baseline(box.y, box.height, metrics));
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
        OverviewLayout overview = layout.GetOverview();
        int y = 0;
        y += PaintTopBar(g, y, width, mouse) + shell.GetGap();
        y += PaintTitle(g, y, width, mouse) + shell.GetGap();
        y += PaintBadges(g, y, width) + shell.GetGap();
        y += PaintStats(g, y, width, mouse) + shell.GetGap();
        y += PaintTags(g, y, width);
        for(DetailSection section : data.GetSections()){
            y += overview.GetSectionGap();
            y += sections.Paint(g, y, width, section, mouse);
        }
        return y + layout.GetList().GetBottomPadding();
    }

    // Back at the left; Edit and Export at the right (Export isn't built yet, so it is greyed)
    private int PaintTopBar(Graphics2D g, int y, int width, Point mouse){
        ShellLayout shell = layout.GetShell();
        List<NavEntry> back = new ArrayList<>();
        back.add(new NavEntry(backKey, backWords, null, false, true, onBack));
        strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, null, back, mouse, hits);
        List<NavEntry> actions = new ArrayList<>();
        actions.add(new NavEntry(editKey, editWords, null, false, true, onEdit));
        actions.add(new NavEntry(exportKey, exportWords, null, false, false, null));
        strip.Paint(g, Math.max(strip.Width(g, null, back) + shell.GetItemGap(), width - strip.Width(g, null, actions)), y, shell.GetStateRowHeight(), width, null, actions, mouse, hits);
        return shell.GetStateRowHeight();
    }

    // The title, centred, and under it the series link (click to open the series) and the locked note
    private int PaintTitle(Graphics2D g, int top, int width, Point mouse){
        TextStyle caption = Theme.Current.GetHeading(), title = Theme.Current.GetTitle();
        FontMetrics captionMetrics = g.getFontMetrics(caption.GetFont()), titleMetrics = g.getFontMetrics(title.GetFont());
        int y = top;
        String text = TextFit.Fit(titleMetrics, data.GetTitle(), width);
        g.setFont(title.GetFont());
        g.setColor(title.GetColour());
        g.drawString(text, LayoutMath.Centre(width, titleMetrics.stringWidth(text)), y + titleMetrics.getAscent());
        y += titleMetrics.getHeight();
        if(data.GetSeriesLink() != null){
            String link = TextFit.Fit(captionMetrics, data.GetSeriesLink() + " " + openMark, width);
            int x = LayoutMath.Centre(width, captionMetrics.stringWidth(link));
            g.setFont(caption.GetFont());
            g.setColor(Theme.Current.GetNav().GetSelected());
            g.drawString(link, x, y + captionMetrics.getAscent());
            Rectangle box = new Rectangle(x, y, captionMetrics.stringWidth(link), captionMetrics.getHeight());
            hits.Add(seriesKey, box, () -> onOpenSeries.accept(data.GetSeriesId()));
            y += captionMetrics.getHeight();
        }
        String locked = GameDetailData.lockedNote;
        g.setFont(caption.GetFont());
        g.setColor(Theme.Current.GetNav().GetDisabledText());
        g.drawString(locked, LayoutMath.Centre(width, captionMetrics.stringWidth(locked)), y + captionMetrics.getAscent());
        return y + captionMetrics.getHeight() - top;
    }

    // The badges in a centred row, wrapping onto more lines when they don't fit
    private int PaintBadges(Graphics2D g, int top, int width){
        NavTheme nav = Theme.Current.GetNav();
        Font font = Theme.Current.GetField().GetFont();
        FontMetrics metrics = g.getFontMetrics(font);
        int padX = layout.GetShell().GetItemPaddingX(), gap = layout.GetShell().GetItemGap();
        int height = metrics.getHeight() + gap;
        int y = top;
        List<DetailBadge> badges = data.GetBadges();
        int i = 0;
        while(i < badges.size()){
            int lineWidth = 0, count = 0;
            while(i + count < badges.size()){
                int next = metrics.stringWidth(badges.get(i + count).GetText()) + padX + padX;
                if(count > 0 && lineWidth + gap + next > width){
                    break;
                }
                lineWidth += (count > 0 ? gap : 0) + next;
                count++;
            }
            int x = LayoutMath.Centre(width, lineWidth);
            for(int k = 0; k < count; k++){
                DetailBadge badge = badges.get(i + k);
                int boxWidth = metrics.stringWidth(badge.GetText()) + padX + padX;
                Rectangle box = new Rectangle(x, y, boxWidth, height);
                PaintBadge(g, box, badge, nav, font, metrics);
                x += boxWidth + gap;
            }
            i += count;
            y += height + gap;
        }
        return Math.max(0, y - gap - top);
    }

    private void PaintBadge(Graphics2D g, Rectangle box, DetailBadge badge, NavTheme nav, Font font, FontMetrics metrics){
        boolean accent = badge.GetLook() == DetailBadge.Look.WARNING || badge.GetLook() == DetailBadge.Look.MORE_ACHIEVEMENTS;
        g.setColor(accent ? nav.GetSelected() : nav.GetCountBackground());
        g.fillRoundRect(box.x, box.y, box.width - 1, box.height - 1, nav.GetCornerArc(), nav.GetCornerArc());
        g.setFont(font);
        g.setColor(accent ? nav.GetSelectedText() : nav.GetCountText());
        g.drawString(badge.GetText(), box.x + LayoutMath.Centre(box.width, metrics.stringWidth(badge.GetText())), TextLine.Baseline(box.y, box.height, metrics));
    }

    // The five figures as cards in as many columns as fit
    private int PaintStats(Graphics2D g, int top, int width, Point mouse){
        OverviewLayout overview = layout.GetOverview();
        List<DetailStat> stats = data.GetStats();
        int columns = Math.max(1, Math.min(stats.size(), (width + overview.GetColumnGap()) / (overview.GetStatCardMinWidth() + overview.GetStatCardMinWidth() + overview.GetColumnGap())));
        int cardWidth = (width - overview.GetColumnGap() * (columns - 1)) / columns;
        int y = top;
        for(int i = 0; i < stats.size(); i++){
            int column = i % columns;
            if(column == 0 && i > 0){
                y += overview.GetStatCardHeight() + overview.GetColumnGap();
            }
            PaintStat(g, new Rectangle(column * (cardWidth + overview.GetColumnGap()), y, cardWidth, overview.GetStatCardHeight()), stats.get(i));
        }
        return y + overview.GetStatCardHeight() - top;
    }

    private void PaintStat(Graphics2D g, Rectangle box, DetailStat stat){
        CardBox.Paint(g, box, false);
        int pad = layout.GetOverview().GetCardPadding();
        TextStyle label = Theme.Current.GetHeading(), value = Theme.Current.GetTitle();
        Font small = Theme.Current.GetField().GetFont();
        FontMetrics labelMetrics = g.getFontMetrics(label.GetFont()), valueMetrics = g.getFontMetrics(value.GetFont()), smallMetrics = g.getFontMetrics(small);
        Graphics2D inside = (Graphics2D) g.create();
        try {
            inside.clipRect(box.x, box.y, box.width, box.height);
            int x = box.x + pad, y = box.y + pad;
            inside.setFont(label.GetFont());
            inside.setColor(label.GetColour());
            inside.drawString(stat.GetLabel(), x, y + labelMetrics.getAscent());
            if(stat.HasViewAll()){
                int viewWidth = labelMetrics.stringWidth(viewAllWords);
                inside.setColor(Theme.Current.GetNav().GetSelected());
                inside.drawString(viewAllWords, box.x + box.width - pad - viewWidth, y + labelMetrics.getAscent());
                hits.Add(achievementsKey, new Rectangle(box.x + box.width - pad - viewWidth, y, viewWidth, labelMetrics.getHeight()), onViewAchievements);
            }
            y += labelMetrics.getHeight();
            inside.setFont(value.GetFont());
            inside.setColor(value.GetColour());
            inside.drawString(stat.GetValue(), x, y + valueMetrics.getAscent());
            if(stat.GetSuffix() != null){
                inside.setFont(small);
                inside.setColor(label.GetColour());
                inside.drawString(stat.GetSuffix(), x + valueMetrics.stringWidth(stat.GetValue()) + layout.GetShell().GetItemGap(), y + valueMetrics.getAscent());
            }
            y += valueMetrics.getHeight();
            if(stat.GetFraction() != null){
                int barWidth = box.width - pad - pad;
                int barHeight = LayoutMath.Half(smallMetrics.getHeight());
                inside.setColor(Theme.Current.GetNav().GetCountBackground());
                inside.fillRect(x, y, barWidth, barHeight);
                inside.setColor(Theme.Current.GetNav().GetSelected());
                inside.fillRect(x, y, (int) Math.round(barWidth * stat.GetFraction()), barHeight);
            } else if(stat.GetNote() != null){
                inside.setFont(small);
                inside.setColor(Theme.Current.GetNav().GetDisabledText());
                inside.drawString(TextFit.Fit(smallMetrics, stat.GetNote(), box.width - pad - pad), x, y + smallMetrics.getAscent());
            }
        } finally {
            inside.dispose();
        }
    }

    // Genre and platform tags as chips in a row that wraps; the odd metadata tags have a dashed outline
    private int PaintTags(Graphics2D g, int top, int width){
        List<String> all = new ArrayList<>(data.GetTags());
        int plainCount = all.size();
        all.addAll(data.GetMetadataTags());
        if(all.isEmpty()){
            return 0;
        }
        NavTheme nav = Theme.Current.GetNav();
        Font font = Theme.Current.GetField().GetFont();
        FontMetrics metrics = g.getFontMetrics(font);
        int padX = layout.GetShell().GetItemPaddingX(), gap = layout.GetShell().GetItemGap();
        int height = metrics.getHeight() + gap;
        int x = 0, y = top;
        Stroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{gap, gap}, 0f);
        for(int i = 0; i < all.size(); i++){
            int boxWidth = metrics.stringWidth(all.get(i)) + padX + padX;
            if(x > 0 && x + boxWidth > width){
                x = 0;
                y += height + gap;
            }
            Rectangle box = new Rectangle(x, y, boxWidth, height);
            g.setColor(nav.GetBorder());
            if(i < plainCount){
                g.drawRoundRect(box.x, box.y, box.width - 1, box.height - 1, nav.GetCornerArc(), nav.GetCornerArc());
            } else {
                Stroke before = g.getStroke();
                g.setStroke(dashed);
                g.drawRoundRect(box.x, box.y, box.width - 1, box.height - 1, nav.GetCornerArc(), nav.GetCornerArc());
                g.setStroke(before);
            }
            g.setFont(font);
            g.setColor(Theme.Current.GetCell().GetText());
            g.drawString(all.get(i), box.x + padX, TextLine.Baseline(box.y, box.height, metrics));
            x += boxWidth + gap;
        }
        return y + height - top;
    }

    // Moves the page up or down by that many wheel notches (positive is down)
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

    // Is that point on a link in the review (so the pointer should be a hand)?
    public boolean IsOverLink(Point point){
        return RichBox.IsLinkKey(hits.KeyAt(point));
    }

    // Where a button was last drawn, or null
    public Rectangle BoundsOf(String key){
        return hits.BoundsOf(key);
    }
}
