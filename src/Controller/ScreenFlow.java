package Controller;

// Which screen is showing, and where Back goes from a game. A game opened from the main view goes back to the main view, and one opened
// from the old list (the Library stopgap) goes back to the list. No drawing in here, so the routes can be checked on their own
public class ScreenFlow {

    public enum Screen {
        MAIN_VIEW,
        FRONT_PAGE,   // TEMP: the old flat list of every review, until the Library screen is built
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

    // Escape on the old list goes back to the main view. Anywhere else it does nothing. True if it moved
    public boolean Escape(){
        if(current != Screen.FRONT_PAGE){
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
