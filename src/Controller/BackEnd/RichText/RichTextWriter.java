package Controller.BackEnd.RichText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

// Writes a RichDocument out as Markdown: **bold**, *italic*, [text](address), <span class="rm-c-amber rm-f-serif">text</span> and :emoji:.
// A space at the edge of a bold or italic stretch is written outside the marks (Markdown can't have it inside). The mark that lasts longest
// is written outermost, and a line break closes every mark. What is written is read back to check it means the same document; if a
// character in the text could be mistaken for a mark (or a line for a list item) it is escaped with a backslash and written again
public class RichTextWriter {
    private static final String hiddenBreak = "<!---->";
    private static final String escaped = "\\*[]<:`";

    // One open mark: what it is (B, I, S or L) and its detail (the span's classes or the link's address)
    private static class Mark {
        private final char kind;
        private final String detail;

        private Mark(char kind, String detail){
            this.kind = kind;
            this.detail = detail;
        }

        private boolean IsEmphasis(){
            return kind == 'B' || kind == 'I';
        }

        @Override
        public boolean equals(Object other){
            return other instanceof Mark && ((Mark) other).kind == kind && ((Mark) other).detail.equals(detail);
        }

        @Override
        public int hashCode(){
            return Objects.hash(kind, detail);
        }
    }

    // knownEmoji is every :name: that will be an emoji when the text is read again; the document's own emoji are always counted
    public static String Write(RichDocument document, Predicate<String> knownEmoji){
        RichDocument joined = Joined(document);
        Set<String> used = EmojiUsed(joined);
        Predicate<String> known = name -> used.contains(name) || knownEmoji.test(name);
        String plain = Compose(joined, false);
        if(RichTextParser.Parse(plain, known).SameLookAs(joined)){
            return plain;
        }
        return Compose(joined, true);
    }

    // Written knowing only the emoji the document has
    public static String Write(RichDocument document){
        return Write(document, name -> false);
    }

    private static Set<String> EmojiUsed(RichDocument document){
        Set<String> names = new HashSet<>();
        for(Block block : document.GetBlocks()){
            for(Inline inline : block.GetInlines()){
                if(inline.GetKind() == Inline.Kind.EMOJI){
                    names.add(inline.GetText());
                }
            }
        }
        return names;
    }

    // The same document with neighbouring runs in one style made one run
    private static RichDocument Joined(RichDocument document){
        List<Block> blocks = new ArrayList<>();
        for(Block block : document.GetBlocks()){
            blocks.add(Rebuilt(block, Merged(block.GetInlines())));
        }
        return new RichDocument(blocks, document.GetLeading(), document.GetTrailing(), null);
    }

    private static Block Rebuilt(Block block, List<Inline> inlines){
        switch(block.GetKind()){
            case BULLET: return Block.Bullet(inlines, block.GetNewlinesBefore());
            case NUMBERED: return Block.Numbered(inlines, block.GetNewlinesBefore(), block.GetNumber());
            default: return Block.Paragraph(inlines, block.GetNewlinesBefore());
        }
    }

    // Neighbouring text runs with the same style are one run
    private static List<Inline> Merged(List<Inline> inlines){
        List<Inline> out = new ArrayList<>();
        for(Inline inline : inlines){
            Inline last = out.isEmpty() ? null : out.get(out.size() - 1);
            if(last != null && last.GetKind() == Inline.Kind.TEXT && inline.GetKind() == Inline.Kind.TEXT && last.GetStyle().equals(inline.GetStyle())){
                out.set(out.size() - 1, Inline.Text(last.GetText() + inline.GetText(), inline.GetStyle()));
            } else {
                out.add(inline);
            }
        }
        return out;
    }

    private static String Compose(RichDocument document, boolean escape){
        StringBuilder out = new StringBuilder(document.GetLeading());
        List<Block> blocks = document.GetBlocks();
        for(int i = 0; i < blocks.size(); i++){
            Block block = blocks.get(i);
            if(i > 0){
                out.append("\n".repeat(Math.max(1, block.GetNewlinesBefore())));
            }
            out.append(Marker(block));
            WriteInlines(out, block.GetInlines(), escape, block.GetKind() == Block.Kind.PARAGRAPH);
        }
        return out.append(document.GetTrailing()).toString();
    }

