package Screens.MainView;

import java.awt.FontMetrics;

// Makes text fit a width by cutting it short and ending it with "..."
public class TextFit {
    private static final String ellipsis = "...";

    // The text itself if it fits; otherwise as much of its start as fits with "..." after it. If not even the dots fit, nothing
    public static String Fit(FontMetrics metrics, String text, int maxWidth){
        if(metrics.stringWidth(text) <= maxWidth){
            return text;
        }
        int dots = metrics.stringWidth(ellipsis);
        if(dots > maxWidth){
            return "";
        }
        int keep = text.length();
        while(keep > 0 && metrics.stringWidth(text.substring(0, keep)) + dots > maxWidth){
            keep--;
        }
        return text.substring(0, keep).trim() + ellipsis;
    }
}
