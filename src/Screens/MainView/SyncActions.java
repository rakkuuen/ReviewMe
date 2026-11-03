package Screens.MainView;

import java.util.function.Supplier;

import Controller.BackEnd.SyncRun.SyncSnapshot;
import Controller.BackEnd.SyncRun.SyncWords;

// What the main view needs for syncing: how the sync stands now (asked at every paint, since it changes while it runs), and what the
// Sync, Retry and Open settings buttons do
public class SyncActions {
    private final Supplier<SyncSnapshot> status;
    private final Runnable sync;
    private final Runnable openSettings;

    public SyncActions(Supplier<SyncSnapshot> status, Runnable sync, Runnable openSettings){
        this.status = status;
        this.sync = sync;
        this.openSettings = openSettings;
    }

    // A sync that never runs: always idle, buttons do nothing
    public static SyncActions None(){
        return new SyncActions(SyncWords::Idle, () -> { }, () -> { });
    }

    public SyncSnapshot Status(){ return status.get(); }
    public void Sync(){ sync.run(); }
    public void OpenSettings(){ openSettings.run(); }
}
