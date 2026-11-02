package Screens.MainView;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import Controller.BackEnd.ListTab.ListBuilder;
import Controller.BackEnd.ListTab.ListChip;
import Controller.BackEnd.ListTab.ListQuery;
import Controller.BackEnd.ListTab.ListResult;
import Controller.BackEnd.ListTab.ListRules;
import Controller.BackEnd.ListTab.ListWords;
import Controller.BackEnd.ListTab.RowAction;
import Controller.BackEnd.ListTab.RowActions;
import Controller.BackEnd.MainView.MainTab;
import Controller.BackEnd.MainView.StateSelection;
import Controller.BackEnd.Overview.GameRow;
import Model.Layout.Layout;
import Model.Layout.LayoutMath;
import Model.Layout.ListLayout;
import Model.Layout.ShellLayout;
import Model.Library.LibraryGame;
import Model.Themes.TextStyle;
import Model.Themes.Theme;

// A tab that is a list of games (Completed, Incomplete, More achievements, or a state's Games): the toolbar (search, order, chips),
// how many are showing, the rows with their buttons, and Show more. What is picked is kept for each list, so coming back to a tab
// finds it as it was left. All the working-out is the backend's (ListBuilder); this keeps what is picked and draws the result
public class ListBody implements BodyPainter {
    public static final String searchKey = "list:search";
    public static final String sortKey = "list:sort";
    public static final String directionKey = "list:direction";
    public static final String moreKey = "list:more";
    public static final String clearKey = "list:clear";

    public static String RowKey(int index){ return "list:row:" + index; }
    public static String ActionKey(int index, RowAction action){ return "list:act:" + index + ":" + action.name(); }
    public static String MenuKey(int index){ return "list:menu:" + index; }
    public static String ChipKey(String chipKey){ return "list:chip:" + chipKey; }

    private final Layout layout;
    private final NavStrip strip;
    private final GameRowPainter rowPainter;
    private final SearchField searchField;
    private final Consumer<LibraryGame> onOpen;
    private final BiConsumer<RowAction, LibraryGame> onAction;
    private final BiConsumer<LibraryGame, List<RowAction>> onMenu;
    private final Map<String, ListQuery> queries = new HashMap<>();
    private final Map<String, String> typed = new HashMap<>();   // What is in each list's search box, spaces and all
    private List<LibraryGame> games;
    private MainTab tab = MainTab.COMPLETED;
    private StateSelection selection = StateSelection.All();
    private boolean searchFocused;
    private ListResult result;   // Null when it has to be worked out again

    // onOpen opens a game that has a review; onAction does a row's button; onMenu shows the menu for a row's "..." button
    public ListBody(List<LibraryGame> games, Layout layout, Consumer<LibraryGame> onOpen, BiConsumer<RowAction, LibraryGame> onAction, BiConsumer<LibraryGame, List<RowAction>> onMenu){
        this.games = games;
        this.layout = layout;
        this.strip = new NavStrip(layout.GetShell());
        this.rowPainter = new GameRowPainter(layout.GetOverview());
        this.searchField = new SearchField(layout.GetShell());
        this.onOpen = onOpen;
        this.onAction = onAction;
        this.onMenu = onMenu;
    }

    // Newer data: the lists are worked out again, with search and chips as they were
    public void Reload(List<LibraryGame> fresh){
        this.games = fresh;
        result = null;
    }

    // Which list this is for. Changing it drops the keyboard focus; each list keeps its own search, order and chips
    public void Show(MainTab nextTab, StateSelection nextSelection){
        if(nextTab != tab || !nextSelection.equals(selection)){
            searchFocused = false;
            result = null;
        }
        tab = nextTab;
        selection = nextSelection;
    }

    public boolean IsTyping(){ return searchFocused; }

    public void Blur(){ searchFocused = false; }

    // A letter typed while the search box has the keys (a backspace comes as '\b'). True if it changed the list
    public boolean Type(char letter){
        if(!searchFocused){
            return false;
        }
        String text = typed.getOrDefault(ContextKey(), "");
        if(letter == '\b'){
            if(text.isEmpty()){
                return false;
            }
            text = text.substring(0, text.length() - 1);
        } else if(Character.isISOControl(letter)){
            return false;
        } else {
            text = text + letter;
        }
        typed.put(ContextKey(), text);
        Pick(Query().WithSearch(text));
        return true;
    }

