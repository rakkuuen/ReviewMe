package Model.Themes;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

// Builds Theme instances parsed from a .properties file
public class ThemeLoader {
    private static final String defaultThemePath = "Resources/Themes/default.properties";

    // The theme the app starts with, read from the default theme file (the look lives in that file, not duplicated in code).
    // Fails loudly if the file is missing or broken
    public static Theme LoadDefault(){
        try {
            return Load(defaultThemePath);
        } catch (IOException | FontFormatException e) {
            throw new IllegalStateException("Can't load the default theme " + defaultThemePath + ": " + e.getMessage(), e);
        }
    }

    public static Theme Load(String propertiesFilePath) throws IOException, FontFormatException {
        Properties props = new Properties();
        try(FileInputStream in = new FileInputStream(propertiesFilePath)){
            props.load(in);
        }

        Map<String, Font> embeddedFonts = LoadEmbeddedFonts(props);

        ButtonTheme button = new ButtonTheme(
                ParseColor(props, "button.main"), ParseColor(props, "button.hover"),
                ParseColor(props, "button.disabled"), ParseColor(props, "button.text"),
                ParseColor(props, "button.border"), ParseInt(props, "button.cornerArc"),
                ParseFont(props, "button.font", embeddedFonts));

        CellTheme cell = new CellTheme(
                ParseColor(props, "cell.main"), ParseColor(props, "cell.hover"),
                ParseColor(props, "cell.text"), ParseColor(props, "cell.border"),
                ParseInt(props, "cell.cornerArc"), ParseFont(props, "cell.font", embeddedFonts));

        FieldTheme field = new FieldTheme(
                ParseColor(props, "field.background"), ParseColor(props, "field.readOnly"),
                ParseColor(props, "field.border"), ParseColor(props, "field.borderFocused"),
                ParseColor(props, "field.placeholder"),
                ParseColor(props, "field.caret"), ParseColor(props, "field.selection"), ParseColor(props, "field.selectedText"),
                ParseColor(props, "field.text"), ParseInt(props, "field.cornerArc"),
                ParseFont(props, "field.font", embeddedFonts));

        NavTheme nav = new NavTheme(
                ParseColor(props, "nav.main"), ParseColor(props, "nav.hover"), ParseColor(props, "nav.selected"),
                ParseColor(props, "nav.text"), ParseColor(props, "nav.selectedText"), ParseColor(props, "nav.disabledText"),
                ParseColor(props, "nav.border"), ParseColor(props, "nav.countBackground"), ParseColor(props, "nav.countText"),
                ParseInt(props, "nav.cornerArc"), ParseFont(props, "nav.font", embeddedFonts));

        TextStyle title = new TextStyle(ParseColor(props, "title.text"), ParseFont(props, "title.font", embeddedFonts));
        TextStyle heading = new TextStyle(ParseColor(props, "heading.text"), ParseFont(props, "heading.font", embeddedFonts));

        RichTheme rich = LoadRich(props, embeddedFonts);

        Color background = ParseColor(props, "background");

        return new Theme(button, cell, field, nav, title, heading, rich, background);
    }

    // The colours and fonts of formatted text (rich.colour.<key> for each key in the palette)
    private static RichTheme LoadRich(Properties props, Map<String, Font> embeddedFonts){
        Map<String, Color> palette = new LinkedHashMap<>();
        for(String key : RichTheme.paletteKeys){
            palette.put(key, ParseColor(props, "rich.colour." + key));
        }
        return new RichTheme(palette, ParseColor(props, "rich.boldText"), ParseColor(props, "rich.link"), ParseColor(props, "rich.linkUnderline"),
                ParseColor(props, "rich.marker"), ParseFont(props, "rich.font." + RichTheme.serifKey, embeddedFonts),
                ParseFont(props, "rich.font." + RichTheme.monoKey, embeddedFonts), ParseFont(props, "rich.font.marker", embeddedFonts));
    }

    // Loads embedded fonts
    private static Map<String, Font> LoadEmbeddedFonts(Properties props) throws IOException, FontFormatException {
        Map<String, Font> embeddedFonts = new HashMap<>();
        String prefix = "font.embed.";

        for(String key : props.stringPropertyNames()){
            if(key.startsWith(prefix)){
                String name = key.substring(prefix.length());
                File fontFile = new File(props.getProperty(key));
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                embeddedFonts.put(name, baseFont);
            }
        }

        return embeddedFonts;
    }

    private static Color ParseColor(Properties props, String key){
        return Color.decode(props.getProperty(key));
    }

    private static int ParseInt(Properties props, String key){
        return Integer.parseInt(props.getProperty(key).trim());
    }

    // Format: family,style,size e.g. "Arial,BOLD,18" - family can also be a name
    // registered via font.embed.<name>, resolved against an embedded file instead
    // of a system-installed family
    private static Font ParseFont(Properties props, String key, Map<String, Font> embeddedFonts){
        String[] parts = props.getProperty(key).split(",");
        String family = parts[0].trim();
        int style = ParseFontStyle(parts[1].trim());
        int size = Integer.parseInt(parts[2].trim());

        Font embedded = embeddedFonts.get(family);
        if(embedded != null){
            return embedded.deriveFont(style, (float) size);
        }
        return new Font(family, style, size);
    }

    private static int ParseFontStyle(String style){
        switch(style.toUpperCase()){
            case "BOLD": return Font.BOLD;
            case "ITALIC": return Font.ITALIC;
            case "BOLD_ITALIC": return Font.BOLD | Font.ITALIC;
            default: return Font.PLAIN;
        }
    }
}
