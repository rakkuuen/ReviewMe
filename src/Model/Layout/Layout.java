package Model.Layout;

// Every size the screens use, in groups. Built by LayoutLoader from layout.properties (and a theme's overrides). Screens are handed
// the group they need, so they never reach for a global or write a number of their own
public class Layout {
    private final WindowLayout window;
    private final ShellLayout shell;
    private final ListLayout list;
    private final OverviewLayout overview;
    private final RichSizes rich;

    public Layout(LayoutValues values){
        window = new WindowLayout(values);
        shell = new ShellLayout(values);
        list = new ListLayout(values);
        overview = new OverviewLayout(values);
        rich = new RichSizes(values);
    }

    public WindowLayout GetWindow(){ return window; }
    public ShellLayout GetShell(){ return shell; }
    public ListLayout GetList(){ return list; }
    public OverviewLayout GetOverview(){ return overview; }
    public RichSizes GetRich(){ return rich; }
}
