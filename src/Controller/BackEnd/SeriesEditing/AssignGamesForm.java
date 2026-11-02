package Controller.BackEnd.SeriesEditing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import Controller.BackEnd.MainView.MainViewLabels;
import Model.Library.LibraryGame;
import Model.Reviews.Series;

// What the assign picker holds: the games that could be put in the series (ones with a review, not already in it) in title order, the
// search words, and which games are ticked. A game's tick stays when a search hides it. A game already in another series says
// where it moves from. No drawing and no database in here
public class AssignGamesForm {
    private static final String dot = " " + (char) 0x00B7 + " ";   // A middle dot, by code point so the source stays plain ASCII
    private static final char openQuote = (char) 0x201C;
    private static final char closeQuote = (char) 0x201D;

    private final String seriesId;
    private final List<Candidate> candidates = new ArrayList<>();
    private final Set<String> picked = new LinkedHashSet<>();
    private String search = "";

    // One game that could be assigned: its title and a line saying where it is now
    public static class Candidate {
        private final String title;
        private final String detail;

        private Candidate(String title, String detail){
            this.title = title;
            this.detail = detail;
        }

        public String GetTitle(){ return title; }
        public String GetDetail(){ return detail; }
    }

    public AssignGamesForm(String seriesId, List<LibraryGame> games, List<Series> allSeries){
        this.seriesId = seriesId;
        for(LibraryGame game : games){
            if(!game.HasReview() || seriesId.equals(game.GetReview().GetSeriesId())){
                continue;
            }
            candidates.add(new Candidate(game.GetTitle(), Detail(game, allSeries)));
        }
        candidates.sort((a, b) -> a.title.compareToIgnoreCase(b.title));
    }

    private static String Detail(LibraryGame game, List<Series> allSeries){
        String state = MainViewLabels.StateLabel(game.GetStatus().GetGameState());
        String from = game.GetReview().GetSeriesId();
        if(from == null){
            return state;
        }
        for(Series series : allSeries){
            if(series.GetId().equals(from)){
                return "Moves from " + series.GetName() + dot + state;
            }
        }
        return state;
    }

    public String GetSeriesId(){ return seriesId; }

    public String GetSearch(){ return search; }
    public void SetSearch(String typed){ search = typed == null ? "" : typed.trim(); }

    // The games that match the search, in title order
    public List<Candidate> GetVisible(){
        List<Candidate> visible = new ArrayList<>();
        for(Candidate candidate : candidates){
            if(search.isEmpty() || candidate.title.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))){
                visible.add(candidate);
            }
        }
        return visible;
    }

    // Takes the ticks from what is picked in the list: the visible games ticked here are picked, the visible ones not ticked are not.
    // Games hidden by the search keep whatever they had
    public void SetVisiblePicked(Collection<String> pickedTitles){
        for(Candidate candidate : GetVisible()){
            if(pickedTitles.contains(candidate.title)){
                picked.add(candidate.title);
            } else {
                picked.remove(candidate.title);
            }
        }
    }

    // The ticked games, in title order
    public List<String> GetPicked(){
        List<String> ordered = new ArrayList<>();
        for(Candidate candidate : candidates){
            if(picked.contains(candidate.title)){
                ordered.add(candidate.title);
            }
        }
        return Collections.unmodifiableList(ordered);
    }

    public boolean IsPicked(String title){ return picked.contains(title); }

    // "3 selected"
    public String GetCountLine(){ return picked.size() + " selected"; }

    // "Assign 3 games", "Assign 1 game", or "Assign games" with none ticked
    public String GetAssignWords(){
        int count = picked.size();
        return count == 0 ? "Assign games" : "Assign " + count + (count == 1 ? " game" : " games");
    }

    public boolean CanAssign(){ return !picked.isEmpty(); }

    // What the list says when no game matches: nothing to assign at all, or nothing matching the search
    public String GetEmptyTitle(){
        if(!GetVisible().isEmpty()){
            return "";
        }
        return search.isEmpty() ? "There are no games to assign" : "No games match " + openQuote + search + closeQuote;
    }

    public String GetEmptyHint(){
        if(!GetVisible().isEmpty() || search.isEmpty()){
            return "";
        }
        return "Check the spelling, or search by part of the title.";
    }
}
