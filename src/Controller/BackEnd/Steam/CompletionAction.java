package Controller.BackEnd.Steam;

import Model.Reviews.CompletionSource;
import Model.Reviews.GameReview;

// What recording completions from Steam would do for one review. RECORD and FILL change the review (the four values below are what it
// will hold afterwards); every other kind leaves it alone and says why. A completion that is already recorded is never overwritten:
// FILL only adds the date and the baseline when they are missing
public class CompletionAction {
    public enum Kind {
        RECORD,              // not completed, now at 100% on Steam: marked completed with date, source and baseline
        FILL,                // already completed and at 100%: only the missing date and/or baseline are added
        ALREADY_COMPLETE,    // completed, at 100%, date and baseline already there
        BELOW_FULL,          // below 100% on Steam: completion (or the lack of it) is up to the user; the baseline too
        NO_ACHIEVEMENTS,     // the game has none on Steam, so completion can't be worked out
        NO_STEAM_DATA,       // linked, but its achievements haven't been synced yet
        SHELVED              // shelved by the user: left exactly as it is
    }

    private GameReview review;
    private Kind kind;
    private boolean completed;
    private String completedOn;
    private CompletionSource source;
    private Integer baseline;

    public CompletionAction(GameReview review, Kind kind, boolean completed, String completedOn, CompletionSource source, Integer baseline){
        this.review = review;
        this.kind = kind;
        this.completed = completed;
        this.completedOn = completedOn;
        this.source = source;
        this.baseline = baseline;
    }

    // Leaves the review as it is
    public static CompletionAction Skip(GameReview review, Kind kind){
        return new CompletionAction(review, kind, review.IsCompleted(), review.GetCompletedOn(), review.GetCompletedOnSource(), review.GetAchievementsAtCompletion());
    }

    public GameReview GetReview(){ return review; }
    public Kind GetKind(){ return kind; }
    public boolean ChangesReview(){ return kind == Kind.RECORD || kind == Kind.FILL; }

    public boolean IsCompleted(){ return completed; }

    // yyyy-MM-dd, or null when no unlock time is known
    public String GetCompletedOn(){ return completedOn; }
    public CompletionSource GetCompletedOnSource(){ return source; }

    // The game's achievement total at the moment it was completed
    public Integer GetBaseline(){ return baseline; }
}
