package Controller.BackEnd.Overview;

import Controller.BackEnd.MainView.MainTab;
import Controller.BackEnd.MainView.StateSelection;
import Controller.BackEnd.MainView.ViewTarget;
import Model.Reviews.GameState;

// What each Overview list is called, what it says when it is empty, and where "View all" goes
public class OverviewSectionInfo {
    private static final String seriesTitle = "Series";
    private static final String seriesEmpty = "No series yet";

    public static String Title(OverviewSectionKind kind){
        switch(kind){
            case INCOMPLETE: return "Incomplete reviews";
            case MORE_ACHIEVEMENTS: return "More achievements added";
            case IN_PROGRESS: return "In progress";
            case COMPLETED: return "Completed";
            case SHELVED: return "Shelved";
            default: return "Not started";
        }
    }

    public static String EmptyText(OverviewSectionKind kind){
        switch(kind){
            case INCOMPLETE: return "Nothing incomplete";
            case MORE_ACHIEVEMENTS: return "No new achievements to chase";
            case IN_PROGRESS: return "Nothing in progress";
            case COMPLETED: return "Nothing completed yet";
            case SHELVED: return "Nothing shelved";
            default: return "Everything has been played";
        }
    }

    public static ViewTarget ViewAll(OverviewSectionKind kind){
        StateSelection hundred = StateSelection.Of(GameState.COMPLETED);
        switch(kind){
            case INCOMPLETE: return new ViewTarget(hundred, MainTab.INCOMPLETE);
            case MORE_ACHIEVEMENTS: return new ViewTarget(hundred, MainTab.MORE_ACHIEVEMENTS);
            case COMPLETED: return new ViewTarget(hundred, MainTab.COMPLETED);
            case IN_PROGRESS: return new ViewTarget(StateSelection.Of(GameState.IN_PROGRESS), MainTab.GAMES);
            case SHELVED: return new ViewTarget(StateSelection.Of(GameState.SHELVED), MainTab.GAMES);
            default: return new ViewTarget(StateSelection.Of(GameState.NOT_STARTED), MainTab.GAMES);
        }
    }

    public static String SeriesTitle(){ return seriesTitle; }
    public static String SeriesEmptyText(){ return seriesEmpty; }
    public static ViewTarget SeriesViewAll(){ return new ViewTarget(StateSelection.All(), MainTab.SERIES); }
}
