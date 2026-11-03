package Controller.BackEnd.SyncRun;

import java.io.IOException;
import java.util.function.Consumer;

import Controller.BackEnd.Steam.AchievementSyncSummary;
import Controller.BackEnd.Steam.GameSyncResult;
import Controller.BackEnd.Steam.LibrarySyncResult;

// Runs a sync from start to finish: the library, then the achievements, telling the listener how it stands after each step. It never
// throws: every way it can stop ends in a snapshot the screen can show. This blocks until the sync is over, so the caller runs it off
// the screen's thread
public class SyncRunner {

    public static SyncSnapshot Run(SyncSteps steps, Consumer<SyncSnapshot> listener){
        listener.accept(SyncWords.Running(0, 0));
        LibrarySyncResult library;
        try {
            library = steps.RunLibrary();
        } catch (IOException e) {
            return Finish(listener, SyncWords.Failure(SyncFailures.Classify(e), e.getMessage()));
        }
        if(library.AreDetailsHidden()){
            return Finish(listener, SyncWords.Failure(SyncState.PRIVATE_PROFILE, null));
        }
        if(!library.Succeeded()){
            return Finish(listener, SyncWords.Failure(SyncState.FAILED, library.GetProblem()));
        }

        AchievementSyncSummary summary;
        try {
            summary = steps.RunAchievements((done, total, game, result) -> listener.accept(SyncWords.Running(done, total)), null);
        } catch (IOException e) {
            return Finish(listener, SyncWords.Failure(SyncFailures.Classify(e), e.getMessage()));
        }
        if(summary.GetStopReason() == AchievementSyncSummary.StopReason.NOT_PUBLIC){
            return Finish(listener, SyncWords.Failure(SyncState.PRIVATE_PROFILE, null));
        }
        if(summary.GetStopReason() == AchievementSyncSummary.StopReason.TOO_MANY_FAILURES){
            return Finish(listener, SyncWords.Failure(SyncState.FAILED, "Steam kept failing, so the sync stopped. What was done is saved; try again in a while."));
        }
        int updated = summary.CountOf(GameSyncResult.Outcome.SYNCED);
        return Finish(listener, SyncWords.Success(library.GetTotal(), updated));
    }

    private static SyncSnapshot Finish(Consumer<SyncSnapshot> listener, SyncSnapshot last){
        listener.accept(last);
        return last;
    }
}
