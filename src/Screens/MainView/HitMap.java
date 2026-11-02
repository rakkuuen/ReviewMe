package Screens.MainView;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

// Where the clickable things were drawn, so a press and a release can be matched to the same thing. Components add their areas as
// they paint, each under a key that stays the same from one paint to the next (the areas themselves move when the window does).
// While a frame is set (for content that scrolls), areas are given in the content's own coordinates: they are moved into the window
// and cut to what is visible, so nothing scrolled out of sight can be clicked
public class HitMap {

    private static class Region {
        private final String key;
        private final Rectangle bounds;
        private final Runnable action;

        private Region(String key, Rectangle bounds, Runnable action){
            this.key = key;
            this.bounds = bounds;
            this.action = action;
        }
    }

    private final List<Region> regions = new ArrayList<>();
    private int offsetX, offsetY;
    private Rectangle visible;   // Null when no frame is set

    // How many areas there are now, so what a paint added after this can be thrown away again with TruncateTo
    public int Size(){
        return regions.size();
    }

    public void TruncateTo(int size){
        while(regions.size() > size){
            regions.remove(regions.size() - 1);
        }
    }

    public void Clear(){
        regions.clear();
        ClearFrame();
    }

    // From now on areas added are in content coordinates: moved by (offsetX, offsetY) into the window, and cut to the visible rectangle
    public void SetFrame(int offsetX, int offsetY, Rectangle visible){
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.visible = new Rectangle(visible);
    }

    public void ClearFrame(){
        offsetX = 0;
        offsetY = 0;
        visible = null;
    }

    public void Add(String key, Rectangle bounds, Runnable action){
        Rectangle placed = new Rectangle(bounds);
        if(visible != null){
            placed.translate(offsetX, offsetY);
            placed = placed.intersection(visible);
            if(placed.isEmpty()){
                return;
            }
        }
        regions.add(new Region(key, placed, action));
    }

    // The key of the thing at that point, or null for empty space. The first one added wins where they overlap
    public String KeyAt(Point point){
        for(Region region : regions){
            if(region.bounds.contains(point)){
                return region.key;
            }
        }
        return null;
    }

    // Where a key was last drawn (in the window, and only the part that is visible), or null
    public Rectangle BoundsOf(String key){
        for(Region region : regions){
            if(region.key.equals(key)){
                return new Rectangle(region.bounds);
            }
        }
        return null;
    }

    // Does what that key does. False if nothing is drawn under it
    public boolean Run(String key){
        for(Region region : regions){
            if(region.key.equals(key)){
                region.action.run();
                return true;
            }
        }
        return false;
    }
}
