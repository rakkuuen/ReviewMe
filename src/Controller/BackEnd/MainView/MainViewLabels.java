package Controller.BackEnd.MainView;

import Model.Reviews.GameState;

// The words on the State row and the tabs
public class MainViewLabels {

    // A state's name, or All when null
    public static String StateLabel(GameState state){
        if(state == null){
            return "All";
        }
        switch(state){
            case COMPLETED: return "100%ed";
            case IN_PROGRESS: return "In progress";
            case SHELVED: return "Shelved";
            default: return "Not started";
        }
    }

    public static String TabLabel(MainTab tab){
        switch(tab){
            case OVERVIEW: return "Overview";
            case COMPLETED: return "Completed";
            case INCOMPLETE: return "Incomplete";
            case MORE_ACHIEVEMENTS: return "More ach.";
            case GAMES: return "Games";
            default: return "Series";
        }
    }
}
