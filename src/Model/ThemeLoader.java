package Model;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// Builds Theme instances - either the built-in default look, or parsed from a .properties file
public class ThemeLoader {

    // Matches the app's original hardcoded look, so nothing changes until a theme is loaded
    public static Theme LoadDefault(){
        ButtonTheme button = new ButtonTheme(Color.BLUE, Color.RED, Color.GRAY, Color.WHITE, Color.BLACK,
                10, new Font("Arial", Font.BOLD, 18));

        CellTheme cell = new CellTheme(Color.WHITE, Color.GRAY, Color.BLACK, Color.BLACK,
                10, new Font("Arial", Font.BOLD, 18));

        FieldTheme field = new FieldTheme(Color.WHITE, new Color(235, 235, 235), Color.BLACK,
                new Color(30, 120, 220), Color.GRAY,
                Color.BLACK, new Color(184, 207, 229), Color.BLACK, Color.BLACK,
                10, new Font("Arial", Font.PLAIN, 13));

        TextStyle title = new TextStyle(Color.BLACK, new Font("Arial", Font.BOLD, 32));
        TextStyle heading = new TextStyle(Color.BLACK, new Font("Arial", Font.BOLD, 14));

        Color background = new Color(238, 238, 238); // Matches Swing's usual default panel grey

        return new Theme(button, cell, field, title, heading, background);
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

        TextStyle title = new TextStyle(ParseColor(props, "title.text"), ParseFont(props, "title.font", embeddedFonts));
        TextStyle heading = new TextStyle(ParseColor(props, "heading.text"), ParseFont(props, "heading.font", embeddedFonts));

        Color background = ParseColor(props, "background");

        return new Theme(button, cell, field, title, heading, background);
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
