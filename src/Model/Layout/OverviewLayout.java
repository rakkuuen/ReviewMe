package Model.Layout;

// The Overview: how many rows each short list shows, how many Top picks slots there are, and the sizes of its pieces
public class OverviewLayout {
    private final int rowsPerSection, topPickSlots, statCardHeight, sectionGap, columnGap, sectionHeaderHeight;

    public OverviewLayout(LayoutValues values){
        rowsPerSection = values.Get(LayoutKey.OVERVIEW_ROWS_PER_SECTION);
        topPickSlots = values.Get(LayoutKey.OVERVIEW_TOP_PICK_SLOTS);
        statCardHeight = values.Get(LayoutKey.OVERVIEW_STAT_CARD_HEIGHT);
        sectionGap = values.Get(LayoutKey.OVERVIEW_SECTION_GAP);
        columnGap = values.Get(LayoutKey.OVERVIEW_COLUMN_GAP);
        sectionHeaderHeight = values.Get(LayoutKey.OVERVIEW_SECTION_HEADER_HEIGHT);
    }

    public int GetRowsPerSection(){ return rowsPerSection; }
    public int GetTopPickSlots(){ return topPickSlots; }
    public int GetStatCardHeight(){ return statCardHeight; }
    public int GetSectionGap(){ return sectionGap; }
    public int GetColumnGap(){ return columnGap; }
    public int GetSectionHeaderHeight(){ return sectionHeaderHeight; }
}
