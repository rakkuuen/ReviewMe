package Screens.MainView;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import Controller.BackEnd.MainView.ViewTarget;
import Controller.BackEnd.Overview.GameRow;
import Controller.BackEnd.Overview.OverviewData;
import Controller.BackEnd.Overview.OverviewRows;
import Controller.BackEnd.Overview.OverviewSection;
import Controller.BackEnd.Overview.OverviewSectionInfo;
import Controller.BackEnd.Overview.OverviewSectionKind;
import Controller.BackEnd.Overview.OverviewSeriesGroup;
import Controller.BackEnd.Overview.SeriesHeading;
import Controller.BackEnd.Overview.StatCard;
import Controller.BackEnd.Overview.StatCards;
import Controller.BackEnd.Overview.TopPickItem;
import Controller.BackEnd.Overview.TopPickItems;
import Controller.BackEnd.Overview.YearBar;
import Controller.BackEnd.Overview.YearBars;
import Model.Layout.Layout;
import Model.Layout.LayoutMath;
import Model.Layout.OverviewLayout;
import Model.Library.LibraryGame;
import Model.Themes.Theme;

// The Overview tab: the stat cards and Top picks side by side (Top picks below the stats in a narrow window), then the short lists:
// Incomplete next to More achievements, then In progress, Completed, Series, Shelved and Not started. Each list has its heading, how
// many games it holds and a "View all" when there are more than it shows. A series heading collapses and opens its games when clicked.
// Going anywhere is left to the callbacks: viewAll for "View all", openGame for a game row
public class OverviewBody implements BodyPainter {
    private static final String statsCaption = "Your stats";
    private static final String viewAllWords = "View all";
    private static final String collapsedMark = "+";
    private static final String openMark = "-";
    private static final OverviewSectionKind[] pairedKinds = {OverviewSectionKind.INCOMPLETE, OverviewSectionKind.MORE_ACHIEVEMENTS};
    private static final OverviewSectionKind[] stackedBeforeSeries = {OverviewSectionKind.IN_PROGRESS, OverviewSectionKind.COMPLETED};
    private static final OverviewSectionKind[] stackedAfterSeries = {OverviewSectionKind.SHELVED, OverviewSectionKind.NOT_STARTED};

    private OverviewData data;
    private final Layout layout;
    private final Consumer<ViewTarget> viewAll;
    private final Consumer<LibraryGame> openGame;
    private List<StatCard> cards;
    private List<YearBar> bars;
    private List<TopPickItem> picks;
    private final StatCardPainter cardPainter;
    private final TopPicksPanel picksPanel;
    private final GameRowPainter rowPainter;
    private final Set<String> collapsedSeries = new HashSet<>();

    public OverviewBody(OverviewData data, Layout layout){
        this(data, layout, target -> { }, game -> { });
    }

    public OverviewBody(OverviewData data, Layout layout, Consumer<ViewTarget> viewAll, Consumer<LibraryGame> openGame){
        this.layout = layout;
        this.viewAll = viewAll;
        this.openGame = openGame;
        Reload(data);
        this.cardPainter = new StatCardPainter(layout.GetOverview());
        this.picksPanel = new TopPicksPanel(layout.GetOverview(), layout.GetList());
        this.rowPainter = new GameRowPainter(layout.GetOverview());
    }

    // Shows newer numbers. Series that were folded stay folded
    public void Reload(OverviewData fresh){
        this.data = fresh;
        this.cards = StatCards.Build(fresh.GetStats());
        this.bars = YearBars.Build(fresh.GetStats().GetCompletedByYear(), layout.GetOverview().GetChartYears());
        this.picks = TopPickItems.Build(fresh.GetTopPicks());
    }

