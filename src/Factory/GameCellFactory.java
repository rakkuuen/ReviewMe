package Factory;
import Database.GameReviewDao;

import Model.GameReview;
import Model.GameCell;
import java.util.ArrayList;
import java.util.List;
public class GameCellFactory {
    // This class will organise a collection of game cells in order to allow them to be scrollable on page.
    // It will need to pull from some database to fill details and make my life easier
    List<GameReview> allGameReviews;
    List<GameCell> allGameCells;
    public int locY = 50;
    public static final int locX = 300;
    public static final int spaceBetweenCellsY = 70;

    public GameCellFactory(){
        allGameReviews = GameReviewDao.GetAllGameReviews();
        allGameCells = new ArrayList<>();

        for (GameReview gameReview : allGameReviews){
            GameCell currentCell = new GameCell(gameReview.GetTitle(), locX, locY, "");
            allGameCells.add(currentCell);
            locY += spaceBetweenCellsY;
        }

    }

    public List<GameCell> GetAllGameCells(){
        return allGameCells;
    }
}
