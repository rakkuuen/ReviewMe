package Controller.BackEnd.RichText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

// A field's text as blocks of styled pieces. Parsed from the Markdown that is stored; the parsed document remembers the exact text it came
// from, so ToMarkdown gives that text back byte for byte until the document is changed. A changed document (With...) is written out fresh.
// leading and trailing are the blank lines before the first block and after the last. Immutable
public class RichDocument {
    private final List<Block> blocks;
    private final String leading;
    private final String trailing;
    private final String source;   // Null once the document has been changed

    RichDocument(List<Block> blocks, String leading, String trailing, String source){
        this.blocks = Collections.unmodifiableList(new ArrayList<>(blocks));
        this.leading = leading;
        this.trailing = trailing;
        this.source = source;
    }

    // A new document (nothing to remember)
    public static RichDocument Of(List<Block> blocks){
        return new RichDocument(blocks, "", "", null);
    }

    public List<Block> GetBlocks(){ return blocks; }
    public String GetLeading(){ return leading; }
    public String GetTrailing(){ return trailing; }

    // The same text with different blocks
    public RichDocument WithBlocks(List<Block> changed){
        return new RichDocument(changed, leading, trailing, null);
    }

    // The Markdown to store: the text this was parsed from if nothing was changed, otherwise the blocks written out
    public String ToMarkdown(){
        return source != null ? source : RichTextWriter.Write(this);
    }

    // The same, knowing which :names: are emoji, so text that looks like one is kept as text
    public String ToMarkdown(Predicate<String> knownEmoji){
        return source != null ? source : RichTextWriter.Write(this, knownEmoji);
    }

    // Is there anything written: a character that isn't a space, or an emoji. Marks with no text don't count
    public boolean HasContent(){
        for(Block block : blocks){
            if(block.HasContent()){
                return true;
            }
        }
        return false;
    }

    // Same blocks and blank lines (whether or not they came from the same text)
    public boolean SameContentAs(RichDocument other){
        return blocks.equals(other.blocks) && leading.equals(other.leading) && trailing.equals(other.trailing);
    }

    // Would the two look the same: same blocks, line breaks, emoji and characters in the same styles. A space is the same whatever its
    // bold, italic, colour or font (you can't see them on a space), so the writer may move a space out of a mark
    public boolean SameLookAs(RichDocument other){
        return Look().equals(other.Look());
    }

    private List<String> Look(){
        List<String> look = new ArrayList<>();
        look.add(leading);
        for(Block block : blocks){
            look.add(block.GetKind() + " " + block.GetNewlinesBefore() + " " + block.GetNumber());
            for(Inline inline : block.GetInlines()){
                if(inline.GetKind() == Inline.Kind.TEXT){
                    for(char c : inline.GetText().toCharArray()){
                        RunStyle style = inline.GetStyle();
                        look.add(c + " " + (Character.isWhitespace(c) ? RunStyle.plain.WithLink(style.GetLink()) : style));
                    }
                } else {
                    look.add(inline.GetKind() + " " + inline.GetText() + " " + inline.GetStyle());
                }
            }
        }
        look.add(trailing);
        return look;
    }

    @Override
    public boolean equals(Object other){
        return other instanceof RichDocument && SameContentAs((RichDocument) other);
    }

    @Override
    public int hashCode(){
        return Objects.hash(blocks, leading, trailing);
    }
}
