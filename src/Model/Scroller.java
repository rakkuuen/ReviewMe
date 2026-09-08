package Model;

import java.awt.Graphics;
import java.awt.Point;

// Reusable vertical scroll offset for custom-painted (non-Swing-component) screens.
// Handles clamping the offset to valid bounds, and translating painted content /
// mouse-position hit-tests to match, so each screen doesn't re-derive this math itself.
public class Scroller {
    private static final int defaultScrollStep = 40;

    private int scrollStep;
    private int offsetY;
    private int maxOffsetY;

    public Scroller(int contentHeight, int viewportHeight){
        this(contentHeight, viewportHeight, defaultScrollStep);
    }

    public Scroller(int contentHeight, int viewportHeight, int scrollStep){
        this.scrollStep = scrollStep;
        this.offsetY = 0;
        SetContentHeight(contentHeight, viewportHeight);
    }

    // Call again if the content height changes (e.g. items added/removed)
    public void SetContentHeight(int contentHeight, int viewportHeight){
        maxOffsetY = Math.max(0, contentHeight - viewportHeight);
        Clamp();
    }

    public void Scroll(int wheelRotation){
        offsetY += wheelRotation * scrollStep;
        Clamp();
    }

    private void Clamp(){
        if(offsetY < 0) offsetY = 0;
        if(offsetY > maxOffsetY) offsetY = maxOffsetY;
    }

    public int GetOffsetY(){
        return offsetY;
    }

    // Translates a real panel-space point (e.g. from a MouseEvent) into content space,
    // for hit-testing against items whose coordinates assume no scrolling
    public Point AdjustPoint(Point panelPoint){
        return new Point(panelPoint.x, panelPoint.y + offsetY);
    }

    // A Graphics context pre-translated for painting scrolled content - caller must dispose() it
    public Graphics ApplyTo(Graphics g){
        Graphics g2 = g.create();
        g2.translate(0, -offsetY);
        return g2;
    }
}