    private static String Marker(Block block){
        if(block.GetKind() == Block.Kind.BULLET){
            return "- ";
        }
        return block.GetKind() == Block.Kind.NUMBERED ? block.GetNumber() + ". " : "";
    }

    private static void WriteInlines(StringBuilder out, List<Inline> inlines, boolean escape, boolean paragraph){
        List<Mark> open = new ArrayList<>();
        boolean justClosed = false;
        boolean lineStart = paragraph;
        for(int i = 0; i < inlines.size(); i++){
            Inline inline = inlines.get(i);
            if(inline.GetKind() == Inline.Kind.BREAK){
                lineStart = paragraph;
                CloseTo(out, open, 0);
                out.append('\n');
                justClosed = false;
                continue;
            }
            List<Mark> wanted = MarksFor(inline.GetStyle());
            justClosed = CloseTo(out, open, KeptCount(open, wanted, inlines, i)) || justClosed;
            List<Mark> added = new ArrayList<>(wanted);
            added.removeAll(open);
            if(inline.GetKind() == Inline.Kind.TEXT && inline.GetText().trim().isEmpty()){
                added.removeIf(Mark::IsEmphasis);   // Spaces never start a bold or italic mark
            }
            final int at = i;
            added.sort((a, b) -> a.kind == 'L' ? -1 : b.kind == 'L' ? 1 : Integer.compare(LastIndex(inlines, at, b), LastIndex(inlines, at, a)));
            String words = inline.GetKind() == Inline.Kind.EMOJI ? ":" + inline.GetText() + ":" : Escaped(inline.GetText(), escape);
            if(escape && lineStart && open.isEmpty() && added.isEmpty()){
                words = ListLookEscaped(words);
            }
            lineStart = false;
            String space = words.trim().isEmpty() ? "" : LeadingSpace(words);
            words = words.substring(space.length());
            boolean afterOpener = false;
            for(Mark mark : added){
                if(mark.IsEmphasis()){
                    out.append(space);
                    space = "";
                }
                Append(out, Open(mark), NeedsGuard(mark, out, open, justClosed, afterOpener));
                afterOpener = mark.IsEmphasis();
                justClosed = false;
                open.add(mark);
            }
            out.append(space).append(words);
            justClosed = false;
        }
        CloseTo(out, open, 0);
    }

    // How many of the open marks carry on into a run that wants these. A link is always outermost, so a change of link starts again. A
    // span that will end before a bold or italic mark that starts now is closed first, so the span goes inside it and the bold or italic
    // isn't cut short when the span ends
    private static int KeptCount(List<Mark> open, List<Mark> wanted, List<Inline> inlines, int at){
        int keep = 0;
        while(keep < open.size() && wanted.contains(open.get(keep))){
            keep++;
        }
        boolean linkWanted = !wanted.isEmpty() && wanted.get(0).kind == 'L';
        boolean sameLinkOpen = linkWanted && !open.isEmpty() && open.get(0).equals(wanted.get(0));
        if(linkWanted && !sameLinkOpen){
            return 0;
        }
        for(int k = 0; k < keep; k++){
            for(Mark mark : wanted){
                if(mark.IsEmphasis() && !open.contains(mark) && open.get(k).kind == 'S' && LastIndex(inlines, at, open.get(k)) < LastIndex(inlines, at, mark)){
                    return k;
                }
            }
        }
        return keep;
    }

    // Closes marks from the innermost until this many are left; true if any was closed. A space just before a bold or italic mark closes
    // goes after it instead, and a space just after one opens goes before it (Markdown doesn't close after a space or open before one)
    private static boolean CloseTo(StringBuilder out, List<Mark> open, int keep){
        boolean closed = open.size() > keep;
        while(open.size() > keep){
            Mark mark = open.remove(open.size() - 1);
            String space = mark.IsEmphasis() ? TrailingSpace(out) : "";
            out.setLength(out.length() - space.length());
            out.append(Close(mark)).append(space);
        }
        return closed;
    }

    private static String LeadingSpace(String words){
        int end = 0;
        while(end < words.length() && Character.isWhitespace(words.charAt(end))){
            end++;
        }
        return words.substring(0, end);
    }

