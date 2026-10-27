package Controller.BackEnd.Steam;

import Model.Library.SteamGame;

// Told after each game of a library-wide achievement sync, so a console (or later a screen) can show how far it is
public interface SyncProgress {

    // done counts the games finished so far (this one included), out of total. result says how this one went
    void Report(int done, int total, SteamGame game, GameSyncResult result);
}
