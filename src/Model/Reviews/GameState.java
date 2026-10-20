package Model.Reviews;

// What has happened with the game itself
public enum GameState {
    COMPLETED,       // 100%'d, or marked complete by hand where there are no achievements (a recorded fact, Steam adding achievements doesn't undo it)
    IN_PROGRESS,     // Worked out: has achievements below 100%, or has been played and has none. Not shelved, not completed
    SHELVED,         // Set by hand, with a reason. Overrides every other state
    NOT_STARTED      // Never played (worked out from Steam)
}
