package Controller.BackEnd.Importer.Results;

// The rules/notes file of a series (e.g. "My rules for the Yakuza reviews"), which isn't a review itself
public class SeriesNote {
    private String seriesId, sourcePath, text;

    public SeriesNote(String seriesId, String sourcePath, String text){
        this.seriesId = seriesId;
        this.sourcePath = sourcePath;
        this.text = text;
    }

    public String GetSeriesId(){ return seriesId; }
    public String GetSourcePath(){ return sourcePath; }
    public String GetText(){ return text; }
}