    private static String TrailingSpace(StringBuilder out){
        int start = out.length();
        while(start > 0 && Character.isWhitespace(out.charAt(start - 1))){
            start--;
        }
        return out.substring(start);
    }

    // A line that starts like a list item ("- ", "+ ", "3. ", "3) ") has a backslash put before the mark so it stays text
    private static String ListLookEscaped(String words){
        if(words.length() > 1 && "-+".indexOf(words.charAt(0)) >= 0 && words.charAt(1) == ' '){
            return "\\" + words;
        }
        int digits = 0;
        while(digits < words.length() && Character.isDigit(words.charAt(digits))){
            digits++;
        }
        boolean numbered = digits > 0 && digits + 1 < words.length() && ".)".indexOf(words.charAt(digits)) >= 0 && words.charAt(digits + 1) == ' ';
        return numbered ? words.substring(0, digits) + "\\" + words.substring(digits) : words;
    }

    // The last inline from this one on that keeps the mark going without a break
    private static int LastIndex(List<Inline> inlines, int from, Mark mark){
        int last = from;
        while(last + 1 < inlines.size() && inlines.get(last + 1).GetKind() != Inline.Kind.BREAK && MarksFor(inlines.get(last + 1).GetStyle()).contains(mark)){
            last++;
        }
        return last;
    }

    // An empty comment (which the reader skips and counts as a space) goes in front of a mark that could be misread: a link straight after
    // a ! (that would be an image), and an emphasis mark that follows a character that isn't a space (it could close one that is open),
    // unless it is straight after another mark opening
    private static boolean NeedsGuard(Mark mark, StringBuilder out, List<Mark> open, boolean justClosed, boolean afterOpener){
        if(out.length() == 0){
            return false;
        }
        if(mark.kind == 'L'){
            return out.charAt(out.length() - 1) == '!';
        }
        return mark.IsEmphasis() && !afterOpener && FollowsWord(out, open, justClosed);
    }

    private static boolean FollowsWord(StringBuilder out, List<Mark> open, boolean justClosed){
        if(Character.isWhitespace(out.charAt(out.length() - 1))){
            return false;
        }
        boolean emphasisOpen = false;
        for(Mark mark : open){
            emphasisOpen |= mark.IsEmphasis();
        }
        return emphasisOpen || (justClosed && out.charAt(out.length() - 1) == '*');
    }

    private static void Append(StringBuilder out, String piece, boolean guard){
        if(guard){
            out.append(hiddenBreak);
        }
        out.append(piece);
    }

    private static List<Mark> MarksFor(RunStyle style){
        List<Mark> marks = new ArrayList<>();
        if(style.GetLink() != null){
            marks.add(new Mark('L', style.GetLink()));
        }
        if(style.IsBold()){
            marks.add(new Mark('B', ""));
        }
        if(style.IsItalic()){
            marks.add(new Mark('I', ""));
        }
        if(style.GetColour() != null || style.GetFont() != null){
            marks.add(new Mark('S', SpanClasses(style)));
        }
        return marks;
    }

    private static String SpanClasses(RunStyle style){
        String colour = style.GetColour() != null ? "rm-c-" + style.GetColour() : "";
        String font = style.GetFont() != null ? "rm-f-" + style.GetFont() : "";
        return colour.isEmpty() ? font : font.isEmpty() ? colour : colour + " " + font;
    }

    private static String Open(Mark mark){
        switch(mark.kind){
            case 'B': return "**";
            case 'I': return "*";
            case 'S': return "<span class=\"" + mark.detail + "\">";
            default: return "[";
        }
    }

    private static String Close(Mark mark){
        switch(mark.kind){
            case 'B': return "**";
            case 'I': return "*";
            case 'S': return "</span>";
            default: return "](" + mark.detail + ")";
        }
    }

    private static String Escaped(String words, boolean escape){
        if(!escape){
            return words;
        }
        StringBuilder out = new StringBuilder();
        for(int i = 0; i < words.length(); i++){
            char c = words.charAt(i);
            if(escaped.indexOf(c) >= 0){
                out.append('\\');
            }
            out.append(c);
        }
        return out.toString();
    }
}
