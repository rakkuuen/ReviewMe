package Model.Widgets;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

// Measuring and wrapping text for the custom-painted parts of a screen (the title, the state line)
public class TextWrap {
    private static final String ellipsis = String.valueOf((char) 0x2026); // By code point so the source stays plain ASCII

    // FontMetrics normally needs a live Graphics context; this gets one without needing
    // an actual on-screen paint, so a layout can measure text independent of painting
    public static FontMetrics MetricsFor(Font font){
        BufferedImage scratch = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics scratchG = scratch.getGraphics();
        scratchG.setFont(font);
        FontMetrics fm = scratchG.getFontMetrics();
        scratchG.dispose();
        return fm;
    }

    // Greedy word-wrap: packs words onto a line until the next one would exceed maxWidth
    public static List<String> Wrap(String text, FontMetrics fm, int maxWidth){
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for(String word : text.split(" ")){
            String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
            if(currentLine.length() == 0 || fm.stringWidth(candidate) <= maxWidth){
                currentLine = new StringBuilder(candidate);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }
        if(currentLine.length() > 0){
            lines.add(currentLine.toString());
        }
        return lines;
    }

    // No more than maxLines lines. If there are more, the last one is cut short and ends with an ellipsis
    public static List<String> Limit(List<String> lines, int maxLines, FontMetrics fm, int maxWidth){
        if(lines.size() <= maxLines){
            return lines;
        }
        List<String> kept = new ArrayList<>(lines.subList(0, maxLines));
        kept.set(maxLines - 1, Fit(kept.get(maxLines - 1) + " " + ellipsis, fm, maxWidth));
        return kept;
    }

    // One line cut short with an ellipsis if it's wider than maxWidth
    public static String Fit(String text, FontMetrics fm, int maxWidth){
        if(fm.stringWidth(text) <= maxWidth){
            return text;
        }
        String body = text.endsWith(ellipsis) ? text.substring(0, text.length() - 1).stripTrailing() : text;
        while(!body.isEmpty() && fm.stringWidth(body + ellipsis) > maxWidth){
            body = body.substring(0, body.length() - 1);
        }
        return body.stripTrailing() + ellipsis;
    }
}
