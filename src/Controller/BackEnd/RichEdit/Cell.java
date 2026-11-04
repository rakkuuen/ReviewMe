package Controller.BackEnd.RichEdit;

import Controller.BackEnd.RichText.RunStyle;
import java.util.Objects;

// One character of text being edited: a character (a whole code point), an emoji (by name) or a line break inside a block, each with the
// style it is drawn in. The caret sits between cells, so an emoji is one step like a letter. Immutable
public final class Cell {
    public enum Kind { CHAR, EMOJI, BREAK }

    private static final Cell lineBreak = new Cell(Kind.BREAK, 0, "", RunStyle.plain);

    private final Kind kind;
    private final int codePoint;
    private final String name;
    private final RunStyle style;

    private Cell(Kind kind, int codePoint, String name, RunStyle style){
        this.kind = kind;
        this.codePoint = codePoint;
        this.name = name;
        this.style = style;
    }

    public static Cell Char(int codePoint, RunStyle style){ return new Cell(Kind.CHAR, codePoint, "", style); }
    public static Cell Emoji(String name, RunStyle style){ return new Cell(Kind.EMOJI, 0, name, style); }
    public static Cell Break(){ return lineBreak; }

    public Kind GetKind(){ return kind; }
    public int GetCodePoint(){ return codePoint; }
    public String GetName(){ return name; }
    public RunStyle GetStyle(){ return style; }

    public boolean IsChar(){ return kind == Kind.CHAR; }
    public boolean IsEmoji(){ return kind == Kind.EMOJI; }
    public boolean IsBreak(){ return kind == Kind.BREAK; }

    // Is it this very character
    public boolean Is(char c){ return kind == Kind.CHAR && codePoint == c; }

    // Is it a space or tab (a line break is not)
    public boolean IsSpace(){ return kind == Kind.CHAR && Character.isWhitespace(codePoint); }

    // A letter, digit or underscore
    public boolean IsWordChar(){ return kind == Kind.CHAR && (Character.isLetterOrDigit(codePoint) || codePoint == '_'); }

    public Cell WithStyle(RunStyle changed){
        return kind == Kind.BREAK ? this : new Cell(kind, codePoint, name, changed);
    }

    // As plain text: the character, ":name:" for an emoji, a newline for a break
    public String Text(){
        switch(kind){
            case EMOJI: return ":" + name + ":";
            case BREAK: return "\n";
            default: return new String(Character.toChars(codePoint));
        }
    }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof Cell)){
            return false;
        }
        Cell that = (Cell) other;
        return kind == that.kind && codePoint == that.codePoint && name.equals(that.name) && style.equals(that.style);
    }

    @Override
    public int hashCode(){
        return Objects.hash(kind, codePoint, name, style);
    }

    @Override
    public String toString(){
        return Text() + (style.IsPlain() ? "" : style.toString());
    }
}
