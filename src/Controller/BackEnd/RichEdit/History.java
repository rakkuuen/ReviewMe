package Controller.BackEnd.RichEdit;

import java.util.ArrayDeque;
import java.util.Deque;

// Undo and redo for the text being edited. Each edit saves the state it started from; several of the same sort in a row (typing,
// backspacing) save only the first, so one undo takes back the lot. Moving the caret or changing sort starts a new one
final class History {
    private static final int deepest = 200;

    private final Deque<EditState> undo = new ArrayDeque<>();
    private final Deque<EditState> redo = new ArrayDeque<>();
    private String lastKind;

    // About to change `before`. kind groups edits that undo together (null: never grouped)
    void Record(String kind, EditState before){
        if(kind == null || !kind.equals(lastKind)){
            undo.addLast(before);
            if(undo.size() > deepest){
                undo.removeFirst();
            }
        }
        lastKind = kind;
        redo.clear();
    }

    // The next edit starts a new step
    void Break(){
        lastKind = null;
    }

    boolean CanUndo(){ return !undo.isEmpty(); }
    boolean CanRedo(){ return !redo.isEmpty(); }

    // The state before the last step (current is kept for Redo), or null if there is none
    EditState Undo(EditState current){
        if(undo.isEmpty()){
            return null;
        }
        redo.addLast(current);
        lastKind = null;
        return undo.removeLast();
    }

    EditState Redo(EditState current){
        if(redo.isEmpty()){
            return null;
        }
        undo.addLast(current);
        lastKind = null;
        return redo.removeLast();
    }
}
