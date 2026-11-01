package Screens.MainView;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

// Whatever fills the space under the tab bar: the Overview, a list of games, the series. It paints inside the area it is given and
// adds anything clickable to the HitMap
public interface BodyPainter {
    void Paint(Graphics2D g, Rectangle area, Point mouse, HitMap hits);
}
