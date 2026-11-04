package Controller.BackEnd.RichEdit;

import Controller.BackEnd.Emoji.EmojiNames;
import Controller.BackEnd.RichText.RunStyle;

// What typing at the caret would do: the style the next character takes, and the part of an emoji name being typed
final class Typing {

    private Typing(){
    }

    // The style chosen with no selection (bold switched on, say), else the text's own: the cell before the caret, or after it at the start
    // of a block. A link goes on only when the cells either side are in the same link. With a selection, the first character selected
    static RunStyle StyleAt(EditState state){
        if(state.GetPending() != null){
            return state.GetPending();
        }
        int from = state.SelectionStart();
        Cell after = state.CellAfter(from);
        Cell before = state.HasSelection() ? null : state.CellBefore(from);
        if(before != null && !before.IsBreak()){
            String link = before.GetStyle().GetLink();
            boolean inside = after != null && link != null && link.equals(after.GetStyle().GetLink());
            return inside ? before.GetStyle() : before.GetStyle().WithLink(null);
        }
        if(after != null && !after.IsBreak()){
            return after.GetStyle().WithLink(null);
        }
        return RunStyle.plain;
    }

    // The letters typed after a ":" that starts a word, up to the caret ("sh" in "it deserves a :sh"), or null when no name is being
    // typed. The caret must be after at least one letter
    static String EmojiPrefix(EditState state){
        if(state.HasSelection()){
            return null;
        }
        int index = state.BlockIndexAt(state.GetCaret());
        int at = state.GetCaret() - state.BlockStart(index);
        java.util.List<Cell> cells = state.BlockAt(index).GetCells();
        StringBuilder name = new StringBuilder();
        int i = at - 1;
        while(i >= 0 && cells.get(i).IsChar() && cells.get(i).GetCodePoint() < Character.MAX_VALUE && EmojiNames.IsNameChar((char) cells.get(i).GetCodePoint())){
            name.insert(0, (char) cells.get(i).GetCodePoint());
            i--;
        }
        boolean startsWord = i >= 0 && cells.get(i).Is(':') && (i == 0 || !cells.get(i - 1).IsWordChar());
        return startsWord && name.length() > 0 ? name.toString() : null;
    }
}
