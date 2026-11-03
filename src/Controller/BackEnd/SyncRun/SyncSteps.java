package Controller.BackEnd.SyncRun;

import java.io.IOException;
import java.util.function.BooleanSupplier;

import Controller.BackEnd.Steam.AchievementSyncSummary;
import Controller.BackEnd.Steam.LibrarySyncResult;
import Controller.BackEnd.Steam.SyncProgress;

// The two things a sync does, so a run can be tried without Steam: fetch and save the library, then sync the achievements of the games
// that need it. Both can fail with an IOException (a SteamException when the call itself failed)
public interface SyncSteps {
    LibrarySyncResult RunLibrary() throws IOException;

    AchievementSyncSummary RunAchievements(SyncProgress progress, BooleanSupplier cancelRequested) throws IOException;
}
