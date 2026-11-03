package Controller.BackEnd.SyncRun;

// Where a Steam sync is: not running, running, finished well, or stopped for one of the reasons the screen explains
public enum SyncState {
    IDLE,
    RUNNING,
    SUCCESS,
    INVALID_KEY,        // Steam didn't accept the API key, or there isn't one set up
    PRIVATE_PROFILE,    // Game details aren't Public
    OFFLINE,            // Steam couldn't be reached
    RATE_LIMITED,       // Steam is asking to slow down
    FAILED;             // Anything else

    public boolean IsFailure(){
        return this != IDLE && this != RUNNING && this != SUCCESS;
    }
}
