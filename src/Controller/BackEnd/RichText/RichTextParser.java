package Controller.BackEnd.RichText;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Reads a field's stored Markdown into a RichDocument. A blank line ends a paragraph; a line starting "- " (or "* ") is a bullet item and
// "1. " a numbered one; a single line break inside a paragraph stays a break in it. Everything else is text. knownEmoji says which
// :names: are emoji (any other :name: stays text). Reading never fails: whatever isn't understood is kept as the characters it was
public class RichTextParser {
    private static final Pattern bullet = Pattern.compile("^[-*+] (.*)$");
    private static final Pattern numbered = Pattern.compile("^(\\d{1,9})[.)] (.*)$");

    public static RichDocument Parse(String source, Predicate<String> knownEmoji){
        int first = FirstContentChar(source);
        if(first < 0){
            return new RichDocument(new ArrayList<>(), "", source, source);
        }
        int start = source.lastIndexOf('\n', first) + 1;
        int last = LastContentChar(source);
        int newline = source.indexOf('\n', last);
        int end = newline < 0 ? source.length() : newline;
        List<Block> blocks = Blocks(source.substring(start, end), knownEmoji);
        return new RichDocument(blocks, source.substring(0, start), source.substring(end), source);
    }

    private static int FirstContentChar(String source){
        for(int i = 0; i < source.length(); i++){
            if(!Character.isWhitespace(source.charAt(i))){
                return i;
            }
        }
        return -1;
    }

    private static int LastContentChar(String source){
        int i = source.length() - 1;
        while(i >= 0 && Character.isWhitespace(source.charAt(i))){
            i--;
        }
        return i;
    }

    // The blocks of text that starts and ends on a line with something on it
    private static List<Block> Blocks(String content, Predicate<String> knownEmoji){
        List<Block> blocks = new ArrayList<>();
        List<String> paragraph = new ArrayList<>();
        int gap = 1;           // Line breaks since the last block ended
        int paragraphGap = 0;  // The gap before the paragraph being collected
        for(String line : content.split("\n", -1)){
            Matcher bulletLine = bullet.matcher(line);
            Matcher numberedLine = numbered.matcher(line);
            if(line.trim().isEmpty()){
                Flush(blocks, paragraph, paragraphGap, knownEmoji);
                gap++;
            } else if(bulletLine.matches()){
                Flush(blocks, paragraph, paragraphGap, knownEmoji);
                blocks.add(Block.Bullet(InlineParser.Parse(bulletLine.group(1), knownEmoji), blocks.isEmpty() ? 0 : gap));
                gap = 1;
            } else if(numberedLine.matches()){
                Flush(blocks, paragraph, paragraphGap, knownEmoji);
                blocks.add(Block.Numbered(InlineParser.Parse(numberedLine.group(2), knownEmoji), blocks.isEmpty() ? 0 : gap, Integer.parseInt(numberedLine.group(1))));
                gap = 1;
            } else {
                if(paragraph.isEmpty()){
                    paragraphGap = blocks.isEmpty() ? 0 : gap;
                    gap = 1;
                }
                paragraph.add(line);
            }
        }
        Flush(blocks, paragraph, paragraphGap, knownEmoji);
        return blocks;
    }

    private static void Flush(List<Block> blocks, List<String> paragraph, int gap, Predicate<String> knownEmoji){
        if(!paragraph.isEmpty()){
            blocks.add(Block.Paragraph(InlineParser.Parse(String.join("\n", paragraph), knownEmoji), gap));
            paragraph.clear();
        }
    }
}
