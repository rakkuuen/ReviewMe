package Controller.BackEnd.Importer.Mapping;

import java.util.ArrayList;
import java.util.List;
import Controller.BackEnd.Importer.Parsing.ParsedMarkdown;
import Controller.BackEnd.Importer.Parsing.Section;
import Controller.BackEnd.Importer.Results.SeriesNote;

// The rules file of a series: tagged with the series and #ReviewInfo instead of #Review
public class SeriesNoteMapper {

    // Null if the file isn't one
    public static SeriesNote Map(ParsedMarkdown parsed){
        boolean isInfo = false;
        String seriesId = null;
        for(String token : parsed.GetTags()){
            String name = TagReader.StripHash(token);
            String lower = name.toLowerCase();
            if(lower.equals("reviewinfo")){
                isInfo = true;
            } else if(!lower.equals("review") && !lower.equals("game") && !TagReader.IsPlatformTag(lower) && seriesId == null){
                seriesId = name;
            }
        }
        if(!isInfo || seriesId == null){
            return null;
        }
        return new SeriesNote(seriesId, parsed.GetRelativePath(), String.join("\n", AllText(parsed)));
    }

    // Lines above any heading, then each heading followed by its lines and anything nested inside it
    private static List<String> AllText(ParsedMarkdown parsed){
        List<String> text = new ArrayList<>(parsed.GetLooseLines());
        for(Section section : parsed.GetSections().values()){
            AddSection(text, section);
        }
        return text;
    }

    private static void AddSection(List<String> text, Section section){
        text.add(section.GetHeading());
        text.addAll(section.GetLines());
        for(Section child : section.GetChildren()){
            AddSection(text, child);
        }
    }
}
