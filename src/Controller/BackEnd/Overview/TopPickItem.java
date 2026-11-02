package Controller.BackEnd.Overview;

// One slot of the Top picks panel: its number, and the game in it (title and rating as typed, null when the slot is empty)
public class TopPickItem {
    private final int slot;
    private final String title;
    private final String rating;

    public TopPickItem(int slot, String title, String rating){
        this.slot = slot;
        this.title = title;
        this.rating = rating;
    }

    public int GetSlot(){ return slot; }
    public String GetTitle(){ return title; }
    public String GetRating(){ return rating; }
    public boolean IsEmpty(){ return title == null; }
}
