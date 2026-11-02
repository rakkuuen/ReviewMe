package Screens.SeriesView;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

// Breaks text into lines no wider than a width: at the spaces where it can, and in the middle of a word only when one word is
// wider than the whole line. A line break in the text starts a new line, and a blank line is kept
public class TextWrap {

    public static List<String> Lines(FontMetrics metrics, String text, int width){
        List<String> lines = new ArrayList<>();
        for(String paragraph : text.replace("\r\n", "\n").split("\n", -1)){
            if(paragraph.trim().isEmpty()){
                lines.add("");
                continue;
            }
            StringBuilder line = new StringBuilder();
            for(String word : paragraph.trim().split("\\s+")){
                String joined = line.length() == 0 ? word : line + " " + word;
                if(metrics.stringWidth(joined) <= width){
                    line = new StringBuilder(joined);
                    continue;
                }
                if(line.length() > 0){
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
                String rest = word;
                while(metrics.stringWidth(rest) > width && rest.length() > 1){
                    int keep = rest.length() - 1;
                    while(keep > 1 && metrics.stringWidth(rest.substring(0, keep)) > width){
                        keep--;
                    }
                    lines.add(rest.substring(0, keep));
                    rest = rest.substring(keep);
                }
                line = new StringBuilder(rest);
            }
            lines.add(line.toString());
        }
        return lines;
    }
}
