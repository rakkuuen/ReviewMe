package Model.Themes;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// The look of formatted text: the colours a writer can pick (stored by name, so a theme can change what "amber" looks like without
// touching any review), the two other fonts, and the colours of bold text, links and list numbers
public class RichTheme {
    public static final List<String> paletteKeys = Collections.unmodifiableList(Arrays.asList("gold", "amber", "red", "green", "blue", "sand"));
    public static final String serifKey = "serif";
    public static final String monoKey = "mono";

    private final Map<String, Color> palette;
    private final Color boldText;
    private final Color link;
    private final Color linkUnderline;
    private final Color marker;
    private final Font serif;
    private final Font mono;
    private final Font markerFont;

    public RichTheme(Map<String, Color> palette, Color boldText, Color link, Color linkUnderline, Color marker, Font serif, Font mono, Font markerFont){
        this.palette = Collections.unmodifiableMap(new LinkedHashMap<>(palette));
        this.boldText = boldText;
        this.link = link;
        this.linkUnderline = linkUnderline;
        this.marker = marker;
        this.serif = serif;
        this.mono = mono;
        this.markerFont = markerFont;
    }

    // The colour for a stored key, or null when the theme has no such colour
    public Color GetPaletteColour(String key){ return palette.get(key); }
    public Color GetBoldText(){ return boldText; }
    public Color GetLink(){ return link; }
    public Color GetLinkUnderline(){ return linkUnderline; }
    public Color GetMarker(){ return marker; }
    public Font GetSerif(){ return serif; }
    public Font GetMono(){ return mono; }
    public Font GetMarkerFont(){ return markerFont; }

    public List<Color> GetAllColours(){
        List<Color> colours = new ArrayList<>(palette.values());
        colours.addAll(Arrays.asList(boldText, link, linkUnderline, marker));
        return colours;
    }

    public List<Font> GetAllFonts(){
        return Arrays.asList(serif, mono, markerFont);
    }
}
