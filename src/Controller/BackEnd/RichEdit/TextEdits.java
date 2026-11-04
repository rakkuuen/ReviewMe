package Controller.BackEnd.RichEdit;

import Controller.BackEnd.RichText.Block;
import java.util.ArrayList;
import java.util.List;

// The edits that add, remove and split text. Each takes a state and gives the changed one; none changes the state it is given
final class TextEdits {
    static final int paragraphGap = 2;   // A blank line between paragraphs
    static final int itemGap = 1;        // List items follow straight on

    private TextEdits(){
    }

    // Removes what lies between two positions; the block the range starts in and the one it ends in become one. The caret is left at from
    static EditState Delete(EditState state, int from, int to){
        if(from >= to){
            return state;
        }
        int first = state.BlockIndexAt(from);
        int last = state.BlockIndexAt(to);
        List<Cell> joined = new ArrayList<>(Before(state, first, from));
        joined.addAll(After(state, last, to));
        List<EditBlock> blocks = new ArrayList<>(state.GetBlocks().subList(0, first));
        blocks.add(state.BlockAt(first).WithCells(joined));
        blocks.addAll(state.GetBlocks().subList(last + 1, state.BlockCount()));
        return state.With(blocks, from, from, null);
    }

    // The cells of a block before a position inside it, and after it
    private static List<Cell> Before(EditState state, int block, int position){
        return state.BlockAt(block).GetCells().subList(0, position - state.BlockStart(block));
    }

    private static List<Cell> After(EditState state, int block, int position){
        return state.BlockAt(block).GetCells().subList(position - state.BlockStart(block), state.BlockAt(block).Length());
    }

    // Puts cells in at the caret (a selection must have been removed first). The caret ends up after them
    static EditState Insert(EditState state, List<Cell> cells){
        int caret = state.GetCaret();
        int index = state.BlockIndexAt(caret);
        List<Cell> joined = new ArrayList<>(Before(state, index, caret));
        joined.addAll(cells);
        joined.addAll(After(state, index, caret));
        List<EditBlock> blocks = new ArrayList<>(state.GetBlocks());
        blocks.set(index, state.BlockAt(index).WithCells(joined));
        int end = caret + cells.size();
        return state.With(blocks, end, end, null);
    }

    // Ends the block at the caret and starts another with what followed it, of the same kind (a numbered item's number is left for
    // the caller to renumber). The caret goes to the start of the new one
    static EditState Split(EditState state){
        int caret = state.GetCaret();
        int index = state.BlockIndexAt(caret);
        EditBlock block = state.BlockAt(index);
        EditBlock left = block.WithCells(new ArrayList<>(Before(state, index, caret)));
        int gap = block.IsListItem() ? itemGap : paragraphGap;
        EditBlock right = new EditBlock(block.GetKind(), block.GetNumber(), gap, new ArrayList<>(After(state, index, caret)));
        List<EditBlock> blocks = new ArrayList<>(state.GetBlocks());
        blocks.set(index, left);
        blocks.add(index + 1, right);
        return state.With(blocks, caret + 1, caret + 1, null);
    }

    // Puts blocks (pasted text) in at the caret: the first joins the text before it, the last joins the text after it. Nothing in the
    // blocks is changed. A pasted list item takes over an empty block it lands in
    static EditState InsertBlocks(EditState state, List<EditBlock> pasted){
        if(pasted.isEmpty()){
            return state;
        }
        int caret = state.GetCaret();
        int index = state.BlockIndexAt(caret);
        EditBlock target = state.BlockAt(index);
        EditBlock first = pasted.get(0);
        EditBlock head = target.IsEmpty() ? new EditBlock(first.GetKind(), first.GetNumber(), target.GetNewlinesBefore(), new ArrayList<>()) : target;
        List<Cell> before = new ArrayList<>(Before(state, index, caret));
        List<Cell> after = new ArrayList<>(After(state, index, caret));
        before.addAll(first.GetCells());
        List<EditBlock> blocks = new ArrayList<>(state.GetBlocks().subList(0, index));
        if(pasted.size() == 1){
            before.addAll(after);
            blocks.add(head.WithCells(before));
            blocks.addAll(state.GetBlocks().subList(index + 1, state.BlockCount()));
            int end = caret + first.Length();
            return state.With(blocks, end, end, null);
        }
        blocks.add(head.WithCells(before));
        for(int i = 1; i < pasted.size() - 1; i++){
            blocks.add(pasted.get(i).WithNewlinesBefore(Math.max(itemGap, pasted.get(i).GetNewlinesBefore())));
        }
        EditBlock tail = pasted.get(pasted.size() - 1);
        List<Cell> tailCells = new ArrayList<>(tail.GetCells());
        tailCells.addAll(after);
        blocks.add(tail.WithNewlinesBefore(Math.max(itemGap, tail.GetNewlinesBefore())).WithCells(tailCells));
        blocks.addAll(state.GetBlocks().subList(index + 1, state.BlockCount()));
        EditState built = state.With(blocks, 0, 0, null);
        int end = built.BlockStart(index + pasted.size() - 1) + tail.Length();
        return state.With(blocks, end, end, null);
    }

    // The part of the text between two positions, as blocks. The first keeps its kind only if the range starts at its start
    static List<EditBlock> Slice(EditState state, int from, int to){
        List<EditBlock> out = new ArrayList<>();
        int first = state.BlockIndexAt(from);
        int last = state.BlockIndexAt(to);
        for(int i = first; i <= last; i++){
            EditBlock block = state.BlockAt(i);
            int start = state.BlockStart(i);
            int cut = i == first ? Math.max(0, from - start) : 0;
            int end = i == last ? to - start : block.Length();
            EditBlock piece = block.WithCells(new ArrayList<>(block.GetCells().subList(cut, end)));
            if(i == first){
                piece = (cut > 0 ? piece.WithKind(Block.Kind.PARAGRAPH, 0) : piece).WithNewlinesBefore(0);
            }
            out.add(piece);
        }
        return out;
    }
}
