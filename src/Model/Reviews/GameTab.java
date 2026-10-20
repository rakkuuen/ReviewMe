package Model.Reviews;

// The state-based tabs of the main view. Series isn't here because it groups games rather than filtering them by state
public enum GameTab {
    INCOMPLETE,          // Completed game, unfinished review
    MORE_ACHIEVEMENTS,   // Completed game that has gained achievements since
    IN_PROGRESS,
    COMPLETED,           // Completed, fully reviewed, and not carrying the More achievements tag
    SHELVED,
    NOT_STARTED
}
