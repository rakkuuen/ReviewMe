package Controller.BackEnd.SeriesView;

import java.util.Collections;
import java.util.List;

import Controller.BackEnd.Overview.StatCard;

// Everything one series' page shows, worded and ready to draw: its name, template line, a warning when games use another template,
// the stat cards, its notes (or a note that there are none) and its games in order
public class SeriesViewData {
    private final String seriesId;
    private final String name;
    private final String templateLine;
    private final String templateWarning;
    private final List<StatCard> cards;
    private final String notes;
    private final boolean hasNotes;
    private final List<SeriesGameItem> games;

    public SeriesViewData(String seriesId, String name, String templateLine, String templateWarning, List<StatCard> cards, String notes, boolean hasNotes, List<SeriesGameItem> games){
        this.seriesId = seriesId;
        this.name = name;
        this.templateLine = templateLine;
        this.templateWarning = templateWarning;
        this.cards = Collections.unmodifiableList(cards);
        this.notes = notes;
        this.hasNotes = hasNotes;
        this.games = Collections.unmodifiableList(games);
    }

    public String GetSeriesId(){ return seriesId; }
    public String GetName(){ return name; }

    // "Template: Yakuza"
    public String GetTemplateLine(){ return templateLine; }

    // "2 games use a different template", or null when they all use the series' own
    public String GetTemplateWarning(){ return templateWarning; }

    public List<StatCard> GetCards(){ return cards; }

    // The notes as written, or the words that stand in for none ("No notes yet. Use Edit series to add some.")
    public String GetNotes(){ return notes; }
    public boolean HasNotes(){ return hasNotes; }

    public List<SeriesGameItem> GetGames(){ return games; }
}
