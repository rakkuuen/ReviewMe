package Controller.BackEnd.RichEdit;

import Controller.BackEnd.RichText.RunStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

// Changing and reading the style of text: bold, italic, colour, font and links
final class StyleEdits {

    // A link in the text: the cells from start up to end all point to the address
    static final class LinkSpan {
        private final int start, end;
        private final String address;

        LinkSpan(int start, int end, String address){
            this.start = start;
            this.end = end;
            this.address = address;
        }

        int Start(){ return start; }
        int End(){ return end; }
        String Address(){ return address; }
    }

    private StyleEdits(){
    }

    // Gives every cell between two positions the style the function makes of its own
    static EditState Apply(EditState state, int from, int to, UnaryOperator<RunStyle> change){
        List<EditBlock> blocks = new ArrayList<>(state.GetBlocks());
        for(int i = state.BlockIndexAt(from); i <= state.BlockIndexAt(to); i++){
            int start = state.BlockStart(i);
            List<Cell> cells = new ArrayList<>(blocks.get(i).GetCells());
            for(int k = Math.max(0, from - start); k < cells.size() && start + k < to; k++){
                cells.set(k, cells.get(k).WithStyle(change.apply(cells.get(k).GetStyle())));
            }
            blocks.set(i, blocks.get(i).WithCells(cells));
        }
        return state.With(blocks, state.GetAnchor(), state.GetCaret(), state.GetPending());
    }

    // The cells that show a style between two positions: all but spaces (a space can't show bold or a colour), unless it is all spaces
    static List<Cell> Showing(EditState state, int from, int to){
        List<Cell> all = new ArrayList<>();
        List<Cell> spaces = new ArrayList<>();
        for(int i = state.BlockIndexAt(from); i <= state.BlockIndexAt(to); i++){
            int start = state.BlockStart(i);
            List<Cell> cells = state.BlockAt(i).GetCells();
            for(int k = Math.max(0, from - start); k < cells.size() && start + k < to; k++){
                Cell cell = cells.get(k);
                if(cell.IsBreak()){
                    continue;
                }
                (cell.IsSpace() ? spaces : all).add(cell);
            }
        }
        return all.isEmpty() ? spaces : all;
    }

    // On if every cell has the property, mixed if some do, off if none
    static Choice Flag(List<Cell> cells, Function<RunStyle, Boolean> has){
        int count = 0;
        for(Cell cell : cells){
            count += has.apply(cell.GetStyle()) ? 1 : 0;
        }
        if(count == 0){
            return Choice.Off();
        }
        return count == cells.size() ? Choice.On() : Choice.Mixed();
    }

    // The value every cell has (null counts as a value: the default), or mixed when they differ
    static Choice Value(List<Cell> cells, Function<RunStyle, String> get){
        String first = cells.isEmpty() ? null : get.apply(cells.get(0).GetStyle());
        for(Cell cell : cells){
            if(!Objects.equals(first, get.apply(cell.GetStyle()))){
                return Choice.Mixed();
            }
        }
        return Choice.Value(first);
    }

    // The link a position is inside or touching the end of (its whole run of cells), or null
    static LinkSpan LinkAt(EditState state, int position){
        Cell cell = state.CellAfter(position) != null && state.CellAfter(position).GetStyle().GetLink() != null ? state.CellAfter(position) : null;
        int inside = position;
        if(cell == null){
            Cell before = state.CellBefore(position);
            if(before == null || before.GetStyle().GetLink() == null){
                return null;
            }
            cell = before;
            inside = position - 1;
        }
        String address = cell.GetStyle().GetLink();
        int start = inside;
        while(start > BlockStartOf(state, inside) && address.equals(LinkOf(state.CellBefore(start)))){
            start--;
        }
        int end = inside + 1;
        while(end < state.BlockEnd(state.BlockIndexAt(inside)) && address.equals(LinkOf(state.CellAfter(end)))){
            end++;
        }
        return new LinkSpan(start, end, address);
    }

    private static int BlockStartOf(EditState state, int position){
        return state.BlockStart(state.BlockIndexAt(position));
    }

    private static String LinkOf(Cell cell){
        return cell == null ? null : cell.GetStyle().GetLink();
    }
}
