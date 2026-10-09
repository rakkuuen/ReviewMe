package Controller.BackEnd.Importer.Parsing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Reads one md file into tags, genres, metadata and sections. Knows nothing about templates
public class MarkdownFileParser {
    // #### Tags: / #### Genres: / #### Metadata:
    private static final Pattern tagHeader = Pattern.compile("^####\\s*(Tags|Genres|Metadata)\\s*:?\\s*$", Pattern.CASE_INSENSITIVE);
    // # Gameplay (levels 1 to 3). Level 4 is only ever a tag header
    private static final Pattern sectionHeading = Pattern.compile("^#{1,3}\\s+\\S.*");
    // #Review, #PC: a hash straight onto a word
    private static final Pattern tagLine = Pattern.compile("^#[^\\s#].*");

    public static ParsedMarkdown Parse(Path file, Path root) throws IOException {
        String fileName = file.getFileName().toString();
        String title = fileName.replaceAll("\\.md$", "");
        String relativePath = root.relativize(file).toString().replace('\\', '/');
        ParsedMarkdown parsed = new ParsedMarkdown(title, relativePath);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        String tagBlock = null;        // which tag line we're waiting for: tags, genres or metadata
        String currentHeading = null;
        boolean seenContent = false;   // for spotting an old-style tag line with no header

        for(int i = 0; i < lines.size(); i++){
            String raw = lines.get(i);
            if(i == 0 && raw.startsWith("\uFEFF")){
                raw = raw.substring(1);
            }
            String trimmed = raw.trim();

            Matcher header = tagHeader.matcher(trimmed);
            if(header.matches()){
                tagBlock = header.group(1).toLowerCase();
                if(tagBlock.equals("tags")){
                    parsed.SetHasTagsHeader(true);
                }
                seenContent = true;
                continue;
            }

            if(tagBlock != null){
                if(trimmed.isEmpty()){
                    continue;
                }
                if(tagLine.matcher(trimmed).matches()){
                    List<String> target = tagBlock.equals("tags") ? parsed.GetTags()
                            : tagBlock.equals("genres") ? parsed.GetGenres() : parsed.GetMetadata();
                    target.addAll(Arrays.asList(trimmed.split("\\s+")));
                    tagBlock = null;
                    continue;
                }
                tagBlock = null;   // the block was empty, this line is something else
            }

            String clean = StripHtml(trimmed);

            // Old-style file: tags on the first line, no #### Tags: header
            if(!seenContent && !parsed.HasTagsHeader() && tagLine.matcher(clean).matches()){
                parsed.GetTags().addAll(Arrays.asList(clean.split("\\s+")));
                seenContent = true;
                continue;
            }

            if(sectionHeading.matcher(clean).matches()){
                currentHeading = clean.replaceFirst("^#+\\s*", "").trim();
                parsed.GetSections().computeIfAbsent(currentHeading, key -> new ArrayList<>());
                seenContent = true;
                continue;
            }

            if(clean.isEmpty()){
                continue;
            }
            seenContent = true;
            if(currentHeading == null){
                parsed.GetLooseLines().add(clean);
            } else {
                parsed.GetSections().get(currentHeading).add(clean);
            }
        }
        return parsed;
    }

    // Reviews wrap ratings in <font size = 5><b>9</b></font>
    private static String StripHtml(String text){
        return text.replaceAll("<.*?>", "").trim();
    }
}
