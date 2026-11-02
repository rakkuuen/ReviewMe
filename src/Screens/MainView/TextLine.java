package Screens.MainView;

import java.awt.FontMetrics;

import Model.Layout.LayoutMath;

// Where to put a line of text
public class TextLine {

    // The line the text sits on, so one line of this font is centred in a box that starts at top and is height tall
    public static int Baseline(int top, int height, FontMetrics metrics){
        return top + LayoutMath.Centre(height, metrics.getHeight()) + metrics.getAscent();
    }
}
