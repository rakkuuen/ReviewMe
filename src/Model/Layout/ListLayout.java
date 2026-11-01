package Model.Layout;

// Game lists: how tall a row is, the space between rows, how far the wheel scrolls, and the space left under the last row
public class ListLayout {
    private final int rowHeight, rowGap, scrollStep, bottomPadding;

    public ListLayout(LayoutValues values){
        rowHeight = values.Get(LayoutKey.LIST_ROW_HEIGHT);
        rowGap = values.Get(LayoutKey.LIST_ROW_GAP);
        scrollStep = values.Get(LayoutKey.LIST_SCROLL_STEP);
        bottomPadding = values.Get(LayoutKey.LIST_BOTTOM_PADDING);
    }

    public int GetRowHeight(){ return rowHeight; }
    public int GetRowGap(){ return rowGap; }
    public int GetScrollStep(){ return scrollStep; }
    public int GetBottomPadding(){ return bottomPadding; }
}
