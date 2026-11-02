package Controller;

import java.awt.Component;
import java.awt.Point;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import Controller.BackEnd.LibraryView.DateRange;
import Controller.BackEnd.LibraryView.LibraryWords;
import Controller.BackEnd.ListTab.ListChip;
import Screens.Library.LibraryPopups;

// Shows the Library's filter choices as pop-up menus where the mouse is: ticks for series, genres and platforms, and the date ranges.
// afterwards runs after any choice, so the screen can paint again
class LibraryPopupMenus implements LibraryPopups {
    private final Component parent;
    private final Supplier<Point> mouse;
    private final Runnable afterwards;

    LibraryPopupMenus(Component parent, Supplier<Point> mouse, Runnable afterwards){
        this.parent = parent;
        this.mouse = mouse;
        this.afterwards = afterwards;
    }

    @Override
    public void ShowChoices(String title, List<ListChip> options, Consumer<String> onToggle){
        JPopupMenu menu = new JPopupMenu(title);
        for(ListChip option : options){
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(option.GetLabel() + " (" + option.GetCount() + ")", option.IsOn());
            item.addActionListener(e -> {
                onToggle.accept(option.GetKey());
                afterwards.run();
            });
            menu.add(item);
        }
        if(options.isEmpty()){
            JMenuItem none = new JMenuItem("Nothing to pick yet");
            none.setEnabled(false);
            menu.add(none);
        }
        menu.show(parent, mouse.get().x, mouse.get().y);
    }

    @Override
    public void ShowRanges(String title, Consumer<DateRange> onPick){
        LocalDate today = LocalDate.now();
        JPopupMenu menu = new JPopupMenu(title);
        Add(menu, LibraryWords.last30Days, DateRange.Last30Days(today), onPick);
        Add(menu, LibraryWords.thisYear, DateRange.ThisYear(today), onPick);
        Add(menu, LibraryWords.lastYear, DateRange.LastYear(today), onPick);
        Add(menu, LibraryWords.anyTime, DateRange.None(), onPick);
        menu.show(parent, mouse.get().x, mouse.get().y);
    }

    private void Add(JPopupMenu menu, String words, DateRange range, Consumer<DateRange> onPick){
        JMenuItem item = new JMenuItem(words);
        item.addActionListener(e -> {
            onPick.accept(range);
            afterwards.run();
        });
        menu.add(item);
    }
}
