package Screens.Detail;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Model.Reviews.ReviewStatus;
import Model.Themes.Theme;
import Model.Widgets.TextWrap;

// The two states and what's missing, as plain wrapped text under the title. A placeholder for the themed badges
public class StateLine {
    private static final int maxLines = 3;
    private static final int gapBelowTitle = 6;
    private static final Color warningColour = new Color(0xE0903C); // The themes have no warning colour yet

    private String text = "";
    private boolean warning;
    private List<String> lines = new ArrayList<>();
    private int firstBaseline;
    private int lineHeight;
    private int bottom;

    public void Show(ReviewStatus status){
        text = StatusText.Describe(status);
        warning = status.CountsAsIncomplete();
    }

    // Wraps the text to the width and gives back the y just under it. titleBottom is where the title block ends
    public int Layout(int titleBottom, int availableWidth){
        FontMetrics metrics = TextWrap.MetricsFor(Theme.Current.GetField().GetFont());
        lines = TextWrap.Limit(TextWrap.Wrap(text, metrics, availableWidth), maxLines, metrics, availableWidth);
        lineHeight = metrics.getHeight();
        firstBaseline = titleBottom + gapBelowTitle + metrics.getAscent();
        bottom = titleBottom + gapBelowTitle + lines.size() * lineHeight;
        return bottom;
    }

    public void Paint(Graphics g, int windowWidth){
        g.setFont(Theme.Current.GetField().GetFont());
        g.setColor(warning ? warningColour : Theme.Current.GetHeading().GetColour());
        FontMetrics metrics = g.getFontMetrics();
        int y = firstBaseline;
        for(String line : lines){
            g.drawString(line, (windowWidth - metrics.stringWidth(line)) / 2, y);
            y += lineHeight;
        }
    }

    public int GetBottom(){ return bottom; }
    public String GetText(){ return text; }
    public boolean IsWarning(){ return warning; }
    public List<String> GetLines(){ return Collections.unmodifiableList(lines); }
}
