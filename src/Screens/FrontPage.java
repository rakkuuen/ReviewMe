package Screens;

import java.util.List;

import Database.GameReviewDao;
import Factory.GameCellFactory;
import Model.GameReview;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Graphics;
import Model.GameCell;
import Model.Scroller;


public class FrontPage {
    private GameCellFactory myFactoryOfCells;
    private List<GameCell> myCellsToPaint;

    // Cells are custom-painted (not real Swing components), so scrolling is handled by
    // Scroller rather than a JScrollPane
    private static final int bottomPadding = 40; // Buffer past the last cell so scroll doesn't dead-stop right on its edge
    private Scroller scroller;

    public FrontPage(Dimension windowDimension){
        myFactoryOfCells = new GameCellFactory();
        myCellsToPaint = myFactoryOfCells.GetAllGameCells();

        // locY ends up sitting at the bottom edge of the last cell once the factory's done
        scroller = new Scroller(myFactoryOfCells.locY + bottomPadding, windowDimension.height);
        Reflow(windowDimension);
    }

    // Re-centers cells horizontally and updates the scrollable viewport height against
    // the window's current size - called on construction and again on every resize
    public void Reflow(Dimension windowDimension){
        int centeredX = (windowDimension.width - GameCell.width) / 2;
        for(GameCell cell : myCellsToPaint){
            cell.x = centeredX;
        }
        scroller.SetContentHeight(myFactoryOfCells.locY + bottomPadding, windowDimension.height);
    }

    public void Scroll(int wheelRotation){
        scroller.Scroll(wheelRotation);
    }

    public void paint(Graphics g, Point mousePos){
        Point adjustedMousePos = scroller.AdjustPoint(mousePos);

        Graphics g2 = scroller.ApplyTo(g);
        // Paint all of the cells to screen after setting them up in cell factory
        for(GameCell cell : myCellsToPaint){
            if(cell != null){
                cell.paint(g2, adjustedMousePos);
            }
        }
        g2.dispose();
    }

    // Pure hit-test, no side effects - lets callers check where a press landed and where
    // a release landed separately, without querying the database on every check
    public GameCell GetCellAt(Point mousePos){
        Point adjustedMousePos = scroller.AdjustPoint(mousePos);
        for(GameCell cell : myCellsToPaint){
            if(cell.contains(adjustedMousePos)){
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
