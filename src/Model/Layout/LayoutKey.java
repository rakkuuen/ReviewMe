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
    SHELL_TOAST_MILLIS("shell.toastMillis", 1),               // How long a short message ("Saved") stays on screen, in milliseconds

    LIST_ROW_HEIGHT("list.rowHeight", 1),
    LIST_ROW_GAP("list.rowGap", 0),
    LIST_SCROLL_STEP("list.scrollStep", 1),
    LIST_BOTTOM_PADDING("list.bottomPadding", 0),
    LIST_SEARCH_WIDTH("list.searchWidth", 1),                 // The search box above a list
    LIST_PAGE_SIZE("list.pageSize", 1, 500),                  // Rows a list shows before "Show more" (a count)

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
    OVERVIEW_CHART_HEIGHT("overview.chartHeight", 1),
    OVERVIEW_SERIES_INDENT("overview.seriesIndent", 0),          // How far a series' games sit in under its heading

    RICH_LINE_HEIGHT_PERCENT("rich.lineHeightPercent", 100, 400),   // Line height as a percentage of the font size (a percentage, not pixels)
    RICH_BLOCK_GAP("rich.blockGap", 0),                             // Between paragraphs, and between a paragraph and a list
    RICH_ITEM_GAP("rich.itemGap", 0),                               // Between list items
    RICH_MARKER_COLUMN("rich.markerColumn", 1),                     // The column a list's bullets and numbers sit in, text starts after it
    RICH_BULLET_SIZE("rich.bulletSize", 1),
    RICH_BULLET_INSET("rich.bulletInset", 0),                       // From the left to the bullet
    RICH_EMOJI_BELOW_BASELINE("rich.emojiBelowBaseline", 0),        // How far an emoji reaches below the text baseline, at 1x
    RICH_EMOJI_SCALE("rich.emojiScale", 1, 8),                      // Pixels per emoji square beside text (a count)
    RICH_EMOJI_ALONE_SCALE("rich.emojiAloneScale", 1, 8),           // The same, for a paragraph of only emoji (a count)
    RICH_LINK_UNDERLINE("rich.linkUnderline", 1),
    RICH_LINK_UNDERLINE_HOVER("rich.linkUnderlineHover", 1);

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
