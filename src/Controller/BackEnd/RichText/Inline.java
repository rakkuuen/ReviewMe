package Controller.BackEnd.RichText;

import java.util.Objects;

// One piece of a line of text: a run of characters in one style, an emoji (by its name, without the colons), or a line break inside a
// paragraph. Immutable
public class Inline {
    public enum Kind { TEXT, EMOJI, BREAK }

    private final Kind kind;
    private final String text;
    private final RunStyle style;

    private Inline(Kind kind, String text, RunStyle style){
        this.kind = kind;
        this.text = text;
        this.style = style;
    }

    public static Inline Text(String text, RunStyle style){ return new Inline(Kind.TEXT, text, style); }
    public static Inline Emoji(String name, RunStyle style){ return new Inline(Kind.EMOJI, name, style); }
    public static Inline Break(){ return new Inline(Kind.BREAK, "", RunStyle.plain); }

    public Kind GetKind(){ return kind; }

    // The characters of a text run, the name of an emoji, or empty for a break
    public String GetText(){ return text; }

    public RunStyle GetStyle(){ return style; }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof Inline)){
            return false;
        }
        Inline that = (Inline) other;
        return kind == that.kind && text.equals(that.text) && style.equals(that.style);
    }

    @Override
    public int hashCode(){
        return Objects.hash(kind, text, style);
    }

    @Override
    public String toString(){
        return kind + "[" + text.replace("\n", "\\n") + "," + style + "]";
    }
}
