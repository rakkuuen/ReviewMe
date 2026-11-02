package Screens.MainView;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

// Whatever fills the space under the tab bar: the Overview, a list of games, the series. It paints in its own coordinates, with
// (0, 0) the top left of its content, inside a viewport of the given size; the screen scrolls it. It adds anything clickable to the
// HitMap and says how tall its content is, so the screen knows how far it can scroll. mouse is in the same coordinates
public interface BodyPainter {
    // Returns the height of all the content (at least the viewport's height if it does not need to scroll)
    int Paint(Graphics2D g, Rectangle viewport, Point mouse, HitMap hits);
}
