package Controller;

import java.awt.Component;
import java.awt.Point;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import Controller.BackEnd.Library.UnreviewedGameManager;
import Controller.BackEnd.Library.UnreviewedGameResult;
import Controller.BackEnd.ListTab.RowAction;
import Model.Library.LibraryGame;

// Does what the buttons on a list row ask for: asks for what's needed in a small dialog, has the backend do it, and says if it was
// refused. onChanged runs after anything was saved, so the screen can read the data again
class RowActionHandler {
    private static final String shelveTitle = "Shelve";
    private static final String completeTitle = "Mark completed";

    private final Component parent;
    private final Runnable onChanged;
    private final Consumer<String> openGame;

    // openGame opens a game's page by its title (used once a review has been written for it)
    RowActionHandler(Component parent, Runnable onChanged, Consumer<String> openGame){
        this.parent = parent;
        this.onChanged = onChanged;
        this.openGame = openGame;
    }

    void Run(RowAction action, LibraryGame game){
        if(game.GetSteam() == null){
            return;
        }
        int appId = game.GetSteam().GetAppId();
        switch(action){
            case WRITE_REVIEW:
                NewReviewDialog.Show(parent, game, title -> {
                    onChanged.run();
                    openGame.accept(title);
                });
                break;
            case SHELVE:
                AskForReason(appId, game, "Why are you shelving " + game.GetTitle() + "? (optional)", "");
                break;
            case EDIT_REASON:
                String current = game.GetStatus().GetShelvedReason();
                AskForReason(appId, game, "Why is " + game.GetTitle() + " shelved? (optional)", current == null ? "" : current);
                break;
            case UNSHELVE:
                Finish(UnreviewedGameManager.Unshelve(appId));
                break;
            case MARK_COMPLETED:
                Object date = JOptionPane.showInputDialog(parent, game.GetTitle() + " has no achievements, so it can't tell when you finish it.\nCompleted on (optional, like 2025-03-02):",
                        completeTitle, JOptionPane.PLAIN_MESSAGE, null, null, "");
                if(date != null){
                    Finish(UnreviewedGameManager.MarkCompleted(appId, date.toString()));
                }
                break;
            default:
                break;
        }
    }

    // The menu of a row's "..." button, shown where the mouse is
    void ShowMenu(LibraryGame game, List<RowAction> items, Point at){
        JPopupMenu menu = new JPopupMenu();
        for(RowAction action : items){
            JMenuItem item = new JMenuItem(action.GetLabel());
            item.addActionListener(e -> Run(action, game));
            menu.add(item);
        }
        menu.show(parent, at.x, at.y);
    }

    private void AskForReason(int appId, LibraryGame game, String question, String start){
        Object reason = JOptionPane.showInputDialog(parent, question, shelveTitle, JOptionPane.PLAIN_MESSAGE, null, null, start);
        if(reason != null){
            Finish(UnreviewedGameManager.Shelve(appId, reason.toString()));
        }
    }

    private void Finish(UnreviewedGameResult result){
        if(result.Succeeded()){
            onChanged.run();
        } else {
            JOptionPane.showMessageDialog(parent, String.join("\n", result.GetErrors()), "ReviewMe", JOptionPane.WARNING_MESSAGE);
        }
    }
}