    @Override
    public int Paint(Graphics2D g, Rectangle viewport, Point mouse, HitMap hits){
        OverviewLayout overview = layout.GetOverview();
        boolean sideBySide = !layout.GetWindow().IsNarrow(viewport.width);
        int statsWidth = sideBySide ? viewport.width - overview.GetSidePanelWidth() - overview.GetColumnGap() : viewport.width;

        int statsHeight = PaintStats(g, new Rectangle(0, 0, statsWidth, 0));
        int y;
        if(sideBySide){
            y = Math.max(statsHeight, picksPanel.Paint(g, new Rectangle(statsWidth + overview.GetColumnGap(), 0, overview.GetSidePanelWidth(), 0), picks));
        } else {
            y = statsHeight + overview.GetSectionGap() + picksPanel.Paint(g, new Rectangle(0, statsHeight + overview.GetSectionGap(), viewport.width, 0), picks);
        }
        y += overview.GetSectionGap();

        y += PaintPair(g, y, viewport.width, sideBySide, mouse, hits);
        for(OverviewSectionKind kind : stackedBeforeSeries){
            y += PaintList(g, new Rectangle(0, y, viewport.width, 0), kind, mouse, hits) + overview.GetSectionGap();
        }
        y += PaintSeries(g, new Rectangle(0, y, viewport.width, 0), mouse, hits) + overview.GetSectionGap();
        for(OverviewSectionKind kind : stackedAfterSeries){
            y += PaintList(g, new Rectangle(0, y, viewport.width, 0), kind, mouse, hits) + overview.GetSectionGap();
        }
        return Math.max(viewport.height, y - overview.GetSectionGap() + layout.GetList().GetBottomPadding());
    }

