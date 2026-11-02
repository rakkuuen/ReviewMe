package Screens.SeriesView;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import Controller.BackEnd.Overview.StatCard;
import Controller.BackEnd.SeriesView.SeriesGameItem;
import Controller.BackEnd.SeriesView.SeriesViewData;
import Model.Layout.Layout;
import Model.Layout.LayoutMath;
import Model.Layout.ListLayout;
import Model.Layout.OverviewLayout;
import Model.Layout.ShellLayout;
import Model.Library.LibraryGame;
import Model.Themes.TextStyle;
import Model.Themes.Theme;
import Screens.MainView.CardBox;
import Screens.MainView.GameRowPainter;
import Screens.MainView.HitMap;
import Screens.MainView.NavEntry;
import Screens.MainView.NavStrip;
import Screens.MainView.StatCardPainter;
import Screens.MainView.TextFit;
import Screens.MainView.TextLine;

// One series' page: Back and the series buttons across the top, its name and template, the four stat cards, its notes and its games
// in order with an assign button. Draws what the backend worked out (SeriesViewData) and hands every click back through the
// callbacks; the whole page scrolls
public class SeriesViewScreen {
    public static final String backKey = "seriesview:back";
    public static final String newKey = "seriesview:new";
    public static final String editKey = "seriesview:edit";
    public static final String assignKey = "seriesview:assign";
    public static final String backWords = "Back";
    public static final String newWords = "+ New series";
    public static final String editWords = "Edit series";
    public static final String assignWords = "+ assign game";
    public static final String seriesCaption = "Series";
    public static final String notesHeading = "Notes";
    public static final String gamesHeading = "Games in this series";
    public static final String noGamesTitle = "No games in this series yet";
    public static final String noGamesHint = "Assign games from your library. They switch to this series' template.";

    public static String RowKey(int index){ return "seriesview:row:" + index; }

    private SeriesViewData data;
    private final Layout layout;
    private final Runnable onBack, onNewSeries;
    private final Consumer<String> onEdit, onAssign;
    private final Consumer<LibraryGame> onOpenGame;
    private final HitMap hits = new HitMap();
    private final NavStrip strip;
    private final StatCardPainter cardPainter;
    private final GameRowPainter rowPainter;
    private String pressedKey;
    private int scroll;
    private int contentHeight;
    private int viewportHeight;

    public SeriesViewScreen(SeriesViewData data, Layout layout, Runnable onBack, Runnable onNewSeries, Consumer<String> onEdit, Consumer<String> onAssign, Consumer<LibraryGame> onOpenGame){
        this.data = data;
        this.layout = layout;
        this.onBack = onBack;
        this.onNewSeries = onNewSeries;
        this.onEdit = onEdit;
        this.onAssign = onAssign;
        this.onOpenGame = onOpenGame;
        this.strip = new NavStrip(layout.GetShell());
        this.cardPainter = new StatCardPainter(layout.GetOverview());
        this.rowPainter = new GameRowPainter(layout.GetOverview());
    }

    // Shows the series as it is now, staying where it is scrolled to
    public void Reload(SeriesViewData fresh){
        this.data = fresh;
    }

    public String GetSeriesId(){ return data.GetSeriesId(); }

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
        OverviewLayout overview = layout.GetOverview();
        int y = 0;

