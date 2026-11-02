package Screens.Library;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import Controller.BackEnd.LibraryView.DateRange;
import Controller.BackEnd.LibraryView.LibraryBuilder;
import Controller.BackEnd.LibraryView.LibraryQuery;
import Controller.BackEnd.LibraryView.LibraryResult;
import Controller.BackEnd.LibraryView.LibraryWords;
import Controller.BackEnd.ListTab.ListChip;
import Controller.BackEnd.ListTab.RowAction;
import Controller.BackEnd.ListTab.RowActions;
import Controller.BackEnd.ListTab.ListWords;
import Controller.BackEnd.Overview.GameRow;
import Model.Layout.Layout;
import Model.Layout.LayoutMath;
import Model.Layout.ListLayout;
import Model.Layout.ShellLayout;
import Model.Library.LibraryGame;
import Model.Reviews.GameState;
import Model.Reviews.ReviewState;
import Model.Reviews.Series;
import Model.Themes.TextStyle;
import Model.Themes.Theme;
import Screens.MainView.GameRowPainter;
import Screens.MainView.HitMap;
import Screens.MainView.NavEntry;
import Screens.MainView.NavStrip;
import Screens.MainView.SearchField;
import Screens.MainView.TextFit;

// The Library: every game, with a search box, order, state and review chips, the filters that are on (click one to remove it, or Clear
// all), then the rows. Draws what LibraryBuilder worked out and keeps only what is picked (the query) and the typing in the search box.
// The whole page scrolls
public class LibraryScreen {
    public static final String backKey = "library:back";
    public static final String searchKey = "library:search";
    public static final String sortKey = "library:sort";
    public static final String directionKey = "library:direction";
    public static final String moreKey = "library:more";
    public static final String clearAllKey = "library:clearall";
    public static final String clearFiltersKey = "library:clearfilters";
    public static final String backWords = "Back";

    public static String StateKey(GameState state){ return "library:state:" + state.name(); }
    public static String ReviewKey(ReviewState review){ return "library:review:" + review.name(); }
    public static final String tagKey = "library:tag";
    public static String FilterKey(String filterKey){ return "library:filter:" + filterKey; }
    public static String RowKey(int index){ return "library:row:" + index; }
    public static String ActionKey(int index, RowAction action){ return "library:act:" + index + ":" + action.name(); }
    public static String MenuKey(int index){ return "library:menu:" + index; }

    public static final String seriesFilterKey = "library:filter:series";
    public static final String genreFilterKey = "library:filter:genre";
    public static final String platformFilterKey = "library:filter:platform";
    public static final String completedFilterKey = "library:filter:completed";
    public static final String addedFilterKey = "library:filter:added";

    private List<LibraryGame> games;
    private List<Series> series;
    private final LibraryPopups popups;
    private final Layout layout;
    private final Runnable onBack;
    private final Consumer<LibraryGame> onOpen;
    private final BiConsumer<RowAction, LibraryGame> onAction;
    private final BiConsumer<LibraryGame, List<RowAction>> onMenu;
    private final HitMap hits = new HitMap();
    private final NavStrip strip;
    private final GameRowPainter rowPainter;
    private final SearchField searchField;
    private LibraryQuery query = LibraryQuery.Initial();
    private String typed = "";       // What is in the search box, spaces and all
    private boolean searchFocused;
    private LibraryResult result;    // Null when it has to be worked out again
    private String pressedKey;
    private int scroll;
    private int contentHeight;
    private int viewportHeight;

    public LibraryScreen(List<LibraryGame> games, Layout layout, Runnable onBack, Consumer<LibraryGame> onOpen, BiConsumer<RowAction, LibraryGame> onAction,
            BiConsumer<LibraryGame, List<RowAction>> onMenu){
        this(games, new ArrayList<>(), layout, onBack, onOpen, onAction, onMenu, LibraryPopups.None());
    }

    // series is where the Series filter's names come from; popups shows the lists and date ranges the filter buttons ask for
    public LibraryScreen(List<LibraryGame> games, List<Series> series, Layout layout, Runnable onBack, Consumer<LibraryGame> onOpen, BiConsumer<RowAction, LibraryGame> onAction,
            BiConsumer<LibraryGame, List<RowAction>> onMenu, LibraryPopups popups){
        this.games = games;
        this.series = series;
        this.popups = popups;
        this.layout = layout;
        this.onBack = onBack;
        this.onOpen = onOpen;
        this.onAction = onAction;
        this.onMenu = onMenu;
        this.strip = new NavStrip(layout.GetShell());
        this.rowPainter = new GameRowPainter(layout.GetOverview());
        this.searchField = new SearchField(layout.GetShell());
    }

