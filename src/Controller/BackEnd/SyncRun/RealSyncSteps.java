package Controller.BackEnd.SyncRun;

import java.io.IOException;
import java.util.function.BooleanSupplier;

import Controller.BackEnd.Steam.AchievementSyncRun;
import Controller.BackEnd.Steam.AchievementSyncSummary;
import Controller.BackEnd.Steam.LibrarySync;
import Controller.BackEnd.Steam.LibrarySyncResult;
import Controller.BackEnd.Steam.SteamClient;
import Controller.BackEnd.Steam.SyncProgress;

// The real sync: Steam's Web API with the key and id from Resources/local.properties, spaced out so Steam isn't hit too fast
public class RealSyncSteps implements SyncSteps {
    private static final long minGapMillis = 300;

    @Override
    public LibrarySyncResult RunLibrary() throws IOException {
        return LibrarySync.Run(SteamClient.FromConfig(minGapMillis));
    }

    @Override
    public AchievementSyncSummary RunAchievements(SyncProgress progress, BooleanSupplier cancelRequested) throws IOException {
        return AchievementSyncRun.Run(SteamClient.FromConfig(minGapMillis), false, null, progress, cancelRequested);
    }
}
