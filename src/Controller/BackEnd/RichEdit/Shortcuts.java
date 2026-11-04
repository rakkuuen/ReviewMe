package Controller.BackEnd.RichEdit;

import Controller.BackEnd.Emoji.EmojiNames;
import Controller.BackEnd.RichText.Block;
import Controller.BackEnd.RichText.RunStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

// Typing Markdown: when the character just typed completes **bold**, *italic*, a ":name:" of a known emoji, or a "- " / "1. " at the
// start of a paragraph, the marks are taken out and the text changes to match. Each looks at the text before the caret (in its block)
final class Shortcuts {
    private static final String bulletMarks = "-*+";
    private static final String numberEnds = ".)";
    private static final int longestNumber = 9;

    private Shortcuts(){
    }

    // The state after the shortcut the typed character completes, or null when it completes none. The caret is just after the character
    static EditState After(EditState state, int typed, Predicate<String> knownEmoji){
        int index = state.BlockIndexAt(state.GetCaret());
        int at = state.GetCaret() - state.BlockStart(index);
        List<Cell> cells = state.BlockAt(index).GetCells();
        if(typed == ' '){
            return List(state, index, cells, at);
        }
        if(typed == '*'){
            return Emphasis(state, index, cells, at);
        }
        return typed == ':' ? Emoji(state, index, cells, at, knownEmoji) : null;
    }

    // The block with its cells from `from` to `to` replaced, and the caret after them (and the style the next character takes)
    private static EditState Replaced(EditState state, int index, int from, int to, List<Cell> with, RunStyle next){
        List<Cell> cells = new ArrayList<>(state.BlockAt(index).GetCells().subList(0, from));
        cells.addAll(with);
        int caret = state.BlockStart(index) + cells.size();
        cells.addAll(state.BlockAt(index).GetCells().subList(to, state.BlockAt(index).Length()));
        List<EditBlock> blocks = new ArrayList<>(state.GetBlocks());
        blocks.set(index, state.BlockAt(index).WithCells(cells));
        return state.With(blocks, caret, caret, next);
    }

    // "- ", "* ", "+ " or "12. " at the start of a paragraph: the block becomes a list item and the marker goes
    private static EditState List(EditState state, int index, List<Cell> cells, int at){
        if(state.BlockAt(index).GetKind() != Block.Kind.PARAGRAPH){
            return null;
        }
        int marker = at - 1;
        Block.Kind kind = null;
        if(marker == 1 && cells.get(0).IsChar() && bulletMarks.indexOf(cells.get(0).GetCodePoint()) >= 0){
            kind = Block.Kind.BULLET;
        } else if(IsNumberMarker(cells, marker)){
            kind = Block.Kind.NUMBERED;
        }
        if(kind == null){
            return null;
        }
        int number = kind == Block.Kind.NUMBERED ? Integer.parseInt(Text(cells, 0, marker - 1)) : 0;
        List<EditBlock> blocks = new ArrayList<>(state.GetBlocks());
        EditBlock converted = state.BlockAt(index).WithCells(new ArrayList<>(cells.subList(at, cells.size()))).WithKind(kind, number);
        blocks.set(index, converted);
        int start = state.BlockStart(index);
        return state.With(blocks, start, start, null);
    }

    private static boolean IsNumberMarker(List<Cell> cells, int marker){
        if(marker < 2 || marker > longestNumber + 1 || !cells.get(marker - 1).IsChar() || numberEnds.indexOf(cells.get(marker - 1).GetCodePoint()) < 0){
            return false;
        }
        for(int i = 0; i < marker - 1; i++){
            if(!cells.get(i).IsChar() || !Character.isDigit(cells.get(i).GetCodePoint()) || cells.get(i).GetCodePoint() > '9'){
                return false;
            }
        }
        return true;
    }

    private static String Text(List<Cell> cells, int from, int to){
        StringBuilder text = new StringBuilder();
        for(int i = from; i < to; i++){
            text.append(cells.get(i).Text());
        }
        return text.toString();
    }

    // The star just typed closes **bold** (the cell before it is a star too) or *italic*
    private static EditState Emphasis(EditState state, int index, List<Cell> cells, int at){
        boolean bold = at >= 2 && cells.get(at - 2).Is('*');
        int closeLength = bold ? 2 : 1;
        int close = at - closeLength;
        int open = LastStar(cells, close);
        int openLength = bold ? 2 : 1;
        int from = open - openLength + 1;
        if(open < 0 || from < 0 || (bold && !cells.get(open - 1).Is('*')) || (!bold && open > 0 && cells.get(open - 1).Is('*'))){
            return null;
        }
        if(bold && from > 0 && cells.get(from - 1).Is('*')){
            return null;
        }
        List<Cell> inner = cells.subList(open + 1, close);
        if(inner.isEmpty() || inner.get(0).IsSpace() || inner.get(inner.size() - 1).IsSpace()){
            return null;
        }
        List<Cell> styled = new ArrayList<>();
        for(Cell cell : inner){
            styled.add(cell.WithStyle(bold ? cell.GetStyle().WithBold(true) : cell.GetStyle().WithItalic(true)));
        }
        return Replaced(state, index, from, at, styled, inner.get(inner.size() - 1).GetStyle());
    }

    // The last star before a position in a block, or -1
    private static int LastStar(List<Cell> cells, int before){
        for(int i = before - 1; i >= 0; i--){
            if(cells.get(i).Is('*')){
                return i;
            }
        }
        return -1;
    }

    // ":name:" of a known emoji, the colon before it at the start of a word
    private static EditState Emoji(EditState state, int index, List<Cell> cells, int at, Predicate<String> knownEmoji){
        int open = at - 2;
        StringBuilder name = new StringBuilder();
        while(open >= 0 && cells.get(open).IsChar() && cells.get(open).GetCodePoint() < Character.MAX_VALUE && EmojiNames.IsNameChar((char) cells.get(open).GetCodePoint())){
            name.insert(0, (char) cells.get(open).GetCodePoint());
            open--;
        }
        if(open < 0 || !cells.get(open).Is(':') || !knownEmoji.test(name.toString()) || (open > 0 && cells.get(open - 1).IsWordChar())){
            return null;
        }
        RunStyle style = cells.get(open).GetStyle();
        List<Cell> emoji = new ArrayList<>();
        emoji.add(Cell.Emoji(name.toString(), style));
        return Replaced(state, index, open, at, emoji, null);
    }
}
