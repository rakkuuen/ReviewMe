package Controller.BackEnd.RichEdit;

import Controller.BackEnd.RichText.Block;
import Controller.BackEnd.RichText.Inline;
import Controller.BackEnd.RichText.RichDocument;
import Controller.BackEnd.RichText.RunStyle;
import java.util.ArrayList;
import java.util.List;

// Turns a RichDocument into the blocks being edited and back
final class DocConvert {
    private static final int paragraphGap = 2;   // A blank line between paragraphs

    private DocConvert(){
    }

    static List<EditBlock> FromDocument(RichDocument document){
        List<EditBlock> blocks = new ArrayList<>();
        for(Block block : document.GetBlocks()){
            blocks.add(new EditBlock(block.GetKind(), block.GetNumber(), block.GetNewlinesBefore(), CellsOf(block.GetInlines())));
        }
        return blocks;
    }

    static List<Cell> CellsOf(List<Inline> inlines){
        List<Cell> cells = new ArrayList<>();
        for(Inline inline : inlines){
            switch(inline.GetKind()){
                case EMOJI:
                    cells.add(Cell.Emoji(inline.GetText(), inline.GetStyle()));
                    break;
                case BREAK:
                    cells.add(Cell.Break());
                    break;
                default:
                    inline.GetText().codePoints().forEach(cp -> cells.add(Cell.Char(cp, inline.GetStyle())));
                    break;
            }
        }
        return cells;
    }

    // The text as a document to store: blocks with nothing in them are left out (a gap of blank lines stands in for an empty paragraph), and
    // the blank lines round the original text are kept when it had some words. With keepEmpty the empty blocks stay, for showing the text
    static RichDocument ToDocument(List<EditBlock> blocks, boolean keepEmpty, RichDocument original){
        List<Block> out = new ArrayList<>();
        int carriedGap = 0;
        for(EditBlock block : keepEmpty ? blocks : Normal.ForStorage(blocks)){
            if(IsBlank(block) && !keepEmpty){
                carriedGap = block.IsListItem() ? carriedGap : paragraphGap;
                continue;
            }
            int gap = out.isEmpty() ? 0 : Math.max(carriedGap, Math.max(1, block.GetNewlinesBefore()));
            boolean afterParagraph = !out.isEmpty() && !out.get(out.size() - 1).IsListItem();
            out.add(Built(block, afterParagraph && !block.IsListItem() ? Math.max(gap, paragraphGap) : gap));
            carriedGap = 0;
        }
        boolean keepEdges = original != null && !original.GetBlocks().isEmpty();
        return keepEdges ? original.WithBlocks(out) : RichDocument.Of(out);
    }

    // Nothing in it but spaces and stray stars: it reads back as a blank line, or as no words at all
    private static boolean IsBlank(EditBlock block){
        return block.GetCells().stream().allMatch(cell -> cell.IsSpace() || cell.Is('*'));
    }

    private static Block Built(EditBlock block, int gap){
        List<Inline> inlines = InlinesOf(block.GetCells());
        switch(block.GetKind()){
            case BULLET: return Block.Bullet(inlines, gap);
            case NUMBERED: return Block.Numbered(inlines, gap, block.GetNumber());
            default: return Block.Paragraph(inlines, gap);
        }
    }

    // Neighbouring characters in one style are one run of text
    static List<Inline> InlinesOf(List<Cell> cells){
        List<Inline> inlines = new ArrayList<>();
        StringBuilder run = new StringBuilder();
        RunStyle runStyle = null;
        for(Cell cell : cells){
            if(cell.IsChar() && cell.GetStyle().equals(runStyle)){
                run.appendCodePoint(cell.GetCodePoint());
                continue;
            }
            if(runStyle != null){
                inlines.add(Inline.Text(run.toString(), runStyle));
                run.setLength(0);
                runStyle = null;
            }
            if(cell.IsChar()){
                runStyle = cell.GetStyle();
                run.appendCodePoint(cell.GetCodePoint());
            } else {
                inlines.add(cell.IsEmoji() ? Inline.Emoji(cell.GetName(), cell.GetStyle()) : Inline.Break());
            }
        }
        if(runStyle != null){
            inlines.add(Inline.Text(run.toString(), runStyle));
        }
        return inlines;
    }
}
