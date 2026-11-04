package Controller.BackEnd.RichEdit;

import java.util.ArrayList;
import java.util.List;

// Keeps the text to what Markdown can hold. A list item is one line, so a line break in one becomes the end of the item (a break takes the
// same single step in positions as the end of a block, so the caret and selection stay where they were). In a paragraph a break can't be
// written down if it is first or last, or has no words on the line before it or the line after (that would be a blank line, a new
// paragraph). While editing those stay as breaks, so Shift+Enter then typing gives a tight line; ForStorage turns them into paragraph ends
final class Normal {

    private Normal(){
    }

    // The state with list items made one line each
    static EditState Of(EditState state){
        return state.With(Run(state.GetBlocks(), false), state.GetAnchor(), state.GetCaret(), state.GetPending());
    }

    // The blocks as they are stored: a break that can't be written ends its paragraph too
    static List<EditBlock> ForStorage(List<EditBlock> blocks){
        return Run(blocks, true);
    }

    private static List<EditBlock> Run(List<EditBlock> from, boolean paragraphs){
        List<EditBlock> blocks = new ArrayList<>();
        List<int[]> splits = new ArrayList<>();
        for(EditBlock block : from){
            int first = blocks.size();
            blocks.addAll(Split(block, paragraphs));
            if(blocks.size() > first + 1){
                splits.add(new int[] { first, blocks.size() - 1 });
            }
        }
        for(int[] range : splits){
            ListEdits.Renumber(blocks, range[0], range[1]);
        }
        return blocks;
    }

    // Is the cell at i a line break that can't stay: any in a list item, and in a paragraph any that doesn't have words on its line before
    // it and on its line after (an empty line would make a new paragraph)
    private static boolean Offends(EditBlock block, int i, boolean paragraphs){
        if(!block.GetCells().get(i).IsBreak()){
            return false;
        }
        return block.IsListItem() || (paragraphs && (!HasWords(block, i - 1, -1) || !HasWords(block, i + 1, 1)));
    }

    // Is there anything but spaces on the line that has cell `from` in it, looking in that direction
    private static boolean HasWords(EditBlock block, int from, int step){
        for(int i = from; i >= 0 && i < block.Length() && !block.GetCells().get(i).IsBreak(); i += step){
            if(!block.GetCells().get(i).IsSpace()){
                return true;
            }
        }
        return false;
    }

    private static List<EditBlock> Split(EditBlock block, boolean paragraphs){
        List<EditBlock> out = new ArrayList<>();
        List<Cell> cells = new ArrayList<>();
        boolean list = block.IsListItem();
        EditBlock head = block;
        for(int i = 0; i < block.Length(); i++){
            if(Offends(block, i, paragraphs)){
                out.add(head.WithCells(cells));
                cells = new ArrayList<>();
                head = new EditBlock(block.GetKind(), block.GetNumber(), list ? TextEdits.itemGap : TextEdits.paragraphGap, cells);
            } else {
                cells.add(block.GetCells().get(i));
            }
        }
        out.add(head.WithCells(cells));
        return out;
    }
}
