package Model;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Central place every widget reads its look from. Widgets read Theme.Current at PAINT
// time rather than caching colours/fonts at construction, so swapping Current and firing
// one repaint changes the whole UI live.
public class Theme {
    public static Theme Current = ThemeLoader.LoadDefault();

    private ButtonTheme button;
    private CellTheme cell;
    private FieldTheme field;
    private TextStyle title;
    private TextStyle heading;

    public Theme(ButtonTheme button, CellTheme cell, FieldTheme field, TextStyle title, TextStyle heading){
        this.button = button;
        this.cell = cell;
        this.field = field;
        this.title = title;
        this.heading = heading;
    }

    public ButtonTheme GetButton(){ return button; }
    public CellTheme GetCell(){ return cell; }
    public FieldTheme GetField(){ return field; }
    public TextStyle GetTitle(){ return title; }
    public TextStyle GetHeading(){ return heading; }

    // For enumerating every colour/font at once (e.g. a future theme-editor screen)
    public List<Color> GetAllColours(){
        List<Color> colours = new ArrayList<>();
        colours.addAll(button.GetAllColours());
        colours.addAll(cell.GetAllColours());
        colours.addAll(field.GetAllColours());
        colours.addAll(title.GetAllColours());
        colours.addAll(heading.GetAllColours());
        return colours;
    }

    public List<Font> GetAllFonts(){
        return Arrays.asList(button.GetFont(), cell.GetFont(), field.GetFont(), title.GetFont(), heading.GetFont());
    }
}