    // Incomplete and More achievements: side by side when there is room, one under the other when not. Returns the height with a gap after
    private int PaintPair(Graphics2D g, int y, int width, boolean sideBySide, Point mouse, HitMap hits){
        OverviewLayout overview = layout.GetOverview();
        if(!sideBySide){
            int first = PaintList(g, new Rectangle(0, y, width, 0), pairedKinds[0], mouse, hits);
            int second = PaintList(g, new Rectangle(0, y + first + overview.GetSectionGap(), width, 0), pairedKinds[1], mouse, hits);
            return first + overview.GetSectionGap() + second + overview.GetSectionGap();
        }
        int columnWidth = LayoutMath.Half(width - overview.GetColumnGap());
        int left = PaintList(g, new Rectangle(0, y, columnWidth, 0), pairedKinds[0], mouse, hits);
        int right = PaintList(g, new Rectangle(columnWidth + overview.GetColumnGap(), y, width - columnWidth - overview.GetColumnGap(), 0), pairedKinds[1], mouse, hits);
        return Math.max(left, right) + overview.GetSectionGap();
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

    // One list: heading, count and "View all", then its rows (or a muted line if it is empty). Returns how tall it came out
    private int PaintList(Graphics2D g, Rectangle area, OverviewSectionKind kind, Point mouse, HitMap hits){
        OverviewLayout overview = layout.GetOverview();
        OverviewSection section = data.GetSection(kind);
        PaintHeading(g, area, OverviewSectionInfo.Title(kind), section.GetTotal(), section.HasMore(), "viewall:" + kind.name(), OverviewSectionInfo.ViewAll(kind), hits);

        int y = area.y + overview.GetSectionHeaderHeight();
        if(section.GetRows().isEmpty()){
            return y + PaintEmpty(g, new Rectangle(area.x, y, area.width, layout.GetList().GetRowHeight()), OverviewSectionInfo.EmptyText(kind)) - area.y;
        }
        int index = 0;
        for(LibraryGame game : section.GetRows()){
            y += PaintGameRow(g, new Rectangle(area.x, y, area.width, layout.GetList().GetRowHeight()), OverviewRows.Build(kind, game), "row:" + kind.name() + ":" + index, mouse, hits);
            y += layout.GetList().GetRowGap();
            index++;
        }
        return y - layout.GetList().GetRowGap() - area.y;
    }

    // The Series list: each series gets a heading line (click to fold it) with its games in order under it
    private int PaintSeries(Graphics2D g, Rectangle area, Point mouse, HitMap hits){
        OverviewLayout overview = layout.GetOverview();
        List<OverviewSeriesGroup> groups = data.GetSeriesGroups();
        PaintHeading(g, area, OverviewSectionInfo.SeriesTitle(), data.GetSeriesTotal(), data.GetSeriesTotal() > groups.size(), "viewall:SERIES", OverviewSectionInfo.SeriesViewAll(), hits);

        int y = area.y + overview.GetSectionHeaderHeight();
        if(groups.isEmpty()){
            return y + PaintEmpty(g, new Rectangle(area.x, y, area.width, layout.GetList().GetRowHeight()), OverviewSectionInfo.SeriesEmptyText()) - area.y;
        }
        for(OverviewSeriesGroup group : groups){
            String id = group.GetSeries().GetId();
            boolean collapsed = collapsedSeries.contains(id);
            y += PaintGroupHeading(g, new Rectangle(area.x, y, area.width, layout.GetList().GetRowHeight()), group, collapsed, mouse, hits) + layout.GetList().GetRowGap();
            if(!collapsed){
                int index = 0;
                for(LibraryGame game : group.GetGames()){
                    y += PaintGameRow(g, new Rectangle(area.x + overview.GetSeriesIndent(), y, area.width - overview.GetSeriesIndent(), layout.GetList().GetRowHeight()),
                            OverviewRows.BuildForSeries(game), "row:series:" + id + ":" + index, mouse, hits) + layout.GetList().GetRowGap();
                    index++;
                }
            }
        }
        return y - layout.GetList().GetRowGap() - area.y;
    }

    // The title, the number of games after it, and "View all" at the right when there is more than is shown
    private void PaintHeading(Graphics2D g, Rectangle area, String title, int total, boolean hasMore, String key, ViewTarget target, HitMap hits){
        OverviewLayout overview = layout.GetOverview();
        Font headingFont = Theme.Current.GetHeading().GetFont(), smallFont = Theme.Current.GetField().GetFont();
        FontMetrics headingMetrics = g.getFontMetrics(headingFont), smallMetrics = g.getFontMetrics(smallFont);
        int headerHeight = overview.GetSectionHeaderHeight();

        g.setFont(headingFont);
        g.setColor(Theme.Current.GetHeading().GetColour());
        g.drawString(title, area.x, TextLine.Baseline(area.y, headerHeight, headingMetrics));
        g.setFont(smallFont);
        g.drawString(String.valueOf(total), area.x + headingMetrics.stringWidth(title) + layout.GetShell().GetItemGap(), TextLine.Baseline(area.y, headerHeight, smallMetrics));

        if(hasMore){
            int width = smallMetrics.stringWidth(viewAllWords);
            g.setColor(Theme.Current.GetNav().GetSelected());
            g.drawString(viewAllWords, area.x + area.width - width, TextLine.Baseline(area.y, headerHeight, smallMetrics));
            hits.Add(key, new Rectangle(area.x + area.width - width, area.y, width, headerHeight), () -> viewAll.accept(target));
        }
    }

    // A row for a game; it can be clicked when the game has a review to open. Returns its height
    private int PaintGameRow(Graphics2D g, Rectangle box, GameRow row, String key, Point mouse, HitMap hits){
        rowPainter.Paint(g, box, row, box.contains(mouse));
        if(row.CanOpen()){
            hits.Add(key, box, () -> openGame.accept(row.GetGame()));
        }
        return box.height;
    }

    // A series heading: + or - to show whether it is folded, the name, and its summary
    private int PaintGroupHeading(Graphics2D g, Rectangle box, OverviewSeriesGroup group, boolean collapsed, Point mouse, HitMap hits){
        CardBox.Paint(g, box, box.contains(mouse));
        Font headingFont = Theme.Current.GetHeading().GetFont(), smallFont = Theme.Current.GetField().GetFont();
        FontMetrics headingMetrics = g.getFontMetrics(headingFont), smallMetrics = g.getFontMetrics(smallFont);
        int pad = layout.GetOverview().GetCardPadding();

        Graphics2D inside = (Graphics2D) g.create();
        try {
            inside.clipRect(box.x, box.y, box.width, box.height);
            inside.setFont(headingFont);
            inside.setColor(Theme.Current.GetHeading().GetColour());
            String mark = collapsed ? collapsedMark : openMark;
            int markX = box.x + pad;
            int nameX = markX + headingMetrics.stringWidth(collapsedMark) + pad;
            inside.drawString(mark, markX, TextLine.Baseline(box.y, box.height, headingMetrics));
            inside.drawString(SeriesHeading.Name(group), nameX, TextLine.Baseline(box.y, box.height, headingMetrics));
            String summary = SeriesHeading.Summary(group);
            inside.setFont(smallFont);
            inside.drawString(summary, box.x + box.width - pad - smallMetrics.stringWidth(summary), TextLine.Baseline(box.y, box.height, smallMetrics));
        } finally {
            inside.dispose();
        }
        String id = group.GetSeries().GetId();
        hits.Add("series:" + id, box, () -> {
            if(!collapsedSeries.remove(id)){
                collapsedSeries.add(id);
            }
        });
        return box.height;
    }

    private int PaintEmpty(Graphics2D g, Rectangle box, String text){
        CardBox.Paint(g, box, false);
        Font smallFont = Theme.Current.GetField().GetFont();
        FontMetrics metrics = g.getFontMetrics(smallFont);
        g.setFont(smallFont);
        g.setColor(Theme.Current.GetNav().GetDisabledText());
        g.drawString(text, box.x + layout.GetOverview().GetCardPadding(), TextLine.Baseline(box.y, box.height, metrics));
        return box.height;
    }
}
