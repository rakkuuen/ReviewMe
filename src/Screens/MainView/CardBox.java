package Screens.MainView;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import Model.Themes.CellTheme;
import Model.Themes.Theme;

// The box cards and rows are drawn on: the cell colour and outline of the theme, with the outline inside the box
public class CardBox {

    public static void Paint(Graphics2D g, Rectangle box, boolean hovered){
        CellTheme cell = Theme.Current.GetCell();
        int width = box.width - 1;
        int height = box.height - 1;
        g.setColor(hovered ? cell.GetHover() : cell.GetMain());
        g.fillRoundRect(box.x, box.y, width, height, cell.GetCornerArc(), cell.GetCornerArc());
        g.setColor(cell.GetBorder());
        g.drawRoundRect(box.x, box.y, width, height, cell.GetCornerArc(), cell.GetCornerArc());
    }
}
