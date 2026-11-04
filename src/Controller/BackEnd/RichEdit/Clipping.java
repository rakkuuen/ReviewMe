package Controller.BackEnd.RichEdit;

import Controller.BackEnd.Emoji.EmojiNames;
import Controller.BackEnd.RichText.Block;
import Controller.BackEnd.RichText.RichDocument;
import Controller.BackEnd.RichText.RunStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

// Text going out to the clipboard (as plain words, or as the Markdown it is stored as) and coming in from it
final class Clipping {

    private Clipping(){
    }

    // The words between two positions: an emoji as ":name:", a line break as a newline, a list item after its "- " or "1. " when it is
    // taken from its start
    static String PlainText(EditState state, int from, int to){
        StringBuilder out = new StringBuilder();
        List<EditBlock> pieces = TextEdits.Slice(state, from, to);
        for(int i = 0; i < pieces.size(); i++){
            EditBlock piece = pieces.get(i);
            out.append(i > 0 ? "\n" : "");
            if(piece.GetKind() == Block.Kind.BULLET){
                out.append("- ");
            } else if(piece.GetKind() == Block.Kind.NUMBERED){
                out.append(piece.GetNumber()).append(". ");
            }
            for(Cell cell : piece.GetCells()){
                out.append(cell.Text());
            }
        }
        return out.toString();
    }

    // The Markdown for what lies between two positions
    static String Markdown(EditState state, int from, int to, Predicate<String> knownEmoji){
        return DocConvert.ToDocument(TextEdits.Slice(state, from, to), false, null).ToMarkdown(knownEmoji);
    }

    // Plain pasted text as blocks: a blank line between paragraphs, a single line break stays a break inside one, ":name:" of a known emoji
    // becomes the emoji. Nothing in it is read as Markdown. Every character takes the style
    static List<EditBlock> FromPlain(String text, RunStyle style, Predicate<String> knownEmoji){
        List<EditBlock> blocks = new ArrayList<>();
        List<Cell> cells = new ArrayList<>();
        boolean pendingBreak = false;
        for(String line : text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1)){
            if(line.trim().isEmpty()){
                Flush(blocks, cells);
                pendingBreak = false;
                continue;
            }
            if(pendingBreak){
                cells.add(Cell.Break());
            }
            cells.addAll(Cells(line, style, knownEmoji));
            pendingBreak = true;
        }
        Flush(blocks, cells);
        return blocks;
    }

    private static void Flush(List<EditBlock> blocks, List<Cell> cells){
        if(!cells.isEmpty()){
            blocks.add(EditBlock.Paragraph(TextEdits.paragraphGap, cells));
            cells.clear();
        }
    }

    private static List<Cell> Cells(String line, RunStyle style, Predicate<String> knownEmoji){
        List<Cell> cells = new ArrayList<>();
        int i = 0;
        while(i < line.length()){
            int close = line.charAt(i) == ':' ? NameEnd(line, i, knownEmoji) : -1;
            if(close > 0){
                cells.add(Cell.Emoji(line.substring(i + 1, close), style));
                i = close + 1;
                continue;
            }
            int cp = line.codePointAt(i);
            if(!Character.isISOControl(cp)){
                cells.add(Cell.Char(cp, style));
            }
            i += Character.charCount(cp);
        }
        return cells;
    }

    // Where the closing colon is of a known ":name:" that starts at i, or -1
    private static int NameEnd(String line, int i, Predicate<String> knownEmoji){
        int end = i + 1;
        while(end < line.length() && EmojiNames.IsNameChar(line.charAt(end))){
            end++;
        }
        boolean closed = end < line.length() && line.charAt(end) == ':';
        return closed && knownEmoji.test(line.substring(i + 1, end)) ? end : -1;
    }

    // The blocks of a Markdown text
    static List<EditBlock> FromDocument(RichDocument document){
        return DocConvert.FromDocument(document);
    }
}
