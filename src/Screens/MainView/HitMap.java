package Screens.MainView;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

// Where the clickable things were drawn, so a press and a release can be matched to the same thing. Components add their areas as
// they paint, each under a key that stays the same from one paint to the next (the areas themselves move when the window does)
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

    public void Clear(){
        regions.clear();
    }

    public void Add(String key, Rectangle bounds, Runnable action){
        regions.add(new Region(key, new Rectangle(bounds), action));
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

    // Where a key was last drawn, or null
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
