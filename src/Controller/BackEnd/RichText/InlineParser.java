package Controller.BackEnd.RichText;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Reads the marks inside one block's text: **bold**, *italic*, [text](https://link), <span class="rm-c-amber rm-f-serif">text</span> and
// :emoji: (only names that are known). Anything it doesn't understand stays as the plain characters it was. Works out, character by
// character, what style each has and which characters are only marks, then builds the pieces. A newline becomes a break
class InlineParser {
    private static final String escapable = "\\*[]()<>:`_-+.";
    private static final String spanClose = "</span>";
    private static final String hiddenBreak = "<!---->";
    private static final Pattern spanOpen = Pattern.compile("<span class=\"([^\"<>]*)\">");
    private static final Pattern classKey = Pattern.compile("rm-([cf])-([a-z0-9_]+)");
    private static final Pattern link = Pattern.compile("\\[((?:\\\\.|[^\\[\\]\\n\\\\])*)\\]\\((https?://[^\\s()]+)\\)");
    private static final int emojiNameMin = 2;
    private static final int emojiNameMax = 24;
    private static final int longestEmphasis = 3;
    private static final int boldMarks = 2;

    // A run of * waiting for its closing one. remaining counts the marks not used yet, from the start of the run
    private static class Opener {
        private final int start;
        private int remaining;

        private Opener(int start, int length){
            this.start = start;
            this.remaining = length;
        }
    }

    private final String text;
    private final int length;
    private final Predicate<String> knownEmoji;
    private final boolean[] consumed;   // Characters that are only marks
    private final boolean[] literal;    // Characters that must stay as they are (escaped, or inside backticks)
    private final boolean[] hidden;     // The characters of an empty comment, which count as a space next to a mark
    private final int[] bold;
    private final int[] italic;
    private final String[] colour;
    private final String[] font;
    private final String[] linkTo;
    private final String[] emojiAt;     // The emoji name at the character that starts one
    private final int[] emojiEnd;

    private InlineParser(String text, Predicate<String> knownEmoji){
        this.text = text;
        this.length = text.length();
        this.knownEmoji = knownEmoji;
        this.consumed = new boolean[length];
        this.literal = new boolean[length];
        this.hidden = new boolean[length];
        this.bold = new int[length];
        this.italic = new int[length];
        this.colour = new String[length];
        this.font = new String[length];
        this.linkTo = new String[length];
        this.emojiAt = new String[length];
        this.emojiEnd = new int[length];
    }

    static List<Inline> Parse(String text, Predicate<String> knownEmoji){
        InlineParser parser = new InlineParser(text, knownEmoji);
        parser.MarkLiterals();
        parser.FindHiddenBreaks();
        parser.FindSpans();
        parser.FindLinks();
        parser.FindEmoji();
        parser.PairEmphasis();
        return parser.Build();
    }

    // A backslash before a mark makes the mark an ordinary character; so does being inside backticks
    private void MarkLiterals(){
        int i = 0;
        while(i < length){
            char c = text.charAt(i);
            if(c == '\\' && i + 1 < length && escapable.indexOf(text.charAt(i + 1)) >= 0){
                consumed[i] = true;
                literal[i + 1] = true;
                i += 2;
            } else if(c == '`'){
                int close = i + 1;
                while(close < length && text.charAt(close) != '`' && text.charAt(close) != '\n'){
                    close++;
                }
                if(close < length && text.charAt(close) == '`'){
                    for(int k = i; k <= close; k++){
                        literal[k] = true;
                    }
                    i = close + 1;
                } else {
                    i++;
                }
            } else {
                i++;
            }
        }
    }

    // An empty comment, which the writer puts between two runs of * that must not run together
    private void FindHiddenBreaks(){
        int at = text.indexOf(hiddenBreak);
        while(at >= 0){
            if(!literal[at]){
                Consume(at, at + hiddenBreak.length());
                for(int k = at; k < at + hiddenBreak.length(); k++){
                    hidden[k] = true;
                }
            }
            at = text.indexOf(hiddenBreak, at + hiddenBreak.length());
        }
    }

    private void FindSpans(){
        Deque<int[]> open = new ArrayDeque<>();   // start of the tag, end of the tag
        Deque<String[]> openKeys = new ArrayDeque<>();
        Matcher matcher = spanOpen.matcher(text);
        int p = text.indexOf('<');
        while(p >= 0){
            if(!literal[p] && !consumed[p]){
                matcher.region(p, length);
                if(text.startsWith(spanClose, p) && !open.isEmpty()){
                    int[] opened = open.pop();
                    String[] keys = openKeys.pop();
                    Style(opened[1], p, keys[0], keys[1], null);
                    Consume(opened[0], opened[1]);
                    Consume(p, p + spanClose.length());
                } else if(matcher.lookingAt()){
                    String[] keys = SpanKeys(matcher.group(1));
                    if(keys != null){
                        open.push(new int[] { p, matcher.end() });
                        openKeys.push(keys);
                    }
                }
            }
            p = text.indexOf('<', p + 1);
        }
    }

    // {colour, font} from the class names of a span (either may be null), or null when it isn't one of ours
    private String[] SpanKeys(String classes){
        String chosenColour = null, chosenFont = null;
        String[] names = classes.trim().split("\\s+");
        for(String name : names){
            Matcher key = classKey.matcher(name);
            if(!key.matches()){
                return null;
            }
            if(key.group(1).equals("c") && chosenColour == null){
                chosenColour = key.group(2);
            } else if(key.group(1).equals("f") && chosenFont == null){
                chosenFont = key.group(2);
            } else {
                return null;
            }
        }
        return new String[] { chosenColour, chosenFont };
    }