    // Newer data: the lists are worked out again, with what was picked kept
    public void Reload(List<LibraryGame> fresh){
        this.games = fresh;
        result = null;
    }

    public void Reload(List<LibraryGame> fresh, List<Series> freshSeries){
        this.series = freshSeries;
        Reload(fresh);
    }

    public boolean IsTyping(){ return searchFocused; }

    public boolean StopTyping(){
        boolean was = searchFocused;
        searchFocused = false;
        return was;
    }

    // A letter typed while the search box has the keys (a backspace comes as '\b'). True if it changed the list
    public boolean KeyTyped(char letter){
        if(!searchFocused){
            return false;
        }
        if(letter == '\b'){
            if(typed.isEmpty()){
                return false;
            }
            typed = typed.substring(0, typed.length() - 1);
        } else if(Character.isISOControl(letter)){
            return false;
        } else {
            typed = typed + letter;
        }
        Pick(query.WithSearch(typed));
        scroll = 0;
        return true;
    }

    public void Paint(Graphics graphics, Point mouse, Dimension size){
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            hits.Clear();
            ShellLayout shell = layout.GetShell();
            Rectangle area = new Rectangle(shell.GetPadding(), shell.GetPadding(), Math.max(0, size.width - shell.GetPadding() - shell.GetPadding()),
                    Math.max(0, size.height - shell.GetPadding() - shell.GetPadding()));
            viewportHeight = area.height;
            scroll = ClampScroll(scroll);
            int mark = hits.Size();
            int scrolledTo = scroll;
            PaintPage(g, area, mouse);
            if(scroll != scrolledTo){
                // The page turned out shorter than the scroll allowed: wipe that attempt and paint from where it can be
                hits.TruncateTo(mark);
                g.setClip(null);
                g.setColor(Theme.Current.GetBackground());
                g.fillRect(0, 0, size.width, size.height);
                PaintPage(g, area, mouse);
            }
        } finally {
            g.dispose();
        }
    }

    private void PaintPage(Graphics2D g, Rectangle area, Point mouse){
        Graphics2D inPage = (Graphics2D) g.create();
        try {
            inPage.clipRect(area.x, area.y, area.width, area.height);
            inPage.translate(area.x, area.y - scroll);
            hits.SetFrame(area.x, area.y - scroll, area);
            contentHeight = PaintContent(inPage, area.width, new Point(mouse.x - area.x, mouse.y - area.y + scroll));
        } finally {
            hits.ClearFrame();
            inPage.dispose();
        }
        scroll = ClampScroll(scroll);
    }

    private int PaintContent(Graphics2D g, int width, Point mouse){
        ShellLayout shell = layout.GetShell();
        LibraryResult shown = Result();
        int y = 0;

        List<NavEntry> back = new ArrayList<>();
        back.add(new NavEntry(backKey, backWords, null, false, true, onBack));
        y += strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, null, back, mouse, hits) + shell.GetGap();
        y += PaintTitle(g, y, width) + shell.GetGap();
        y += PaintSearchLine(g, y, width, mouse) + shell.GetGap();
        y += PaintChips(g, y, width, LibraryWords.stateCaption, StateEntries(shown), mouse);
        y += PaintChips(g, y, width, LibraryWords.moreFiltersCaption, MoreFilterEntries(shown), mouse);
        y += PaintChips(g, y, width, LibraryWords.reviewCaption, ReviewEntries(shown), mouse);
        y += PaintChips(g, y, width, LibraryWords.filtersCaption, FilterEntries(shown), mouse);

        if(shown.GetRows().isEmpty()){
            y += PaintEmpty(g, y, width, shown, mouse);
        } else {
            y += PaintStatus(g, y, width, shown);
            y += PaintRows(g, y, width, shown, mouse);
            if(shown.HasMore()){
                y += shell.GetGap();
                List<NavEntry> more = new ArrayList<>();
                more.add(new NavEntry(moreKey, ListWords.showMore, shown.GetMatching() - shown.GetRows().size(), false, true, () -> Pick(query.WithMorePages())));
                y += strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, null, more, mouse, hits);
            }
        }
        return y + layout.GetList().GetBottomPadding();
    }

    private int PaintTitle(Graphics2D g, int top, int width){
        TextStyle caption = Theme.Current.GetHeading(), title = Theme.Current.GetTitle();
        FontMetrics captionMetrics = g.getFontMetrics(caption.GetFont()), titleMetrics = g.getFontMetrics(title.GetFont());
        int y = top;
        g.setFont(caption.GetFont());
        g.setColor(caption.GetColour());
        g.drawString(LibraryWords.subtitle, 0, y + captionMetrics.getAscent());
        y += captionMetrics.getHeight();
        g.setFont(title.GetFont());
        g.setColor(title.GetColour());
        g.drawString(LibraryWords.title, 0, y + titleMetrics.getAscent());
        y += titleMetrics.getHeight();
        g.setFont(caption.GetFont());
        g.setColor(caption.GetColour());
        g.drawString(TextFit.Fit(captionMetrics, LibraryWords.description, width), 0, y + captionMetrics.getAscent());
        return y + captionMetrics.getHeight() - top;
    }

    // The search box, with the sort and direction buttons beside it
    private int PaintSearchLine(Graphics2D g, int y, int width, Point mouse){
        ShellLayout shell = layout.GetShell();
        int lineHeight = shell.GetStateRowHeight();
        Rectangle box = new Rectangle(0, y, Math.min(width, layout.GetList().GetSearchWidth()), lineHeight);
        searchField.Paint(g, box, typed, searchFocused, LibraryWords.searchPlaceholder);
        hits.Add(searchKey, box, () -> searchFocused = true);
        List<NavEntry> orders = new ArrayList<>();
        orders.add(new NavEntry(sortKey, LibraryWords.SortButton(query), null, false, true, () -> Pick(query.WithNextSort())));
        orders.add(new NavEntry(directionKey, LibraryWords.DirectionButton(query), null, false, true, () -> Pick(query.WithSort(query.GetSort()))));
        strip.Paint(g, box.x + box.width + shell.GetGap(), y, lineHeight, Math.max(0, width - box.width - shell.GetGap()), null, orders, mouse, hits);
        return lineHeight;
    }

    private List<NavEntry> StateEntries(LibraryResult shown){
        List<NavEntry> entries = new ArrayList<>();
        GameState[] states = GameState.values();
        for(int i = 0; i < states.length; i++){
            GameState state = states[i];
            ListChip chip = shown.GetStateChips().get(i);
            entries.add(new NavEntry(StateKey(state), chip.GetLabel(), chip.GetCount(), chip.IsOn(), true, () -> Pick(query.WithState(state, !chip.IsOn()))));
        }
        return entries;
    }

    private List<NavEntry> ReviewEntries(LibraryResult shown){
        List<NavEntry> entries = new ArrayList<>();
        for(ListChip chip : shown.GetReviewChips()){
            ReviewState review = ReviewState.valueOf(chip.GetKey().substring(LibraryBuilder.reviewKeyPrefix.length()));
            entries.add(new NavEntry(ReviewKey(review), chip.GetLabel(), chip.GetCount(), chip.IsOn(), true, () -> Pick(query.WithReviewState(review, !chip.IsOn()))));
        }
        ListChip tag = shown.GetMoreAchievementsChip();
        if(tag != null){
            entries.add(new NavEntry(tagKey, tag.GetLabel(), tag.GetCount(), tag.IsOn(), true, () -> Pick(query.WithMoreAchievements(!tag.IsOn()))));
        }
        return entries;
    }

    // The buttons for the other filters. Each says how many are picked, and is lit when any are
    private List<NavEntry> MoreFilterEntries(LibraryResult shown){
        List<NavEntry> entries = new ArrayList<>();
        int seriesPicked = query.GetExtras().GetSeriesIds().size(), genresPicked = query.GetExtras().GetGenres().size(), platformsPicked = query.GetExtras().GetPlatforms().size();
        entries.add(new NavEntry(seriesFilterKey, LibraryWords.seriesFilter, seriesPicked == 0 ? null : seriesPicked, seriesPicked > 0, true,
                () -> popups.ShowChoices(LibraryWords.seriesFilter, shown.GetSeriesOptions(), id -> Pick(query.WithSeries(id, !query.GetExtras().GetSeriesIds().contains(id))))));
        entries.add(new NavEntry(genreFilterKey, LibraryWords.genreFilter, genresPicked == 0 ? null : genresPicked, genresPicked > 0, true,
                () -> popups.ShowChoices(LibraryWords.genreFilter, shown.GetGenreOptions(), genre -> Pick(query.WithGenre(genre, !query.GetExtras().GetGenres().contains(genre))))));
        entries.add(new NavEntry(platformFilterKey, LibraryWords.platformFilter, platformsPicked == 0 ? null : platformsPicked, platformsPicked > 0, true,
                () -> popups.ShowChoices(LibraryWords.platformFilter, shown.GetPlatformOptions(), platform -> Pick(query.WithPlatform(platform, !query.GetExtras().GetPlatforms().contains(platform))))));
        entries.add(new NavEntry(completedFilterKey, LibraryWords.completedFilter, null, query.GetExtras().GetCompleted().IsSet(), true,
                () -> popups.ShowRanges(LibraryWords.completedFilter, range -> Pick(query.WithCompleted(range)))));
        entries.add(new NavEntry(addedFilterKey, LibraryWords.addedFilter, null, query.GetExtras().GetAdded().IsSet(), true,
                () -> popups.ShowRanges(LibraryWords.addedFilter, range -> Pick(query.WithAdded(range)))));
        return entries;
    }

    // Each filter that is on, as a chip that removes itself when clicked, and Clear all
    private List<NavEntry> FilterEntries(LibraryResult shown){
        List<NavEntry> entries = new ArrayList<>();
        for(LibraryResult.ActiveFilter filter : shown.GetActiveFilters()){
            entries.add(new NavEntry(FilterKey(filter.GetKey()), filter.GetLabel(), null, true, true, () -> Remove(filter.GetKey())));
        }
        if(!entries.isEmpty()){
            entries.add(new NavEntry(clearAllKey, LibraryWords.clearAll, null, false, true, this::ClearAll));
        }
        return entries;
    }

    private int PaintChips(Graphics2D g, int y, int width, String caption, List<NavEntry> entries, Point mouse){
        if(entries.isEmpty()){
            return 0;
        }
        ShellLayout shell = layout.GetShell();
        return strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, caption, entries, mouse, hits) + shell.GetGap();
    }

    private int PaintStatus(Graphics2D g, int top, int width, LibraryResult shown){
        TextStyle style = Theme.Current.GetHeading();
        FontMetrics metrics = g.getFontMetrics(style.GetFont());
        g.setFont(style.GetFont());
        g.setColor(style.GetColour());
        g.drawString(TextFit.Fit(metrics, LibraryWords.Status(shown, query), width), 0, top + metrics.getAscent());
        return metrics.getHeight() + layout.GetShell().GetGap();
    }

    private int PaintRows(Graphics2D g, int top, int width, LibraryResult shown, Point mouse){
        ShellLayout shell = layout.GetShell();
        ListLayout list = layout.GetList();
        int pad = layout.GetOverview().GetCardPadding();
        int y = top;
        List<GameRow> rows = shown.GetRows();
        for(int i = 0; i < rows.size(); i++){
            GameRow row = rows.get(i);
            Rectangle box = new Rectangle(0, y, width, list.GetRowHeight());
            List<NavEntry> buttons = RowButtons(i, row.GetGame());
            int buttonsWidth = buttons.isEmpty() ? 0 : strip.Width(g, null, buttons);
            rowPainter.Paint(g, box, row, box.contains(mouse), buttons.isEmpty() ? 0 : buttonsWidth + pad);
            strip.Paint(g, box.x + box.width - pad - buttonsWidth, y + LayoutMath.Centre(box.height, shell.GetStateRowHeight()), shell.GetStateRowHeight(), buttonsWidth, null, buttons, mouse, hits);
            if(row.CanOpen()){
                hits.Add(RowKey(i), box, () -> onOpen.accept(row.GetGame()));
            }
            y += list.GetRowHeight() + list.GetRowGap();
        }
        return y - list.GetRowGap() - top;
    }

    private List<NavEntry> RowButtons(int index, LibraryGame game){
        List<NavEntry> entries = new ArrayList<>();
        for(RowAction action : RowActions.Buttons(game)){
            entries.add(new NavEntry(ActionKey(index, action), action.GetLabel(), null, false, true, () -> onAction.accept(action, game)));
        }
        List<RowAction> menu = RowActions.MenuItems(game);
        if(!menu.isEmpty()){
            entries.add(new NavEntry(MenuKey(index), ListWords.menuButton, null, false, true, () -> onMenu.accept(game, menu)));
        }
        return entries;
    }

    private int PaintEmpty(Graphics2D g, int top, int width, LibraryResult shown, Point mouse){
        ShellLayout shell = layout.GetShell();
        boolean nothing = shown.GetEmptiness() == LibraryResult.Emptiness.NOTHING_IN_LIBRARY;
        FontMetrics titleMetrics = g.getFontMetrics(Theme.Current.GetTitle().GetFont()), detailMetrics = g.getFontMetrics(Theme.Current.GetHeading().GetFont());
        int y = top + shell.GetGap();
        g.setFont(Theme.Current.GetTitle().GetFont());
        g.setColor(Theme.Current.GetTitle().GetColour());
        g.drawString(TextFit.Fit(titleMetrics, nothing ? LibraryWords.title : LibraryWords.noMatchTitle, width), 0, y + titleMetrics.getAscent());
        y += titleMetrics.getHeight();
        g.setFont(Theme.Current.GetHeading().GetFont());
        g.setColor(Theme.Current.GetHeading().GetColour());
        g.drawString(TextFit.Fit(detailMetrics, nothing ? LibraryWords.emptyLibrary : LibraryWords.noMatchHint, width), 0, y + detailMetrics.getAscent());
        y += detailMetrics.getHeight() + shell.GetGap();
        if(!nothing){
            List<NavEntry> clear = new ArrayList<>();
            clear.add(new NavEntry(clearFiltersKey, LibraryWords.clearAllFilters, null, false, true, this::ClearAll));
            y += strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, null, clear, mouse, hits);
        }
        return y - top;
    }

    // ------------------------------------------------------------------ what is picked

    private void Pick(LibraryQuery next){
        query = next;
        result = null;
    }

    private void ClearAll(){
        typed = "";
        Pick(query.Cleared());
    }

    // Takes off one of the filters that are on
    private void Remove(String filterKey){
        if(filterKey.equals(LibraryBuilder.stateKeyPrefix)){
            Pick(query.WithAllStates());
        } else if(filterKey.equals(LibraryBuilder.moreAchievementsKey)){
            Pick(query.WithMoreAchievements(false));
        } else if(filterKey.equals(LibraryBuilder.seriesKey)){
            Pick(query.WithoutSeries());
        } else if(filterKey.equals(LibraryBuilder.genreKey)){
            Pick(query.WithoutGenres());
        } else if(filterKey.equals(LibraryBuilder.platformKey)){
            Pick(query.WithoutPlatforms());
        } else if(filterKey.equals(LibraryBuilder.completedKey)){
            Pick(query.WithCompleted(DateRange.None()));
        } else if(filterKey.equals(LibraryBuilder.addedKey)){
            Pick(query.WithAdded(DateRange.None()));
        } else if(filterKey.equals(LibraryBuilder.searchKey)){
            typed = "";
            Pick(query.WithSearch(""));
        } else if(filterKey.startsWith(LibraryBuilder.reviewKeyPrefix)){
            Pick(query.WithReviewState(ReviewState.valueOf(filterKey.substring(LibraryBuilder.reviewKeyPrefix.length())), false));
        }
    }

    private LibraryResult Result(){
        if(result == null){
            result = LibraryBuilder.Build(games, series, query, layout.GetList().GetPageSize());
        }
        return result;
    }

    // ------------------------------------------------------------------ scrolling and clicks

    public void Scroll(int wheelRotation){
        scroll = ClampScroll(scroll + wheelRotation * layout.GetList().GetScrollStep());
    }

    private int ClampScroll(int value){
        return Math.max(0, Math.min(value, Math.max(0, contentHeight - viewportHeight)));
    }

    public void Press(Point point){
        pressedKey = hits.KeyAt(point);
    }

    // True if that completed a click, in which case the screen has changed and needs painting again
    public boolean Release(Point point){
        String key = hits.KeyAt(point);
        boolean clicked = key != null && key.equals(pressedKey);
        pressedKey = null;
        if(!searchKey.equals(key)){
            searchFocused = false;
        }
        return clicked && hits.Run(key);
    }

    // Where a button was last drawn, or null
    public Rectangle BoundsOf(String key){
        return hits.BoundsOf(key);
    }
}
