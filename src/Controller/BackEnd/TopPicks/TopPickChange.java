package Controller.BackEnd.TopPicks;

// One review moving to a slot, or off the Top picks when slot is null
public class TopPickChange {
    private final int reviewId;
    private final Integer slot;

    public TopPickChange(int reviewId, Integer slot){
        this.reviewId = reviewId;
        this.slot = slot;
    }

    public int GetReviewId(){ return reviewId; }
    public Integer GetSlot(){ return slot; }
}