    private void FindLinks(){
        Matcher matcher = link.matcher(text);
        while(matcher.find()){
            int start = matcher.start();
            boolean image = start > 0 && text.charAt(start - 1) == '!';
            if(image || literal[start] || consumed[start]){
                continue;
            }
            Style(matcher.start(1), matcher.end(1), null, null, matcher.group(2));
            Consume(start, start + 1);
            Consume(matcher.end(1), matcher.end());
        }
    }

    private void FindEmoji(){
        int i = 0;
        while(i < length){
            int end = i;
            if(text.charAt(i) == ':' && !literal[i] && !consumed[i]){
                end = EmojiEnd(i);
            }
            if(end > i){
                emojiAt[i] = text.substring(i + 1, end - 1);
                emojiEnd[i] = end;
                Consume(i, end);
                i = end;
            } else {
                i++;
            }
        }
    }

    // Where the emoji starting at this colon ends, or the colon's own position when there isn't a known one here
    private int EmojiEnd(int colon){
        int j = colon + 1;
        while(j < length && !literal[j] && !consumed[j] && IsNameChar(text.charAt(j))){
            j++;
        }
        int nameLength = j - colon - 1;
        boolean closed = j < length && text.charAt(j) == ':' && !literal[j] && !consumed[j];
        if(closed && nameLength >= emojiNameMin && nameLength <= emojiNameMax && knownEmoji.test(text.substring(colon + 1, j))){
            return j + 1;
        }
        return colon;
    }

    private static boolean IsNameChar(char c){
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_';
    }

    // Matches runs of * as openers and closers. A run opens when a non-space follows it and closes when a non-space is before it. A
    // closer uses up the nearest opener's marks (two make bold, one makes italic); marks left over stay as plain *
    private void PairEmphasis(){
        Deque<Opener> openers = new ArrayDeque<>();
        int i = 0;
        while(i < length){
            if(!IsMark(i)){
                i++;
                continue;
            }
            int end = i;
            while(end < length && IsMark(end)){
                end++;
            }
            int run = end - i;
            if(run <= longestEmphasis){
                boolean canClose = i > 0 && !IsSpace(i - 1);
                boolean canOpen = end < length && !IsSpace(end);
                int used = canClose ? Close(openers, i, run) : 0;
                if(used == 0 && canOpen){
                    openers.push(new Opener(i, run));
                }
            }
            i = end;
        }
    }

    private boolean IsSpace(int i){
        return Character.isWhitespace(text.charAt(i)) || hidden[i];
    }

    private boolean IsMark(int i){
        return text.charAt(i) == '*' && !literal[i] && !consumed[i];
    }

    // Closes as many openers as this run of marks can; returns how many of its marks were used
    private int Close(Deque<Opener> openers, int start, int run){
        int used = 0;
        while(used < run && !openers.isEmpty()){
            Opener opener = openers.peek();
            int count = Math.min(Math.min(run - used, opener.remaining), boldMarks);
            int contentStart = opener.start + opener.remaining;
            int contentEnd = start + used;
            for(int k = contentStart; k < contentEnd; k++){
                if(count == boldMarks){
                    bold[k]++;
                } else {
                    italic[k]++;
                }
            }
            Consume(contentStart - count, contentStart);
            Consume(contentEnd, contentEnd + count);
            opener.remaining -= count;
            used += count;
            if(opener.remaining == 0){
                openers.pop();
            }
        }
        return used;
    }

    // Colour, font and link for the characters in a range (an inner span, which is read first, keeps what it has)
    private void Style(int from, int to, String colourKey, String fontKey, String address){
        for(int k = from; k < to; k++){
            if(colour[k] == null){
                colour[k] = colourKey;
            }
            if(font[k] == null){
                font[k] = fontKey;
            }
            if(linkTo[k] == null){
                linkTo[k] = address;
            }
        }
    }

    private void Consume(int from, int to){
        for(int k = from; k < to; k++){
            consumed[k] = true;
        }
    }

    private RunStyle StyleAt(int i){
        return RunStyle.plain.WithBold(bold[i] > 0).WithItalic(italic[i] > 0).WithColour(colour[i]).WithFont(font[i]).WithLink(linkTo[i]);
    }

    private List<Inline> Build(){
        List<Inline> inlines = new ArrayList<>();
        StringBuilder run = new StringBuilder();
        RunStyle runStyle = RunStyle.plain;
        int i = 0;
        while(i < length){
            if(emojiAt[i] != null){
                Flush(inlines, run, runStyle);
                inlines.add(Inline.Emoji(emojiAt[i], StyleAt(i)));
                i = emojiEnd[i];
            } else if(consumed[i]){
                i++;
            } else if(text.charAt(i) == '\n'){
                Flush(inlines, run, runStyle);
                inlines.add(Inline.Break());
                i++;
            } else {
                RunStyle style = StyleAt(i);
                if(run.length() > 0 && !style.equals(runStyle)){
                    Flush(inlines, run, runStyle);
                }
                runStyle = style;
                run.append(text.charAt(i));
                i++;
            }
        }
        Flush(inlines, run, runStyle);
        return inlines;
    }

    private static void Flush(List<Inline> inlines, StringBuilder run, RunStyle style){
        if(run.length() > 0){
            inlines.add(Inline.Text(run.toString(), style));
            run.setLength(0);
        }
    }
}
