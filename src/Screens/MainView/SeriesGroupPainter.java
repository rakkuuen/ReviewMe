package Screens.MainView;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import Controller.BackEnd.Overview.GameRow;
import Controller.BackEnd.Overview.OverviewRows;
import Controller.BackEnd.Overview.OverviewSeriesGroup;
import Controller.BackEnd.Overview.SeriesHeading;
import Model.Layout.Layout;
import Model.Layout.LayoutMath;
import Model.Layout.ListLayout;
import Model.Layout.ShellLayout;
import Model.Library.LibraryGame;
import Model.Themes.Theme;

// One series as a foldable group: a heading line (a + or - for whether it is folded, the name, its summary, and optionally an
// "Open series" button) with its games under it, indented. Clicking the heading folds it; the screen keeps what is folded.
// Used by the Overview and the Series tab, which differ only in the button
public class SeriesGroupPainter {
    public static final String collapsedMark = "+";
    public static final String openMark = "-";

    public static String HeadingKey(String seriesId){ return "series:" + seriesId; }
    public static String RowKey(String seriesId, int index){ return "row:series:" + seriesId + ":" + index; }
    public static String OpenSeriesKey(String seriesId){ return "openseries:" + seriesId; }

    private final Layout layout;
    private final GameRowPainter rowPainter;
    private final NavStrip strip;

    public SeriesGroupPainter(Layout layout, GameRowPainter rowPainter){
        this.layout = layout;
        this.rowPainter = rowPainter;
        this.strip = new NavStrip(layout.GetShell());
    }

    // Draws the group with its top left corner at (x, y), width wide. toggle folds or opens it, openGame opens a game with a review,
    // and openSeries (null for no button) is what the heading's "Open series" button does. Returns the height drawn
    public int Paint(Graphics2D g, int x, int y, int width, OverviewSeriesGroup group, boolean collapsed, Point mouse, HitMap hits,
            Runnable toggle, Consumer<LibraryGame> openGame, String openLabel, Runnable openSeries){
        ListLayout list = layout.GetList();
        int top = y;
        PaintHeading(g, new Rectangle(x, y, width, list.GetRowHeight()), group, collapsed, mouse, hits, toggle, openLabel, openSeries);
        y += list.GetRowHeight() + list.GetRowGap();
        if(!collapsed){
            int indent = layout.GetOverview().GetSeriesIndent();
            int index = 0;
            for(LibraryGame game : group.GetGames()){
                GameRow row = OverviewRows.BuildForSeries(game);
                Rectangle box = new Rectangle(x + indent, y, width - indent, list.GetRowHeight());
                rowPainter.Paint(g, box, row, box.contains(mouse));
                if(row.CanOpen()){
                    hits.Add(RowKey(group.GetSeries().GetId(), index), box, () -> openGame.accept(row.GetGame()));
                }
                y += list.GetRowHeight() + list.GetRowGap();
                index++;
            }
        }
        return y - list.GetRowGap() - top;
    }

    private void PaintHeading(Graphics2D g, Rectangle box, OverviewSeriesGroup group, boolean collapsed, Point mouse, HitMap hits, Runnable toggle,
            String openLabel, Runnable openSeries){
        ShellLayout shell = layout.GetShell();
        CardBox.Paint(g, box, box.contains(mouse));
        Font headingFont = Theme.Current.GetHeading().GetFont(), smallFont = Theme.Current.GetField().GetFont();
        FontMetrics headingMetrics = g.getFontMetrics(headingFont), smallMetrics = g.getFontMetrics(smallFont);
        int pad = layout.GetOverview().GetCardPadding();

        int right = box.x + box.width - pad;
        String id = group.GetSeries().GetId();
        if(openSeries != null){
            List<NavEntry> button = new ArrayList<>();
            button.add(new NavEntry(OpenSeriesKey(id), openLabel, null, false, true, openSeries));
            int buttonWidth = strip.Width(g, null, button);
            strip.Paint(g, right - buttonWidth, box.y + LayoutMath.Centre(box.height, shell.GetStateRowHeight()), shell.GetStateRowHeight(), buttonWidth, null, button, mouse, hits);
            right -= buttonWidth + shell.GetItemGap();
        }

        Graphics2D inside = (Graphics2D) g.create();
        try {
            inside.clipRect(box.x, box.y, box.width, box.height);
            inside.setFont(headingFont);
            inside.setColor(Theme.Current.GetHeading().GetColour());
            int markX = box.x + pad;
            int nameX = markX + headingMetrics.stringWidth(collapsedMark) + pad;
            inside.drawString(collapsed ? collapsedMark : openMark, markX, TextLine.Baseline(box.y, box.height, headingMetrics));
            inside.drawString(SeriesHeading.Name(group), nameX, TextLine.Baseline(box.y, box.height, headingMetrics));
            String summary = SeriesHeading.Summary(group);
            inside.setFont(smallFont);
            inside.drawString(summary, right - smallMetrics.stringWidth(summary), TextLine.Baseline(box.y, box.height, smallMetrics));
        } finally {
            inside.dispose();
        }
        hits.Add(HeadingKey(id), box, toggle);
    }
}
