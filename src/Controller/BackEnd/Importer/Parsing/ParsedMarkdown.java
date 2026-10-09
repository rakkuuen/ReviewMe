package Controller.BackEnd.Importer.Parsing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// What was read out of one md file, before any meaning is applied to it
public class ParsedMarkdown {
    private String title, relativePath;
    private boolean hasTagsHeader;
    // Tag lines as written, with the # (e.g. #Review)
    private List<String> tags = new ArrayList<>();
    private List<String> genres = new ArrayList<>();
    private List<String> metadata = new ArrayList<>();
    // Heading -> its non-blank lines, in file order
    private Map<String, List<String>> sections = new LinkedHashMap<>();
    // Lines that sit above the first heading (the series rules file is only this)
    private List<String> looseLines = new ArrayList<>();

    public ParsedMarkdown(String title, String relativePath){
        this.title = title;
        this.relativePath = relativePath;
    }

    public String GetTitle(){ return title; }
    public String GetRelativePath(){ return relativePath; }

    public boolean HasTagsHeader(){ return hasTagsHeader; }
    public void SetHasTagsHeader(boolean hasTagsHeader){ this.hasTagsHeader = hasTagsHeader; }

    public List<String> GetTags(){ return tags; }
    public List<String> GetGenres(){ return genres; }
    public List<String> GetMetadata(){ return metadata; }
    public Map<String, List<String>> GetSections(){ return sections; }
    public List<String> GetLooseLines(){ return looseLines; }
}
