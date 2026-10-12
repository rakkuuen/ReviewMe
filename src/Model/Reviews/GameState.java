package Model.Reviews;

// What has happened with the game itself
public enum GameState {
    COMPLETED,       // 100%'d, or marked complete by hand where there are no achievements
    IN_PROGRESS,     // Not finished and currently being played
    NOT_COMPLETED,   // Played but not finished, and not currently being played
    NOT_STARTED      // Never played (needs Steam data to tell)
}
