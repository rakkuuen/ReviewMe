package Screens.MainView;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import Controller.BackEnd.Overview.OverviewData;
import Controller.BackEnd.Overview.StatCard;
import Controller.BackEnd.Overview.StatCards;
import Controller.BackEnd.Overview.TopPickItem;
import Controller.BackEnd.Overview.TopPickItems;
import Controller.BackEnd.Overview.YearBar;
import Controller.BackEnd.Overview.YearBars;
import Model.Layout.Layout;
import Model.Layout.OverviewLayout;
import Model.Themes.Theme;

// The Overview tab: the stat cards and the Top picks panel side by side (the Top picks drop below the stats in a narrow window)
public class OverviewBody implements BodyPainter {
    private static final String statsCaption = "Your stats";

    private final OverviewData data;
    private final Layout layout;
    private final List<StatCard> cards;
    private final List<YearBar> bars;
    private final List<TopPickItem> picks;
    private final StatCardPainter cardPainter;
    private final TopPicksPanel picksPanel;

    public OverviewBody(OverviewData data, Layout layout){
        this.data = data;
        this.layout = layout;
        this.cards = StatCards.Build(data.GetStats());
        this.bars = YearBars.Build(data.GetStats().GetCompletedByYear(), layout.GetOverview().GetChartYears());
        this.picks = TopPickItems.Build(data.GetTopPicks());
        this.cardPainter = new StatCardPainter(layout.GetOverview());
        this.picksPanel = new TopPicksPanel(layout.GetOverview(), layout.GetList());
    }

    @Override
    public int Paint(Graphics2D g, Rectangle viewport, Point mouse, HitMap hits){
        OverviewLayout overview = layout.GetOverview();
        boolean sideBySide = !layout.GetWindow().IsNarrow(viewport.width);
        int statsWidth = sideBySide ? viewport.width - overview.GetSidePanelWidth() - overview.GetColumnGap() : viewport.width;

        int statsHeight = PaintStats(g, new Rectangle(0, 0, statsWidth, 0));
        int picksHeight;
        if(sideBySide){
            picksHeight = picksPanel.Paint(g, new Rectangle(statsWidth + overview.GetColumnGap(), 0, overview.GetSidePanelWidth(), 0), picks);
            return Math.max(viewport.height, Math.max(statsHeight, picksHeight));
        }
        picksHeight = picksPanel.Paint(g, new Rectangle(0, statsHeight + overview.GetSectionGap(), viewport.width, 0), picks);
        return Math.max(viewport.height, statsHeight + overview.GetSectionGap() + picksHeight);
    }

    // The "Your stats" heading and the cards under it, as many to a line as fit. Returns how tall that came out
    private int PaintStats(Graphics2D g, Rectangle area){
        OverviewLayout overview = layout.GetOverview();
        Font headingFont = Theme.Current.GetHeading().GetFont();
        FontMetrics headingMetrics = g.getFontMetrics(headingFont);
        g.setFont(headingFont);
        g.setColor(Theme.Current.GetHeading().GetColour());
        g.drawString(statsCaption, area.x, TextLine.Baseline(area.y, overview.GetSectionHeaderHeight(), headingMetrics));

        int gap = overview.GetColumnGap();
        int totalSpan = 0;
        for(StatCard card : cards){
            totalSpan += card.GetSpan();
        }
        int unitsPerLine = Math.max(1, Math.min(totalSpan, (area.width + gap) / (overview.GetStatCardMinWidth() + gap)));
        int unitWidth = (area.width - gap * (unitsPerLine - 1)) / unitsPerLine;

        int y = area.y + overview.GetSectionHeaderHeight();
        int unit = 0;
        for(StatCard card : cards){
            int span = Math.min(card.GetSpan(), unitsPerLine);
            if(unit > 0 && unit + span > unitsPerLine){
                unit = 0;
                y += overview.GetStatCardHeight() + gap;
            }
            int width = unitWidth * span + gap * (span - 1);
            cardPainter.Paint(g, new Rectangle(area.x + unit * (unitWidth + gap), y, width, overview.GetStatCardHeight()), card, bars);
            unit += span;
        }
        return y + overview.GetStatCardHeight() - area.y;
    }
}
