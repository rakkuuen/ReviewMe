package Model.Reviews;

// What the write-up looks like, whatever state the game is in
public enum ReviewState {
    REVIEWED,            // Nothing missing and no manual flag
    REVIEW_INCOMPLETE,   // A section is missing, a sub-heading is empty, or it's flagged by hand
    NOT_REVIEWED         // Nothing written for a game that hasn't been started, so no review is expected yet
}
