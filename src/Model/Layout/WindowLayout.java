package Model.Layout;

// The window's size and the width at which screens switch to their narrow layout
public class WindowLayout {
    private final int startWidth, startHeight, minWidth, minHeight, narrowBreakpoint;

    public WindowLayout(LayoutValues values){
        startWidth = values.Get(LayoutKey.WINDOW_START_WIDTH);
        startHeight = values.Get(LayoutKey.WINDOW_START_HEIGHT);
        minWidth = values.Get(LayoutKey.WINDOW_MIN_WIDTH);
        minHeight = values.Get(LayoutKey.WINDOW_MIN_HEIGHT);
        narrowBreakpoint = values.Get(LayoutKey.WINDOW_NARROW_BREAKPOINT);
    }

    public int GetStartWidth(){ return startWidth; }
    public int GetStartHeight(){ return startHeight; }
    public int GetMinWidth(){ return minWidth; }
    public int GetMinHeight(){ return minHeight; }
    public int GetNarrowBreakpoint(){ return narrowBreakpoint; }

    public boolean IsNarrow(int width){
        return width < narrowBreakpoint;
    }
}
