package Controller.BackEnd.Importer.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import Controller.BackEnd.Importer.Results.ImportResult;
import Controller.BackEnd.Importer.Results.MappedReview;
import Controller.BackEnd.Importer.Results.SeriesNote;

// Each series with its games, template and notes file
public class SeriesSection implements ReportSection {

    @Override
    public List<String> Lines(ImportResult result){
        Map<String, List<String>> bySeries = new TreeMap<>();
        int noSeries = 0;
        for(MappedReview m : result.GetReviews()){
            String series = m.GetReview().GetSeriesId();
            if(series == null){
                noSeries++;
            } else {
                bySeries.computeIfAbsent(series, key -> new ArrayList<>()).add(m.GetReview().GetTitle());
            }
        }

        List<String> out = new ArrayList<>();
        out.add("== SERIES ==");
        for(Map.Entry<String, List<String>> entry : bySeries.entrySet()){
            String template = result.GetTemplates().containsKey(entry.getKey()) ? entry.getKey() : "Standard";
            out.add(entry.getKey() + " (" + entry.getValue().size() + " games, template " + template + ", "
                    + NotesDescription(result, entry.getKey()) + "): " + String.join(", ", entry.getValue()));
        }
        for(SeriesNote note : result.GetSeriesNotes()){
            if(!bySeries.containsKey(note.GetSeriesId())){
                out.add(note.GetSeriesId() + " has a notes file (" + note.GetSourcePath() + ") but no games");
            }
        }
        out.add("games with no series: " + noSeries);
        out.add("");
        return out;
    }

    private String NotesDescription(ImportResult result, String seriesId){
        return result.GetSeriesNotes().stream().filter(n -> n.GetSeriesId().equalsIgnoreCase(seriesId))
                .map(n -> "notes from " + n.GetSourcePath() + " (" + n.GetText().length() + " chars)")
                .findFirst().orElse("no notes file");
    }
}
