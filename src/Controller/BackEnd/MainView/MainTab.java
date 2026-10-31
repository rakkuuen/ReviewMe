package Controller.BackEnd.MainView;

// The tabs of the main view. Which of them exist depends on the State row (see ViewRules)
public enum MainTab {
    OVERVIEW,            // Stats, top picks and short lists. Only with State = All
    COMPLETED,           // 100%ed, review complete, no More achievements tag
    INCOMPLETE,          // 100%ed, review partial or none
    MORE_ACHIEVEMENTS,   // 100%ed, gained achievements since
    GAMES,               // One list of every game in the state (In progress, Shelved, Not started)
    SERIES               // Series with at least one game in the state
}
