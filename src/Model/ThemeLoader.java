package Model;

import java.awt.Color;
import java.awt.Font;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

// Builds Theme instances - either the built-in default look, or parsed from a .properties file
public class ThemeLoader {

    // Matches the app's original hardcoded look, so nothing changes until a theme is loaded
    public static Theme LoadDefault(){
        ButtonTheme button = new ButtonTheme(Color.BLUE, Color.RED, Color.GRAY, Color.WHITE, Color.BLACK,
                new Font("Arial", Font.BOLD, 18));

        CellTheme cell = new CellTheme(Color.WHITE, Color.GRAY, Color.BLACK, Color.BLACK,
                new Font("Arial", Font.BOLD, 18));

        FieldTheme field = new FieldTheme(Color.WHITE, new Color(235, 235, 235), Color.BLACK,
                new Color(30, 120, 220), Color.GRAY,
                Color.BLACK, new Color(184, 207, 229), Color.BLACK, Color.BLACK,
                new Font("Arial", Font.PLAIN, 13));

        TextStyle title = new TextStyle(Color.BLACK, new Font("Arial", Font.BOLD, 32));
        TextStyle heading = new TextStyle(Color.BLACK, new Font("Arial", Font.BOLD, 14));

        Color background = new Color(238, 238, 238); // Matches Swing's usual default panel grey

        return new Theme(button, cell, field, title, heading, background);
    }

    public static Theme Load(String propertiesFilePath) throws IOException {
        Properties props = new Properties();
        try(FileInputStream in = new FileInputStream(propertiesFilePath)){
            props.load(in);
        }

        ButtonTheme button = new ButtonTheme(
                ParseColor(props, "button.main"), ParseColor(props, "button.hover"),
                ParseColor(props, "button.disabled"), ParseColor(props, "button.text"),
                ParseColor(props, "button.border"), ParseFont(props, "button.font"));

        CellTheme cell = new CellTheme(
                ParseColor(props, "cell.main"), ParseColor(props, "cell.hover"),
                ParseColor(props, "cell.text"), ParseColor(props, "cell.border"),
                ParseFont(props, "cell.font"));

        FieldTheme field = new FieldTheme(
                ParseColor(props, "field.background"), ParseColor(props, "field.readOnly"),
                ParseColor(props, "field.border"), ParseColor(props, "field.borderFocused"),
                ParseColor(props, "field.placeholder"),
                ParseColor(props, "field.caret"), ParseColor(props, "field.selection"), ParseColor(props, "field.selectedText"),
                ParseColor(props, "field.text"),
                ParseFont(props, "field.font"));

        TextStyle title = new TextStyle(ParseColor(props, "title.text"), ParseFont(props, "title.font"));
        TextStyle heading = new TextStyle(ParseColor(props, "heading.text"), ParseFont(props, "heading.font"));

        Color background = ParseColor(props, "background");

        return new Theme(button, cell, field, title, heading, background);
    }

    private static Color ParseColor(Properties props, String key){
        return Color.decode(props.getProperty(key));
    }

    // Format: family,style,size e.g. "Arial,BOLD,18"
    private static Font ParseFont(Properties props, String key){
        String[] parts = props.getProperty(key).split(",");
        String family = parts[0].trim();
        int style = ParseFontStyle(parts[1].trim());
        int size = Integer.parseInt(parts[2].trim());
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
