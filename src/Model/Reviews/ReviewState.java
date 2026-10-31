package Model.Reviews;

// What the write-up looks like, whatever state the game is in. Only a 100%ed game can have a complete review: a review written
// before the game is finished is partial however much of it is filled in
public enum ReviewState {
    COMPLETE,   // Game is completed, nothing missing and no manual flag
    PARTIAL,    // Something is written but a section is missing, a sub-heading is empty, it's flagged by hand, or the game isn't completed
    NONE        // Nothing written (no review at all, or every section blank)
}
