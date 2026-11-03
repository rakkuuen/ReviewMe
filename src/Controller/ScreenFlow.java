package Controller;

// Which screen is showing, and where Back goes from a game or a series. A game opened from the main view goes back to the main view,
// one opened from a series' page goes back to that page, and one opened from the old list (the Library stopgap) goes back to the list. No drawing in here, so the routes can be checked on their own
public class ScreenFlow {

    public enum Screen {
        MAIN_VIEW,
        FRONT_PAGE,   // TEMP: the old flat list of every review, until the Library screen is built
        SERIES_VIEW,
        LIBRARY,
        ACHIEVEMENTS,
        SETTINGS,
        GAME_INFO_SCREEN
    }

    private Screen current = Screen.MAIN_VIEW;
    private Screen cameFrom = Screen.MAIN_VIEW;

    public Screen Current(){ return current; }

    public void ShowMainView(){
        current = Screen.MAIN_VIEW;
    }

    public void ShowFrontPage(){
        current = Screen.FRONT_PAGE;
    }

    public void ShowLibrary(){
        current = Screen.LIBRARY;
    }

    // Settings was opened from the main view. Back (or Escape) returns there
    public void ShowSettings(){
        current = Screen.SETTINGS;
    }

    // A series' page was opened from the main view
    public void OpenSeries(){
        current = Screen.SERIES_VIEW;
    }

    // Back from a series' page to the main view
    public void CloseSeries(){
        if(current == Screen.SERIES_VIEW){
            current = Screen.MAIN_VIEW;
        }
    }

    // A game's achievements were opened from its page
    public void OpenAchievements(){
        if(current == Screen.GAME_INFO_SCREEN){
            current = Screen.ACHIEVEMENTS;
        }
    }

    // Back from the achievements to the game's page
    public void CloseAchievements(){
        if(current == Screen.ACHIEVEMENTS){
            current = Screen.GAME_INFO_SCREEN;
        }
    }

    // Escape on the achievements goes back to the game. On the old list, the Library, Settings or a series' page it goes back to the main view. Anywhere else it does nothing. True if it moved
    public boolean Escape(){
        if(current == Screen.ACHIEVEMENTS){
            current = Screen.GAME_INFO_SCREEN;
            return true;
        }
        if(current != Screen.FRONT_PAGE && current != Screen.SERIES_VIEW && current != Screen.LIBRARY && current != Screen.SETTINGS){
            return false;
        }
        current = Screen.MAIN_VIEW;
        return true;
    }

    // A game was opened from whatever is showing. Opening another game while one is open keeps the original way back
    public void OpenGame(){
        if(current != Screen.GAME_INFO_SCREEN){
            cameFrom = current;
        }
        current = Screen.GAME_INFO_SCREEN;
    }

    // Back from a game: the screen it was opened from, which is now showing. Does nothing if no game is open
    public Screen CloseGame(){
        if(current == Screen.GAME_INFO_SCREEN){
            current = cameFrom;
        }
        return current;
    }
}
