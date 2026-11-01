package Controller.BackEnd.Overview;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import Model.Library.LibraryGame;

// Everything the Overview shows, worked out and ready to draw. Nothing in it needs the database
public class OverviewData {
    private final OverviewStats stats;
    private final List<LibraryGame> topPicks;
    private final Map<OverviewSectionKind, OverviewSection> sections;
    private final List<OverviewSeriesGroup> seriesGroups;
    private final int seriesTotal;

    public OverviewData(OverviewStats stats, List<LibraryGame> topPicks, Map<OverviewSectionKind, OverviewSection> sections,
                        List<OverviewSeriesGroup> seriesGroups, int seriesTotal){
        this.stats = stats;
        this.topPicks = Collections.unmodifiableList(topPicks);
        this.sections = Collections.unmodifiableMap(sections);
        this.seriesGroups = Collections.unmodifiableList(seriesGroups);
        this.seriesTotal = seriesTotal;
    }

    public OverviewStats GetStats(){ return stats; }

    // One entry per slot, in slot order. Null for a slot with nothing pinned
    public List<LibraryGame> GetTopPicks(){ return topPicks; }

    public OverviewSection GetSection(OverviewSectionKind kind){ return sections.get(kind); }

    // The first few series, most recently played first, and how many series there are in all
    public List<OverviewSeriesGroup> GetSeriesGroups(){ return seriesGroups; }
    public int GetSeriesTotal(){ return seriesTotal; }
}
