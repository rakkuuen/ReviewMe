package Screens.MainView;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import Controller.BackEnd.Overview.GameRow;
import Model.Layout.LayoutMath;
import Model.Layout.OverviewLayout;
import Model.Themes.CellTheme;
import Model.Themes.Theme;

// Draws one game row: the title with a line of detail under it on the left, the rating on the right. Words too long for the row are
// cut short with "...", a rating gets at most a quarter of the row, and a row that can be opened lights up under the mouse. Room can be
// kept clear at the right for buttons
public class GameRowPainter {
    private final OverviewLayout layout;

    public GameRowPainter(OverviewLayout layout){
        this.layout = layout;
    }

    public void Paint(Graphics2D g, Rectangle box, GameRow row, boolean hovered){
        Paint(g, box, row, hovered, 0);
    }

    // reservedRight is room kept clear at the right edge for buttons drawn over the row
    public void Paint(Graphics2D g, Rectangle box, GameRow row, boolean hovered, int reservedRight){
        CardBox.Paint(g, box, hovered && row.CanOpen());
        CellTheme cell = Theme.Current.GetCell();
        Font detailFont = Theme.Current.GetField().GetFont();
        FontMetrics titleMetrics = g.getFontMetrics(cell.GetFont());
        FontMetrics detailMetrics = g.getFontMetrics(detailFont);
        int pad = layout.GetCardPadding();

        Graphics2D inside = (Graphics2D) g.create();
        try {
            inside.clipRect(box.x, box.y, box.width, box.height);
            String rating = row.GetRating() == null ? "" : TextFit.Fit(titleMetrics, row.GetRating(), LayoutMath.Half(LayoutMath.Half(box.width - reservedRight)));
            int ratingWidth = titleMetrics.stringWidth(rating);
            int textWidth = box.width - reservedRight - pad - ratingWidth - pad - pad;
            int top = box.y + LayoutMath.Centre(box.height, titleMetrics.getHeight() + detailMetrics.getHeight());

            inside.setFont(cell.GetFont());
            inside.setColor(cell.GetText());
            inside.drawString(TextFit.Fit(titleMetrics, row.GetTitle(), textWidth), box.x + pad, top + titleMetrics.getAscent());
            inside.drawString(rating, box.x + box.width - reservedRight - pad - ratingWidth, TextLine.Baseline(box.y, box.height, titleMetrics));

            inside.setFont(detailFont);
            inside.setColor(Theme.Current.GetHeading().GetColour());
            inside.drawString(TextFit.Fit(detailMetrics, row.GetDetail(), textWidth), box.x + pad, top + titleMetrics.getHeight() + detailMetrics.getAscent());
        } finally {
            inside.dispose();
        }
    }
}
