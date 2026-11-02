package Model.Layout;

// Game lists: how tall a row is, the space between rows, how far the wheel scrolls, the space left under the last row, how wide the
// search box is and how many rows show before "Show more"
public class ListLayout {
    private final int rowHeight, rowGap, scrollStep, bottomPadding, searchWidth, pageSize;

    public ListLayout(LayoutValues values){
        rowHeight = values.Get(LayoutKey.LIST_ROW_HEIGHT);
        rowGap = values.Get(LayoutKey.LIST_ROW_GAP);
        scrollStep = values.Get(LayoutKey.LIST_SCROLL_STEP);
        bottomPadding = values.Get(LayoutKey.LIST_BOTTOM_PADDING);
        searchWidth = values.Get(LayoutKey.LIST_SEARCH_WIDTH);
        pageSize = values.Get(LayoutKey.LIST_PAGE_SIZE);
    }

    public int GetRowHeight(){ return rowHeight; }
    public int GetRowGap(){ return rowGap; }
    public int GetScrollStep(){ return scrollStep; }
    public int GetBottomPadding(){ return bottomPadding; }
    public int GetSearchWidth(){ return searchWidth; }
    public int GetPageSize(){ return pageSize; }
}
