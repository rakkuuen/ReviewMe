package Controller.BackEnd.RichEdit;

import Controller.BackEnd.RichText.Block;
import java.util.ArrayList;
import java.util.List;

// Bullet and numbered lists: switching blocks in and out of them, and keeping numbers in order
final class ListEdits {

    private ListEdits(){
    }

    // What the blocks the selection touches are: all the same kind (that kind), or mixed (null)
    static Block.Kind KindOfSelection(EditState state){
        Block.Kind kind = null;
        for(int i = state.BlockIndexAt(state.SelectionStart()); i <= state.BlockIndexAt(state.SelectionEnd()); i++){
            Block.Kind here = state.BlockAt(i).GetKind();
            if(kind != null && kind != here){
                return null;
            }
            kind = here;
        }
        return kind;
    }

    // Makes the blocks the selection touches this kind of list item; if they all are already, makes them paragraphs
    static EditState Toggle(EditState state, Block.Kind kind){
        int first = state.BlockIndexAt(state.SelectionStart());
        int last = state.BlockIndexAt(state.SelectionEnd());
        Block.Kind target = KindOfSelection(state) == kind ? Block.Kind.PARAGRAPH : kind;
        List<EditBlock> blocks = new ArrayList<>(state.GetBlocks());
        for(int i = first; i <= last; i++){
            blocks.set(i, Made(blocks.get(i), target));
        }
        Renumber(blocks, first, last);
        return state.With(blocks, state.GetAnchor(), state.GetCaret(), state.GetPending());
    }

    // A block made a different kind (the caret stays where it is)
    static EditState SetKind(EditState state, int index, Block.Kind kind){
        List<EditBlock> blocks = new ArrayList<>(state.GetBlocks());
        blocks.set(index, Made(blocks.get(index), kind));
        Renumber(blocks, index, index);
        return state.With(blocks, state.GetAnchor(), state.GetCaret(), state.GetPending());
    }

    // The block as another kind; a paragraph is set apart from what is before it by a blank line
    private static EditBlock Made(EditBlock block, Block.Kind kind){
        EditBlock changed = block.WithKind(kind, 1);
        return kind == Block.Kind.PARAGRAPH ? changed.WithNewlinesBefore(Math.max(block.GetNewlinesBefore(), TextEdits.paragraphGap)) : changed;
    }

    // Numbers 1, 2, 3... every numbered run that includes or touches a block from first to last
    static void Renumber(List<EditBlock> blocks, int first, int last){
        int i = Math.max(0, first - 1);
        int stop = Math.min(blocks.size() - 1, last + 1);
        while(i <= stop){
            if(blocks.get(i).GetKind() != Block.Kind.NUMBERED){
                i++;
                continue;
            }
            int number = 1;
            while(i < blocks.size() && blocks.get(i).GetKind() == Block.Kind.NUMBERED){
                blocks.set(i, blocks.get(i).WithNumber(number++));
                i++;
            }
        }
    }
}
