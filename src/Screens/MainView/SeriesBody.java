package Screens.MainView;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import Controller.BackEnd.MainView.StateSelection;
import Controller.BackEnd.Overview.OverviewSeriesGroup;
import Controller.BackEnd.SeriesTab.SeriesSort;
import Controller.BackEnd.SeriesTab.SeriesTabBuilder;
import Controller.BackEnd.SeriesTab.SeriesTabWords;
import Model.Layout.Layout;
import Model.Layout.ListLayout;
import Model.Layout.ShellLayout;
import Model.Library.LibraryGame;
import Model.Reviews.Series;
import Model.Themes.TextStyle;
import Model.Themes.Theme;

// The Series tab: how many series, buttons to fold or open them all, the order and New series, then each series as a foldable group
// with an "Open series" button. What is folded and the order are kept here; working out the groups is the backend's (SeriesTabBuilder)
public class SeriesBody implements BodyPainter {
    public static final String collapseAllKey = "series:collapseall";
    public static final String expandAllKey = "series:expandall";
    public static final String sortKey = "series:sort";
    public static final String newKey = "series:new";

    private final Layout layout;
    private final NavStrip strip;
    private final SeriesGroupPainter groupPainter;
    private final Consumer<LibraryGame> openGame;
    private final SeriesActions actions;
    private final Set<String> collapsed = new HashSet<>();
    private List<LibraryGame> games;
    private List<Series> series;
    private StateSelection selection = StateSelection.All();
    private SeriesSort sort = SeriesSort.NAME;
    private List<OverviewSeriesGroup> groups;   // Null when they have to be worked out again

    public SeriesBody(List<LibraryGame> games, List<Series> series, Layout layout, Consumer<LibraryGame> openGame, SeriesActions actions){
        this.games = games;
        this.series = series;
        this.layout = layout;
        this.strip = new NavStrip(layout.GetShell());
        this.groupPainter = new SeriesGroupPainter(layout, new GameRowPainter(layout.GetOverview()));
        this.openGame = openGame;
        this.actions = actions;
    }

    // Newer data: what was folded stays folded
    public void Reload(List<LibraryGame> freshGames, List<Series> freshSeries){
        this.games = freshGames;
        this.series = freshSeries;
        groups = null;
    }

    // Which games the tab is for (the State row's choice)
    public void Show(StateSelection next){
        if(!next.equals(selection)){
            groups = null;
        }
        selection = next;
    }

    @Override
    public int Paint(Graphics2D g, Rectangle viewport, Point mouse, HitMap hits){
        ShellLayout shell = layout.GetShell();
        ListLayout list = layout.GetList();
        List<OverviewSeriesGroup> shown = Groups();
        int width = viewport.width;
        int y = 0;

        TextStyle style = Theme.Current.GetHeading();
        FontMetrics metrics = g.getFontMetrics(style.GetFont());
        g.setFont(style.GetFont());
        g.setColor(style.GetColour());
        g.drawString(TextFit.Fit(metrics, SeriesTabWords.Summary(shown.size()), width), 0, y + metrics.getAscent());
        y += metrics.getHeight() + shell.GetGap();

        y += strip.Paint(g, 0, y, shell.GetStateRowHeight(), width, null, Buttons(shown), mouse, hits) + shell.GetGap();

        if(shown.isEmpty()){
            y += PaintEmpty(g, y, width);
        } else {
            for(OverviewSeriesGroup group : shown){
                String id = group.GetSeries().GetId();
                y += groupPainter.Paint(g, 0, y, width, group, collapsed.contains(id), mouse, hits, () -> Toggle(id), openGame, SeriesTabWords.openSeries, () -> actions.OpenSeries(id)) + list.GetRowGap();
            }
            y -= list.GetRowGap();
        }
        return Math.max(viewport.height, y + list.GetBottomPadding());
    }

    private List<NavEntry> Buttons(List<OverviewSeriesGroup> shown){
        List<NavEntry> buttons = new ArrayList<>();
        buttons.add(new NavEntry(collapseAllKey, SeriesTabWords.collapseAll, null, false, true, () -> {
            for(OverviewSeriesGroup group : shown){
                collapsed.add(group.GetSeries().GetId());
            }
        }));
        buttons.add(new NavEntry(expandAllKey, SeriesTabWords.expandAll, null, false, true, collapsed::clear));
        buttons.add(new NavEntry(sortKey, SeriesTabWords.SortButton(sort), null, false, true, () -> {
            sort = sort.Next();
            groups = null;
        }));
        buttons.add(new NavEntry(newKey, SeriesTabWords.newSeries, null, false, true, actions::NewSeries));
        return buttons;
    }

    private int PaintEmpty(Graphics2D g, int top, int width){
        ListLayout list = layout.GetList();
        Rectangle box = new Rectangle(0, top, width, list.GetRowHeight());
        CardBox.Paint(g, box, false);
        FontMetrics metrics = g.getFontMetrics(Theme.Current.GetField().GetFont());
        g.setFont(Theme.Current.GetField().GetFont());
        g.setColor(Theme.Current.GetNav().GetDisabledText());
        g.drawString(TextFit.Fit(metrics, SeriesTabWords.Empty(selection), box.width - layout.GetOverview().GetCardPadding() - layout.GetOverview().GetCardPadding()),
                box.x + layout.GetOverview().GetCardPadding(), TextLine.Baseline(box.y, box.height, metrics));
        return box.height;
    }

    private void Toggle(String id){
        if(!collapsed.remove(id)){
            collapsed.add(id);
        }
    }

    private List<OverviewSeriesGroup> Groups(){
        if(groups == null){
            groups = SeriesTabBuilder.Build(games, series, selection, sort);
        }
        return groups;
    }
}
