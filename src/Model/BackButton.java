package Model;

import java.awt.Point;

// Standardized top-left "Back" button that owns its own click detection (press must
// land on it, release must still be on it) and fires onBack when actually clicked,
// instead of screens polling it for whether they were clicked.
public class BackButton extends Button {
    private static final int x = 20;
    private static final int y = 20;
    private static final int width = 100;
    private static final int height = 50;
    private static final int archWAndH = 10;

    private boolean pressed = false;
    private Runnable onBack;

    public BackButton(Runnable onBack){
        super(width, height, x, y, archWAndH, "Back");
        this.onBack = onBack;
    }

    public void HandlePress(Point mousePos){
        pressed = contains(mousePos);
    }

    public void HandleRelease(Point mousePos){
        if(pressed && contains(mousePos)){
            onBack.run();
        }
        pressed = false;
    }
}
