package Controller.BackEnd.RichText;

import java.util.List;

// A document as one line of plain text, for the places that preview a written field (list rows, search results, series cards). Marks are
// dropped (bold, italic, colour, font; a link keeps its words), line and paragraph breaks become a space, items after the first in a
// list are separated with a middle dot, and runs of spaces become one. The caller cuts it to the width it has
public class PlainText {
    private static final String itemSeparator = " " + (char) 0x00B7 + " ";

    // How an emoji shows in a preview: as :name:, or not at all
    public enum EmojiMode { SHORTCODE, HIDDEN }

    public static String Make(RichDocument document, EmojiMode emoji){
        StringBuilder out = new StringBuilder();
        List<Block> blocks = document.GetBlocks();
        for(int i = 0; i < blocks.size(); i++){
            Block block = blocks.get(i);
            if(i > 0){
                out.append(block.IsListItem() && blocks.get(i - 1).IsListItem() ? itemSeparator : " ");
            }
            for(Inline inline : block.GetInlines()){
                out.append(WordsOf(inline, emoji));
            }
        }
        return out.toString().replaceAll("\\s+", " ").trim();
    }

    private static String WordsOf(Inline inline, EmojiMode emoji){
        if(inline.GetKind() == Inline.Kind.BREAK){
            return " ";
        }
        if(inline.GetKind() == Inline.Kind.EMOJI){
            return emoji == EmojiMode.SHORTCODE ? ":" + inline.GetText() + ":" : "";
        }
        return inline.GetText();
    }
}
