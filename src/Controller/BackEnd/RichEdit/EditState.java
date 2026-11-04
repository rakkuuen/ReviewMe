package Controller.BackEnd.RichEdit;

import Controller.BackEnd.RichText.RunStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Everything about the text being edited at one moment: its blocks, the selection (anchor and caret) and the style the next typed
// character takes when that was chosen with no selection (null: it follows the text around the caret). A position is a place between
// cells, counted from the start of the text, where the step from the end of one block to the start of the next counts as one. So a text of
// blocks of 3 and 2 cells has positions 0 to 6, and 3 and 4 are the end of the first block and the start of the second. Immutable
public final class EditState {
    private final List<EditBlock> blocks;
    private final int anchor;
    private final int caret;
    private final RunStyle pending;

    public EditState(List<EditBlock> blocks, int anchor, int caret, RunStyle pending){
        List<EditBlock> kept = new ArrayList<>(blocks);
        if(kept.isEmpty()){
            kept.add(EditBlock.Paragraph(0, new ArrayList<>()));
        }
        this.blocks = Collections.unmodifiableList(kept);
        int last = LengthOf(kept);
        this.anchor = Math.max(0, Math.min(anchor, last));
        this.caret = Math.max(0, Math.min(caret, last));
        this.pending = pending;
    }

    private static int LengthOf(List<EditBlock> blocks){
        int total = blocks.size() - 1;
        for(EditBlock block : blocks){
            total += block.Length();
        }
        return total;
    }

    public List<EditBlock> GetBlocks(){ return blocks; }
    public int GetAnchor(){ return anchor; }
    public int GetCaret(){ return caret; }
    public RunStyle GetPending(){ return pending; }

    public int Length(){ return LengthOf(blocks); }
    public int BlockCount(){ return blocks.size(); }
    public EditBlock BlockAt(int index){ return blocks.get(index); }

    public boolean HasSelection(){ return anchor != caret; }
    public int SelectionStart(){ return Math.min(anchor, caret); }
    public int SelectionEnd(){ return Math.max(anchor, caret); }

    // The position where block i starts
    public int BlockStart(int index){
        int start = 0;
        for(int i = 0; i < index; i++){
            start += blocks.get(i).Length() + 1;
        }
        return start;
    }

    public int BlockEnd(int index){
        return BlockStart(index) + blocks.get(index).Length();
    }

    // Which block a position is in (the end of one block is not the start of the next: that is a step further on)
    public int BlockIndexAt(int position){
        int start = 0;
        for(int i = 0; i < blocks.size(); i++){
            int end = start + blocks.get(i).Length();
            if(position <= end){
                return i;
            }
            start = end + 1;
        }
        return blocks.size() - 1;
    }

    // The cell just after a position, or null at the end of a block
    public Cell CellAfter(int position){
        int index = BlockIndexAt(position);
        int at = position - BlockStart(index);
        return at < blocks.get(index).Length() ? blocks.get(index).GetCells().get(at) : null;
    }

    // The cell just before a position, or null at the start of a block
    public Cell CellBefore(int position){
        int index = BlockIndexAt(position);
        int at = position - BlockStart(index);
        return at > 0 ? blocks.get(index).GetCells().get(at - 1) : null;
    }

    public boolean IsEmpty(){
        return blocks.size() == 1 && blocks.get(0).IsEmpty();
    }

    public EditState With(List<EditBlock> changed, int newAnchor, int newCaret, RunStyle newPending){
        return new EditState(changed, newAnchor, newCaret, newPending);
    }

    public EditState WithSelection(int newAnchor, int newCaret){
        return new EditState(blocks, newAnchor, newCaret, null);
    }

    public EditState WithPending(RunStyle style){
        return new EditState(blocks, anchor, caret, style);
    }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof EditState)){
            return false;
        }
        EditState that = (EditState) other;
        return anchor == that.anchor && caret == that.caret && blocks.equals(that.blocks) && java.util.Objects.equals(pending, that.pending);
    }

    @Override
    public int hashCode(){
        return java.util.Objects.hash(blocks, anchor, caret, pending);
    }
}
