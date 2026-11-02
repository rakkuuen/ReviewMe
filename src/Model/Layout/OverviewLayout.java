package Model.Layout;

// The Overview: how many rows each short list shows, how many Top picks slots there are, and the sizes of its pieces
public class OverviewLayout {
    private final int rowsPerSection, topPickSlots, statCardHeight, statCardMinWidth, cardPadding, sectionGap, columnGap, sectionHeaderHeight;
    private final int sidePanelWidth, topPickRowHeight, chartYears, chartBarWidth, chartBarGap, chartHeight, seriesIndent;

    public OverviewLayout(LayoutValues values){
        rowsPerSection = values.Get(LayoutKey.OVERVIEW_ROWS_PER_SECTION);
        topPickSlots = values.Get(LayoutKey.OVERVIEW_TOP_PICK_SLOTS);
        statCardHeight = values.Get(LayoutKey.OVERVIEW_STAT_CARD_HEIGHT);
        statCardMinWidth = values.Get(LayoutKey.OVERVIEW_STAT_CARD_MIN_WIDTH);
        cardPadding = values.Get(LayoutKey.OVERVIEW_CARD_PADDING);
        sectionGap = values.Get(LayoutKey.OVERVIEW_SECTION_GAP);
        columnGap = values.Get(LayoutKey.OVERVIEW_COLUMN_GAP);
        sectionHeaderHeight = values.Get(LayoutKey.OVERVIEW_SECTION_HEADER_HEIGHT);
        sidePanelWidth = values.Get(LayoutKey.OVERVIEW_SIDE_PANEL_WIDTH);
        topPickRowHeight = values.Get(LayoutKey.OVERVIEW_TOP_PICK_ROW_HEIGHT);
        chartYears = values.Get(LayoutKey.OVERVIEW_CHART_YEARS);
        chartBarWidth = values.Get(LayoutKey.OVERVIEW_CHART_BAR_WIDTH);
        chartBarGap = values.Get(LayoutKey.OVERVIEW_CHART_BAR_GAP);
        chartHeight = values.Get(LayoutKey.OVERVIEW_CHART_HEIGHT);
        seriesIndent = values.Get(LayoutKey.OVERVIEW_SERIES_INDENT);
    }

    public int GetRowsPerSection(){ return rowsPerSection; }
    public int GetTopPickSlots(){ return topPickSlots; }
    public int GetStatCardHeight(){ return statCardHeight; }
    public int GetStatCardMinWidth(){ return statCardMinWidth; }
    public int GetCardPadding(){ return cardPadding; }
    public int GetSectionGap(){ return sectionGap; }
    public int GetColumnGap(){ return columnGap; }
    public int GetSectionHeaderHeight(){ return sectionHeaderHeight; }
    public int GetSidePanelWidth(){ return sidePanelWidth; }
    public int GetTopPickRowHeight(){ return topPickRowHeight; }
    public int GetChartYears(){ return chartYears; }
    public int GetChartBarWidth(){ return chartBarWidth; }
    public int GetChartBarGap(){ return chartBarGap; }
    public int GetChartHeight(){ return chartHeight; }
    public int GetSeriesIndent(){ return seriesIndent; }
}
