package Screens;

import java.util.List;

import Database.GameReviewDao;
import Factory.GameCellFactory;
import Model.GameReview;

import java.awt.Point;
import java.awt.Graphics;
import Model.GameCell;


public class FrontPage {
    private GameCellFactory myFactoryOfCells;
    private List<GameCell> myCellsToPaint;
    public FrontPage(){
        myFactoryOfCells = new GameCellFactory();
        myCellsToPaint = myFactoryOfCells.GetAllGameCells();

    }

    public void paint(Graphics g, Point mousePos){
        // Paint all of the cells to screen after setting them up in cell factory
        for(GameCell cell : myCellsToPaint){
            if(cell != null){
                cell.paint(g, mousePos);
            }
        }
    }

    // Pure hit-test, no side effects - lets callers check where a press landed and where
    // a release landed separately, without querying the database on every check
    public GameCell GetCellAt(Point mousePos){
        for(GameCell cell : myCellsToPaint){
            if(cell.contains(mousePos)){
                return cell;
            }
        }
        return null;
    }

    public GameReview CheckWhichCellWasClicked(Point mousePos){
        GameCell cell = GetCellAt(mousePos);
        if(cell == null){
            return null;
        }
        // This should take me to another screen with the games info and ability to add or edit a review
        System.out.println("You clicked: " + cell.gameTitle);

        // Need to get the game review from the cell name
        return GameReviewDao.GetGameReview(cell.gameTitle);
    }
}
