package Controller.BackEnd.Importer.Parsing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Reads one md file into tags, genres, metadata and sections. Knows nothing about templates.
// It reads line by line and remembers a little: which tag line it is waiting for, and which section it is in
public class MarkdownFileParser {
    // #### Tags: / #### Genres: / #### Metadata:
    private static final Pattern tagHeader = Pattern.compile("^####\\s*(Tags|Genres|Metadata)\\s*:?\\s*$", Pattern.CASE_INSENSITIVE);
    // # Gameplay (levels 1 to 3). Level 4 is only ever a tag header
    private static final Pattern sectionHeading = Pattern.compile("^#{1,3}\\s+\\S.*");
    // #Review, #PC: a hash straight onto a word
    private static final Pattern tagLine = Pattern.compile("^#[^\\s#].*");
    // Some editors put this invisible mark at the very start of a file
    private static final char byteOrderMark = 0xFEFF;

    private ParsedMarkdown parsed;
    private String tagBlock = null;        // which tag line we're waiting for: tags, genres or metadata
    private String currentHeading = null;  // the section we're inside
    private boolean seenContent = false;   // lets us spot an old-style tag line with no header

    private MarkdownFileParser(ParsedMarkdown parsed){
        this.parsed = parsed;
    }

    public static ParsedMarkdown Parse(Path file, Path root) throws IOException {
        String title = file.getFileName().toString().replaceAll("\\.md$", "");
        String relativePath = root.relativize(file).toString().replace('\\', '/');
        MarkdownFileParser parser = new MarkdownFileParser(new ParsedMarkdown(title, relativePath));

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for(int i = 0; i < lines.size(); i++){
            parser.ReadLine(i == 0 ? StripByteOrderMark(lines.get(i)) : lines.get(i));
        }
        return parser.parsed;
    }

    // The first rule that wants the line takes it
    private void ReadLine(String raw){
        String trimmed = raw.trim();
        if(ReadTagHeader(trimmed) || ReadTagLine(trimmed)){
            return;
        }

        String clean = StripHtml(trimmed);
        if(ReadOldStyleTags(clean) || ReadSectionHeading(clean)){
            return;
        }
        ReadContent(clean);
    }

    // "#### Genres:" means the next tag line belongs to genres
    private boolean ReadTagHeader(String trimmed){
        Matcher header = tagHeader.matcher(trimmed);
        if(!header.matches()){
            return false;
        }
        tagBlock = header.group(1).toLowerCase();
        if(tagBlock.equals("tags")){
            parsed.SetHasTagsHeader(true);
        }
        seenContent = true;
        return true;
    }

    // While waiting for a tag line: skip blanks, take the tag line, or give up if the block was empty
    private boolean ReadTagLine(String trimmed){
        if(tagBlock == null){
            return false;
        }
        if(trimmed.isEmpty()){
            return true;
        }
        if(tagLine.matcher(trimmed).matches()){
            TagListFor(tagBlock).addAll(Arrays.asList(trimmed.split("\\s+")));
            tagBlock = null;
            return true;
        }
        tagBlock = null;   // the block was empty, so this line is something else
        return false;
    }

    // Old files have the tags on line 1 with no "#### Tags:" header
    private boolean ReadOldStyleTags(String clean){
        if(seenContent || parsed.HasTagsHeader() || !tagLine.matcher(clean).matches()){
            return false;
        }
        parsed.GetTags().addAll(Arrays.asList(clean.split("\\s+")));
        seenContent = true;
        return true;
    }

    // "# Gameplay" starts a new section
    private boolean ReadSectionHeading(String clean){
        if(!sectionHeading.matcher(clean).matches()){
            return false;
        }
        currentHeading = clean.replaceFirst("^#+\\s*", "").trim();
        parsed.GetSections().computeIfAbsent(currentHeading, key -> new ArrayList<>());
        seenContent = true;
        return true;
    }

    // Text belongs to the current section, or is loose if it sits above every heading. Blank lines are dropped
    private void ReadContent(String clean){
        if(clean.isEmpty()){
            return;
        }
        seenContent = true;
        if(currentHeading == null){
            parsed.GetLooseLines().add(clean);
        } else {
            parsed.GetSections().get(currentHeading).add(clean);
        }
    }

    private List<String> TagListFor(String block){
        if(block.equals("tags")){
            return parsed.GetTags();
        }
        return block.equals("genres") ? parsed.GetGenres() : parsed.GetMetadata();
    }

    private static String StripByteOrderMark(String line){
        return !line.isEmpty() && line.charAt(0) == byteOrderMark ? line.substring(1) : line;
    }

    // Reviews wrap ratings in <font size = 5><b>9</b></font>
    private static String StripHtml(String text){
        return text.replaceAll("<.*?>", "").trim();
    }
}
