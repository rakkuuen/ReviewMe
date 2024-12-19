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

    public void CheckWhichCellWasClicked(Point mousePos){
        for(GameCell cell : myCellsToPaint){
            if(cell.contains(mousePos)){
                // This should take me to another screen with the games info and ability to add or edit a review
                System.out.println("You clicked: " + cell.gameTitle);

                // Need to get the game review from the cell name
                GameReview myGameReview = GameReviewDao.GetGameReview(cell.gameTitle);
                GameInfoScreen myGameInfoScreen = new GameInfoScreen(myGameReview);
            }
        }
    }
}
