package Screens.MainView;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import Controller.BackEnd.Overview.StatCard;
import Controller.BackEnd.Overview.YearBar;
import Model.Layout.LayoutMath;
import Model.Layout.OverviewLayout;
import Model.Themes.Theme;
import Model.Themes.TextStyle;

// Draws one stat card: the big value, its label and detail, and for the Completed card the per-year bars on the right. Anything that
// won't fit the card is cut off at its edge. The bars are left out if there is no room for them beside the words
public class StatCardPainter {
    private final OverviewLayout layout;

    public StatCardPainter(OverviewLayout layout){
        this.layout = layout;
    }

    public void Paint(Graphics2D g, Rectangle box, StatCard card, List<YearBar> bars){
        CardBox.Paint(g, box, false);
        Graphics2D inside = (Graphics2D) g.create();
        try {
            inside.clipRect(box.x, box.y, box.width, box.height);
            int pad = layout.GetCardPadding();
            TextStyle valueStyle = Theme.Current.GetTitle(), labelStyle = Theme.Current.GetHeading();
            Font detailFont = Theme.Current.GetField().GetFont();
            FontMetrics valueMetrics = inside.getFontMetrics(valueStyle.GetFont());
            FontMetrics labelMetrics = inside.getFontMetrics(labelStyle.GetFont());
            FontMetrics detailMetrics = inside.getFontMetrics(detailFont);

            int x = box.x + pad;
            int y = box.y + pad;
            inside.setFont(valueStyle.GetFont());
            inside.setColor(valueStyle.GetColour());
            inside.drawString(card.GetValue(), x, y + valueMetrics.getAscent());
            y += valueMetrics.getHeight();
            inside.setFont(labelStyle.GetFont());
            inside.setColor(labelStyle.GetColour());
            inside.drawString(card.GetLabel(), x, y + labelMetrics.getAscent());
            y += labelMetrics.getHeight();
            if(card.GetDetail() != null){
                inside.setFont(detailFont);
                inside.drawString(card.GetDetail(), x, y + detailMetrics.getAscent());
            }

            int wordsRight = x + Math.max(valueMetrics.stringWidth(card.GetValue()), Math.max(labelMetrics.stringWidth(card.GetLabel()),
                    card.GetDetail() == null ? 0 : detailMetrics.stringWidth(card.GetDetail())));
            if(card.ShowsYearChart() && !bars.isEmpty()){
                PaintBars(inside, box, bars, wordsRight, detailMetrics);
            }
        } finally {
            inside.dispose();
        }
    }

    private void PaintBars(Graphics2D g, Rectangle box, List<YearBar> bars, int wordsRight, FontMetrics labelMetrics){
        int pad = layout.GetCardPadding();
        int slot = layout.GetChartBarWidth() + layout.GetChartBarGap();
        for(YearBar bar : bars){
            slot = Math.max(slot, labelMetrics.stringWidth(bar.GetLabel()) + layout.GetChartBarGap());
        }
        int total = slot * bars.size();
        int left = box.x + box.width - pad - total;
        if(left < wordsRight + pad){
            return;
        }

        int baseline = box.y + box.height - pad - labelMetrics.getHeight();
        g.setFont(Theme.Current.GetField().GetFont());
        for(int i = 0; i < bars.size(); i++){
            YearBar bar = bars.get(i);
            int slotX = left + i * slot;
            int barX = slotX + LayoutMath.Centre(slot, layout.GetChartBarWidth());
            int barHeight = (int) Math.round(bar.GetFraction() * layout.GetChartHeight());
            if(bar.GetCount() > 0){
                g.setColor(Theme.Current.GetNav().GetSelected());
                g.fillRect(barX, baseline - Math.max(barHeight, 1), layout.GetChartBarWidth(), Math.max(barHeight, 1));
            } else {
                g.setColor(Theme.Current.GetCell().GetBorder());
                g.fillRect(barX, baseline - 1, layout.GetChartBarWidth(), 1);
            }
            g.setColor(Theme.Current.GetHeading().GetColour());
            g.drawString(bar.GetLabel(), slotX + LayoutMath.Centre(slot, labelMetrics.stringWidth(bar.GetLabel())), baseline + labelMetrics.getAscent());
        }
    }
}
