package Controller.BackEnd.Overview;

import Model.Library.LibraryGame;

// One game in a list on the Overview: its title, a line of detail under it, and its rating as typed (null for none). The game itself
// comes along so the row can be opened
public class GameRow {
    private final LibraryGame game;
    private final String title;
    private final String detail;
    private final String rating;

    public GameRow(LibraryGame game, String title, String detail, String rating){
        this.game = game;
        this.title = title;
        this.detail = detail;
        this.rating = rating;
    }

    public LibraryGame GetGame(){ return game; }
    public String GetTitle(){ return title; }
    public String GetDetail(){ return detail; }
    public String GetRating(){ return rating; }

    // Only a game with a review has a page to open (one without gets a Write review flow later)
    public boolean CanOpen(){ return game.HasReview(); }
}
