package Model.Layout;

// Every size the layout settings hold: the name used in layout.properties, and the range it may take. A key has to be in the
// file, and nothing else may be (see LayoutLoader). A theme file can override any of them as layout.<name>
public enum LayoutKey {
    WINDOW_START_WIDTH("window.startWidth", 1),
    WINDOW_START_HEIGHT("window.startHeight", 1),
    WINDOW_MIN_WIDTH("window.minWidth", 1),
    WINDOW_MIN_HEIGHT("window.minHeight", 1),
    WINDOW_NARROW_BREAKPOINT("window.narrowBreakpoint", 1),   // The Overview and lists use their narrow layout below this width

    SHELL_PADDING("shell.padding", 0),
    SHELL_GAP("shell.gap", 0),
    SHELL_HEADER_HEIGHT("shell.headerHeight", 1),
    SHELL_STATE_ROW_HEIGHT("shell.stateRowHeight", 1),
    SHELL_TAB_BAR_HEIGHT("shell.tabBarHeight", 1),
    SHELL_ITEM_PADDING_X("shell.itemPaddingX", 0),            // Sideways padding inside a state button or tab
    SHELL_ITEM_GAP("shell.itemGap", 0),                       // Space between state buttons, and between tabs

    LIST_ROW_HEIGHT("list.rowHeight", 1),
    LIST_ROW_GAP("list.rowGap", 0),
    LIST_SCROLL_STEP("list.scrollStep", 1),
    LIST_BOTTOM_PADDING("list.bottomPadding", 0),

    OVERVIEW_ROWS_PER_SECTION("overview.rowsPerSection", 1, 100),
    OVERVIEW_TOP_PICK_SLOTS("overview.topPickSlots", 1, 5),   // 5 is the most the database holds (the topPickSlot column's check)
    OVERVIEW_STAT_CARD_HEIGHT("overview.statCardHeight", 1),
    OVERVIEW_STAT_CARD_MIN_WIDTH("overview.statCardMinWidth", 1),   // Cards wrap onto more lines rather than get narrower than this
    OVERVIEW_CARD_PADDING("overview.cardPadding", 0),
    OVERVIEW_SECTION_GAP("overview.sectionGap", 0),
    OVERVIEW_COLUMN_GAP("overview.columnGap", 0),
    OVERVIEW_SECTION_HEADER_HEIGHT("overview.sectionHeaderHeight", 1),
    OVERVIEW_SIDE_PANEL_WIDTH("overview.sidePanelWidth", 1),        // The Top picks panel beside the stats (below them in a narrow window)
    OVERVIEW_TOP_PICK_ROW_HEIGHT("overview.topPickRowHeight", 1),
    OVERVIEW_CHART_YEARS("overview.chartYears", 1, 12),             // Bars in the completed-per-year chart (a count)
    OVERVIEW_CHART_BAR_WIDTH("overview.chartBarWidth", 1),
    OVERVIEW_CHART_BAR_GAP("overview.chartBarGap", 0),
    OVERVIEW_CHART_HEIGHT("overview.chartHeight", 1);

    private final String propertyName;
    private final int min;
    private final int max;

    LayoutKey(String propertyName, int min){
        this(propertyName, min, Integer.MAX_VALUE);
    }

    LayoutKey(String propertyName, int min, int max){
        this.propertyName = propertyName;
        this.min = min;
        this.max = max;
    }

    public String GetPropertyName(){ return propertyName; }
    public int GetMin(){ return min; }
    public int GetMax(){ return max; }

    // Null if there is no such key
    public static LayoutKey FromPropertyName(String name){
        for(LayoutKey key : values()){
            if(key.propertyName.equals(name)){
                return key;
            }
        }
        return null;
    }
}
