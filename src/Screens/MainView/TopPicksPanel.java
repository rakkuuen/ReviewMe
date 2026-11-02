package Screens.MainView;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import Controller.BackEnd.Overview.TopPickItem;
import Controller.BackEnd.Overview.TopPickItems;
import Model.Layout.LayoutMath;
import Model.Layout.ListLayout;
import Model.Layout.OverviewLayout;
import Model.Themes.CellTheme;
import Model.Themes.Theme;

// The Top picks panel: a heading with "4 of 5 slots", then one row for each slot with its number, the game and its rating, or a muted
// "Pin a game" where a slot is empty. Returns how tall it came out
public class TopPicksPanel {
    private static final String caption = "Top picks";
    private static final String emptyText = "Pin a game";
    private static final String slotWord = "Slot ";

    private final OverviewLayout layout;
    private final ListLayout list;

    public TopPicksPanel(OverviewLayout layout, ListLayout list){
        this.layout = layout;
        this.list = list;
    }

    public int Paint(Graphics2D g, Rectangle area, List<TopPickItem> items){
        Font headingFont = Theme.Current.GetHeading().GetFont();
        Font smallFont = Theme.Current.GetField().GetFont();
        FontMetrics headingMetrics = g.getFontMetrics(headingFont), smallMetrics = g.getFontMetrics(smallFont);
        int headerHeight = layout.GetSectionHeaderHeight();

        g.setFont(headingFont);
        g.setColor(Theme.Current.GetHeading().GetColour());
        g.drawString(caption, area.x, TextLine.Baseline(area.y, headerHeight, headingMetrics));
        String summary = TopPickItems.Summary(items);
        g.setFont(smallFont);
        g.drawString(summary, area.x + area.width - smallMetrics.stringWidth(summary), TextLine.Baseline(area.y, headerHeight, smallMetrics));

        int y = area.y + headerHeight;
        for(TopPickItem item : items){
            PaintRow(g, new Rectangle(area.x, y, area.width, layout.GetTopPickRowHeight()), item);
            y += layout.GetTopPickRowHeight() + list.GetRowGap();
        }
        return Math.max(headerHeight, y - list.GetRowGap() - area.y);
    }

    private void PaintRow(Graphics2D g, Rectangle row, TopPickItem item){
        CardBox.Paint(g, row, false);
        CellTheme cell = Theme.Current.GetCell();
        Font numberFont = Theme.Current.GetHeading().GetFont();
        FontMetrics numberMetrics = g.getFontMetrics(numberFont);
        int pad = layout.GetCardPadding();

        Graphics2D inside = (Graphics2D) g.create();
        try {
            inside.clipRect(row.x, row.y, row.width, row.height);
            if(item.IsEmpty()){
                Font smallFont = Theme.Current.GetField().GetFont();
                FontMetrics smallMetrics = inside.getFontMetrics(smallFont);
                inside.setFont(smallFont);
                inside.setColor(Theme.Current.GetNav().GetDisabledText());
                inside.drawString(slotWord + item.GetSlot() + "  " + emptyText, row.x + pad, TextLine.Baseline(row.y, row.height, smallMetrics));
                return;
            }

            String number = String.valueOf(item.GetSlot());
            inside.setFont(numberFont);
            inside.setColor(Theme.Current.GetHeading().GetColour());
            inside.drawString(number, row.x + pad, TextLine.Baseline(row.y, row.height, numberMetrics));

            FontMetrics cellMetrics = inside.getFontMetrics(cell.GetFont());
            inside.setFont(cell.GetFont());
            inside.setColor(cell.GetText());
            int titleX = row.x + pad + numberMetrics.stringWidth(number) + pad;
            // A rating is shown as typed, which for a joke rating can be very long, so it gets at most a quarter of the row
            String rating = item.GetRating() == null ? "" : TextFit.Fit(cellMetrics, item.GetRating(), LayoutMath.Half(LayoutMath.Half(row.width)));
            int ratingWidth = cellMetrics.stringWidth(rating);
            String title = TextFit.Fit(cellMetrics, item.GetTitle(), row.x + row.width - pad - ratingWidth - pad - titleX);
            int baseline = TextLine.Baseline(row.y, row.height, cellMetrics);
            inside.drawString(title, titleX, baseline);
            inside.drawString(rating, row.x + row.width - pad - ratingWidth, baseline);
        } finally {
            inside.dispose();
        }
    }
}
