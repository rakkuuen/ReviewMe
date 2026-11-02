package Controller.BackEnd.GameDetail;

import java.util.Collections;
import java.util.List;

// Everything a game's page shows, worked out and worded: title, the link to its series, badges, the stats strip, tags and sections
public class GameDetailData {
    public static final String lockedNote = "Locked " + (char) 0x00B7 + " read only";

    private final String title;
    private final String seriesId;
    private final String seriesLink;
    private final List<DetailBadge> badges;
    private final List<DetailStat> stats;
    private final List<String> tags;
    private final List<String> metadataTags;
    private final List<DetailSection> sections;

    public GameDetailData(String title, String seriesId, String seriesLink, List<DetailBadge> badges, List<DetailStat> stats, List<String> tags, List<String> metadataTags,
            List<DetailSection> sections){
        this.title = title;
        this.seriesId = seriesId;
        this.seriesLink = seriesLink;
        this.badges = Collections.unmodifiableList(badges);
        this.stats = Collections.unmodifiableList(stats);
        this.tags = Collections.unmodifiableList(tags);
        this.metadataTags = Collections.unmodifiableList(metadataTags);
        this.sections = Collections.unmodifiableList(sections);
    }

    public String GetTitle(){ return title; }

    // The series this game is in, or null
    public String GetSeriesId(){ return seriesId; }

    // "Fallout, 2 of 4", or null when it is in no series
    public String GetSeriesLink(){ return seriesLink; }

    public List<DetailBadge> GetBadges(){ return badges; }
    public List<DetailStat> GetStats(){ return stats; }

    // Genres then platforms
    public List<String> GetTags(){ return tags; }

    // The odd tags (DLC notes and the like), drawn differently
    public List<String> GetMetadataTags(){ return metadataTags; }

    public List<DetailSection> GetSections(){ return sections; }
}
