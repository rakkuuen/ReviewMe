package Controller.BackEnd.RichText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// One block of a field's text: a paragraph, a bullet item or a numbered item. Lists don't nest. newlinesBefore is how many line breaks
// separate it from the block before (1 when the next line follows straight on, 2 for a blank line between), so spacing and saving can
// match what was written. A numbered item keeps its number. Immutable
public class Block {
    public enum Kind { PARAGRAPH, BULLET, NUMBERED }

    private final Kind kind;
    private final List<Inline> inlines;
    private final int newlinesBefore;
    private final int number;

    private Block(Kind kind, List<Inline> inlines, int newlinesBefore, int number){
        this.kind = kind;
        this.inlines = Collections.unmodifiableList(new ArrayList<>(inlines));
        this.newlinesBefore = newlinesBefore;
        this.number = number;
    }

    public static Block Paragraph(List<Inline> inlines, int newlinesBefore){ return new Block(Kind.PARAGRAPH, inlines, newlinesBefore, 0); }
    public static Block Bullet(List<Inline> inlines, int newlinesBefore){ return new Block(Kind.BULLET, inlines, newlinesBefore, 0); }
    public static Block Numbered(List<Inline> inlines, int newlinesBefore, int number){ return new Block(Kind.NUMBERED, inlines, newlinesBefore, number); }

    public Kind GetKind(){ return kind; }
    public List<Inline> GetInlines(){ return inlines; }

    // Line breaks between the end of the block before and the start of this one (0 for the first block)
    public int GetNewlinesBefore(){ return newlinesBefore; }

    // Is there a blank line between the block before and this one
    public boolean FollowsBlankLine(){ return newlinesBefore > 1; }

    // The item's number for a numbered item, otherwise 0
    public int GetNumber(){ return number; }

    public boolean IsListItem(){ return kind != Kind.PARAGRAPH; }

    // Does it hold anything: a character that isn't a space or *, or an emoji
    public boolean HasContent(){
        for(Inline inline : inlines){
            if(inline.GetKind() == Inline.Kind.EMOJI || (inline.GetKind() == Inline.Kind.TEXT && HasWords(inline.GetText()))){
                return true;
            }
        }
        return false;
    }

    // Spaces and stray * (marks with nothing between them) are not words
    private static boolean HasWords(String text){
        return text.chars().anyMatch(c -> !Character.isWhitespace(c) && c != '*');
    }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof Block)){
            return false;
        }
        Block that = (Block) other;
        return kind == that.kind && newlinesBefore == that.newlinesBefore && number == that.number && inlines.equals(that.inlines);
    }

    @Override
    public int hashCode(){
        return Objects.hash(kind, inlines, newlinesBefore, number);
    }

    @Override
    public String toString(){
        return kind + "(" + newlinesBefore + (number > 0 ? "," + number : "") + ")" + inlines;
    }
}