        y += PaintTopBar(g, y, width, mouse) + shell.GetGap();
        y += PaintTitle(g, y, width) + shell.GetGap();
        y += PaintCards(g, y, width) + overview.GetSectionGap();
        y += PaintNotes(g, y, width) + overview.GetSectionGap();
        y += PaintGames(g, y, width, mouse);
        return y + layout.GetList().GetBottomPadding();
    }

    // Back at the left; New series and Edit series at the right
    private int PaintTopBar(Graphics2D g, int y, int width, Point mouse){
        ShellLayout shell = layout.GetShell();
        List<NavEntry> back = new ArrayList<>();
        back.add(new NavEntry(backKey, backWords, null, false, true, onBack));
        strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, null, back, mouse, hits);
        List<NavEntry> actions = new ArrayList<>();
        actions.add(new NavEntry(newKey, newWords, null, false, true, onNewSeries));
        actions.add(new NavEntry(editKey, editWords, null, false, true, () -> onEdit.accept(data.GetSeriesId())));
        strip.Paint(g, Math.max(strip.Width(g, null, back) + shell.GetItemGap(), width - strip.Width(g, null, actions)), y, shell.GetStateRowHeight(), width, null, actions, mouse, hits);
        return shell.GetStateRowHeight();
    }

    private int PaintTitle(Graphics2D g, int top, int width){
        TextStyle caption = Theme.Current.GetHeading(), title = Theme.Current.GetTitle();
        FontMetrics captionMetrics = g.getFontMetrics(caption.GetFont()), titleMetrics = g.getFontMetrics(title.GetFont());
        int y = top;
        g.setFont(caption.GetFont());
        g.setColor(caption.GetColour());
        g.drawString(seriesCaption, 0, y + captionMetrics.getAscent());
        y += captionMetrics.getHeight();
        g.setFont(title.GetFont());
        g.setColor(title.GetColour());
        g.drawString(TextFit.Fit(titleMetrics, data.GetName(), width), 0, y + titleMetrics.getAscent());
        y += titleMetrics.getHeight();
        g.setFont(caption.GetFont());
        g.setColor(caption.GetColour());
        g.drawString(TextFit.Fit(captionMetrics, data.GetTemplateLine(), width), 0, y + captionMetrics.getAscent());
        y += captionMetrics.getHeight();
        if(data.GetTemplateWarning() != null){
            g.setColor(Theme.Current.GetNav().GetSelected());
            g.drawString(TextFit.Fit(captionMetrics, data.GetTemplateWarning(), width), 0, y + captionMetrics.getAscent());
            y += captionMetrics.getHeight();
        }
        return y - top;
    }

    // The cards in as many columns as fit, wrapping onto more lines. A card is never narrower than the layout's smallest, nor than its
    // own words need
    private int PaintCards(Graphics2D g, int top, int width){
        OverviewLayout overview = layout.GetOverview();
        List<StatCard> cards = data.GetCards();
        int cardMinWidth = overview.GetStatCardMinWidth();
        FontMetrics valueMetrics = g.getFontMetrics(Theme.Current.GetTitle().GetFont()), labelMetrics = g.getFontMetrics(Theme.Current.GetHeading().GetFont());
        for(StatCard card : cards){
            cardMinWidth = Math.max(cardMinWidth, Math.max(valueMetrics.stringWidth(card.GetValue()), labelMetrics.stringWidth(card.GetLabel())) + overview.GetCardPadding() + overview.GetCardPadding());
        }
        int columns = Math.max(1, Math.min(cards.size(), (width + overview.GetColumnGap()) / (cardMinWidth + overview.GetColumnGap())));
        int cardWidth = (width - overview.GetColumnGap() * (columns - 1)) / columns;
        int y = top;
        for(int i = 0; i < cards.size(); i++){
            int column = i % columns;
            if(column == 0 && i > 0){
                y += overview.GetStatCardHeight() + overview.GetColumnGap();
            }
            cardPainter.Paint(g, new Rectangle(column * (cardWidth + overview.GetColumnGap()), y, cardWidth, overview.GetStatCardHeight()), cards.get(i), Collections.emptyList());
        }
        return y + overview.GetStatCardHeight() - top;
    }

    // The notes in a box, wrapped to its width; the stand-in words (no notes yet) are drawn quieter
    private int PaintNotes(Graphics2D g, int top, int width){
        OverviewLayout overview = layout.GetOverview();
        int y = top + PaintHeading(g, top, notesHeading);
        FontMetrics metrics = g.getFontMetrics(Theme.Current.GetField().GetFont());
        int pad = overview.GetCardPadding();
        List<String> lines = TextWrap.Lines(metrics, data.GetNotes(), Math.max(1, width - pad - pad));
        Rectangle box = new Rectangle(0, y, width, pad + lines.size() * metrics.getHeight() + pad);
        CardBox.Paint(g, box, false);
        g.setFont(Theme.Current.GetField().GetFont());
        g.setColor(data.HasNotes() ? Theme.Current.GetCell().GetText() : Theme.Current.GetNav().GetDisabledText());
        for(int i = 0; i < lines.size(); i++){
            g.drawString(lines.get(i), box.x + pad, box.y + pad + i * metrics.getHeight() + metrics.getAscent());
        }
        return box.y + box.height - top;
    }

    // "Games in this series" with its assign button at the right, then each game numbered in order (or the words for none)
    private int PaintGames(Graphics2D g, int top, int width, Point mouse){
        ShellLayout shell = layout.GetShell();
        ListLayout list = layout.GetList();
        int pad = layout.GetOverview().GetCardPadding();
        int headerHeight = layout.GetOverview().GetSectionHeaderHeight();
        PaintHeading(g, top, gamesHeading);
        List<NavEntry> assign = new ArrayList<>();
        assign.add(new NavEntry(assignKey, assignWords, null, false, true, () -> onAssign.accept(data.GetSeriesId())));
        strip.Paint(g, width - strip.Width(g, null, assign), top + LayoutMath.Centre(headerHeight, shell.GetStateRowHeight()), shell.GetStateRowHeight(), width, null, assign, mouse, hits);
        int y = top + Math.max(headerHeight, shell.GetStateRowHeight()) + shell.GetGap();

        if(data.GetGames().isEmpty()){
            return y + PaintEmpty(g, y, width) - top;
        }
        FontMetrics numberMetrics = g.getFontMetrics(Theme.Current.GetCell().GetFont()), tagMetrics = g.getFontMetrics(Theme.Current.GetField().GetFont());
        int numberWidth = numberMetrics.stringWidth(String.valueOf(data.GetGames().size())) + pad;
        for(int i = 0; i < data.GetGames().size(); i++){
            SeriesGameItem item = data.GetGames().get(i);
            Rectangle number = new Rectangle(0, y, numberWidth, list.GetRowHeight());
            g.setFont(Theme.Current.GetCell().GetFont());
            g.setColor(Theme.Current.GetHeading().GetColour());
            g.drawString(item.GetPosition(), number.x, TextLine.Baseline(number.y, number.height, numberMetrics));

            Rectangle box = new Rectangle(numberWidth, y, width - numberWidth, list.GetRowHeight());
            String tag = item.GetTemplateTag() == null ? null : item.GetTemplateTag() + " template";
            int reserved = tag == null ? 0 : tagMetrics.stringWidth(tag) + pad;
            rowPainter.Paint(g, box, item.GetRow(), box.contains(mouse), reserved);
            if(tag != null){
                g.setFont(Theme.Current.GetField().GetFont());
                g.setColor(Theme.Current.GetNav().GetSelected());
                g.drawString(tag, box.x + box.width - pad - tagMetrics.stringWidth(tag), TextLine.Baseline(box.y, box.height, tagMetrics));
            }
            if(item.GetRow().CanOpen()){
                hits.Add(RowKey(i), box, () -> onOpenGame.accept(item.GetRow().GetGame()));
            }
            y += list.GetRowHeight() + list.GetRowGap();
        }
        return y - list.GetRowGap() - top;
    }

    private int PaintEmpty(Graphics2D g, int top, int width){
        ListLayout list = layout.GetList();
        int pad = layout.GetOverview().GetCardPadding();
        FontMetrics titleMetrics = g.getFontMetrics(Theme.Current.GetCell().GetFont()), hintMetrics = g.getFontMetrics(Theme.Current.GetField().GetFont());
        Rectangle box = new Rectangle(0, top, width, Math.max(list.GetRowHeight(), pad + titleMetrics.getHeight() + hintMetrics.getHeight() + pad));
        CardBox.Paint(g, box, false);
        g.setFont(Theme.Current.GetCell().GetFont());
        g.setColor(Theme.Current.GetCell().GetText());
        g.drawString(TextFit.Fit(titleMetrics, noGamesTitle, width - pad - pad), box.x + pad, box.y + pad + titleMetrics.getAscent());
        g.setFont(Theme.Current.GetField().GetFont());
        g.setColor(Theme.Current.GetNav().GetDisabledText());
        g.drawString(TextFit.Fit(hintMetrics, noGamesHint, width - pad - pad), box.x + pad, box.y + pad + titleMetrics.getHeight() + hintMetrics.getAscent());
        return box.height;
    }

    // A section heading; returns the height it took
    private int PaintHeading(Graphics2D g, int top, String text){
        TextStyle style = Theme.Current.GetHeading();
        FontMetrics metrics = g.getFontMetrics(style.GetFont());
        g.setFont(style.GetFont());
        g.setColor(style.GetColour());
        int height = Math.max(layout.GetOverview().GetSectionHeaderHeight(), metrics.getHeight());
        g.drawString(text, 0, TextLine.Baseline(top, height, metrics));
        return height;
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

    // Where a button was last drawn, or null
    public Rectangle BoundsOf(String key){
        return hits.BoundsOf(key);
    }
}
