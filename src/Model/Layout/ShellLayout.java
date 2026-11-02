package Model.Layout;

// The frame around every screen: the header, the State row, the tab bar, and the space between them, and how long a short message stays
public class ShellLayout {
    private final int padding, gap, headerHeight, stateRowHeight, tabBarHeight, itemPaddingX, itemGap, toastMillis;

    public ShellLayout(LayoutValues values){
        padding = values.Get(LayoutKey.SHELL_PADDING);
        gap = values.Get(LayoutKey.SHELL_GAP);
        headerHeight = values.Get(LayoutKey.SHELL_HEADER_HEIGHT);
        stateRowHeight = values.Get(LayoutKey.SHELL_STATE_ROW_HEIGHT);
        tabBarHeight = values.Get(LayoutKey.SHELL_TAB_BAR_HEIGHT);
        itemPaddingX = values.Get(LayoutKey.SHELL_ITEM_PADDING_X);
        itemGap = values.Get(LayoutKey.SHELL_ITEM_GAP);
        toastMillis = values.Get(LayoutKey.SHELL_TOAST_MILLIS);
    }

    public int GetPadding(){ return padding; }
    public int GetGap(){ return gap; }
    public int GetHeaderHeight(){ return headerHeight; }
    public int GetStateRowHeight(){ return stateRowHeight; }
    public int GetTabBarHeight(){ return tabBarHeight; }
    public int GetItemPaddingX(){ return itemPaddingX; }
    public int GetItemGap(){ return itemGap; }
    public int GetToastMillis(){ return toastMillis; }
}