    @Override
    public int Paint(Graphics2D g, Rectangle viewport, Point mouse, HitMap hits){
        ShellLayout shell = layout.GetShell();
        ListLayout list = layout.GetList();
        ListQuery query = Query();
        ListResult shown = Result();
        int width = viewport.width;
        int y = 0;

        y += PaintToolbar(g, y, width, query, shown, mouse, hits);
        if(shown.GetRows().isEmpty()){
            y += PaintEmpty(g, y, width, query, shown, mouse, hits);
        } else {
            y += PaintStatus(g, y, width, query, shown);
            y += PaintRows(g, y, width, shown, mouse, hits);
            if(shown.HasMore()){
                y += shell.GetGap();
                List<NavEntry> more = new ArrayList<>();
                more.add(new NavEntry(moreKey, ListWords.showMore, shown.GetMatching() - shown.GetRows().size(), false, true, () -> Pick(Query().WithMorePages())));
                y += strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, null, more, mouse, hits);
            }
        }
        return Math.max(viewport.height, y + list.GetBottomPadding());
    }

    // Search and order on the first line (where the tab has them), then the chips, each group with its caption
    private int PaintToolbar(Graphics2D g, int top, int width, ListQuery query, ListResult shown, Point mouse, HitMap hits){
        ShellLayout shell = layout.GetShell();
        int y = top;
        int lineHeight = shell.GetStateRowHeight();
        List<NavEntry> orders = new ArrayList<>();
        if(!ListRules.SortsFor(tab).isEmpty()){
            orders.add(new NavEntry(sortKey, ListWords.SortButton(query), null, false, true, () -> Pick(Query().WithSort(ListRules.NextSort(tab, Query().GetSort())))));
            orders.add(new NavEntry(directionKey, ListWords.DirectionButton(query), null, false, true, () -> Pick(Query().WithSort(Query().GetSort()))));
        }
        Rectangle box = new Rectangle(0, y, Math.min(width, layout.GetList().GetSearchWidth()), lineHeight);
        searchField.Paint(g, box, typed.getOrDefault(ContextKey(), ""), searchFocused, ListWords.searchPlaceholder);
        hits.Add(searchKey, box, () -> searchFocused = true);
        if(!orders.isEmpty()){
            strip.Paint(g, box.x + box.width + shell.GetGap(), y, lineHeight, Math.max(0, width - box.width - shell.GetGap()), null, orders, mouse, hits);
        }
        y += lineHeight + shell.GetGap();

        y += PaintChips(g, y, width, ListWords.reviewCaption, shown.GetReviewChips(), mouse, hits);
        y += PaintChips(g, y, width, ListWords.reasonCaption, shown.GetReasonChips(), mouse, hits);
        return y - top;
    }

    private int PaintChips(Graphics2D g, int y, int width, String caption, List<ListChip> chips, Point mouse, HitMap hits){
        if(chips.isEmpty()){
            return 0;
        }
        ShellLayout shell = layout.GetShell();
        List<NavEntry> entries = new ArrayList<>();
        for(ListChip chip : chips){
            entries.add(new NavEntry(ChipKey(chip.GetKey()), chip.GetLabel(), chip.GetCount(), chip.IsOn(), true, () -> Pick(ToggleChip(chip))));
        }
        return strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, caption, entries, mouse, hits) + shell.GetGap();
    }

    private ListQuery ToggleChip(ListChip chip){
        ListQuery query = Query();
        if(ListBuilder.IsReasonKey(chip.GetKey())){
            return query.WithReason(chip.GetKey(), !chip.IsOn());
        }
        return query.WithReviewState(ListBuilder.ReviewOfKey(chip.GetKey()), !chip.IsOn());
    }

    private int PaintStatus(Graphics2D g, int top, int width, ListQuery query, ListResult shown){
        TextStyle style = Theme.Current.GetHeading();
        FontMetrics metrics = g.getFontMetrics(style.GetFont());
        g.setFont(style.GetFont());
        g.setColor(style.GetColour());
        g.drawString(TextFit.Fit(metrics, ListWords.Status(shown, query, tab), width), 0, top + metrics.getAscent());
        return metrics.getHeight() + layout.GetShell().GetGap();
    }

    private int PaintRows(Graphics2D g, int top, int width, ListResult shown, Point mouse, HitMap hits){
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
            // The buttons sit on the row, so their click areas go in first: where they overlap the row, they win
            strip.Paint(g, box.x + box.width - pad - buttonsWidth, y + LayoutMath.Centre(box.height, shell.GetStateRowHeight()), shell.GetStateRowHeight(), buttonsWidth, null, buttons, mouse, hits);
            if(row.CanOpen()){
                hits.Add(RowKey(i), box, () -> onOpen.accept(row.GetGame()));
            }
            y += list.GetRowHeight() + list.GetRowGap();
        }
        return y - list.GetRowGap() - top;
    }

    // The buttons a row shows at its right edge: what the game needs, and a "..." for its menu if it has one
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

    private int PaintEmpty(Graphics2D g, int top, int width, ListQuery query, ListResult shown, Point mouse, HitMap hits){
        ShellLayout shell = layout.GetShell();
        ListResult.Emptiness emptiness = shown.GetEmptiness();
        TextStyle detailStyle = Theme.Current.GetHeading();
        FontMetrics titleMetrics = g.getFontMetrics(Theme.Current.GetTitle().GetFont());
        FontMetrics detailMetrics = g.getFontMetrics(detailStyle.GetFont());
        int y = top + shell.GetGap();

        String heading = ListWords.EmptyTitle(emptiness, tab, selection);
        g.setFont(Theme.Current.GetTitle().GetFont());
        g.setColor(Theme.Current.GetTitle().GetColour());
        g.drawString(TextFit.Fit(titleMetrics, heading, width), 0, y + titleMetrics.getAscent());
        y += titleMetrics.getHeight();

        String detail = ListWords.EmptyDetail(emptiness, query);
        if(!detail.isEmpty()){
            g.setFont(detailStyle.GetFont());
            g.setColor(detailStyle.GetColour());
            g.drawString(TextFit.Fit(detailMetrics, detail, width), 0, y + detailMetrics.getAscent());
            y += detailMetrics.getHeight() + shell.GetGap();
            List<NavEntry> clear = new ArrayList<>();
            clear.add(new NavEntry(clearKey, ListWords.clearFilters, null, false, true, () -> {
                typed.remove(ContextKey());
                Pick(Query().Cleared());
            }));
            y += strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, null, clear, mouse, hits);
        }
        return y - top;
    }

    // ------------------------------------------------------------------ what is picked

    private String ContextKey(){
        return tab.name() + "/" + selection.GetStates();
    }

    private ListQuery Query(){
        return queries.computeIfAbsent(ContextKey(), key -> ListQuery.Initial(tab));
    }

    private void Pick(ListQuery next){
        queries.put(ContextKey(), next);
        result = null;
    }

    private ListResult Result(){
        if(result == null){
            result = ListBuilder.Build(games, selection, tab, Query(), layout.GetList().GetPageSize());
        }
        return result;
    }
}
