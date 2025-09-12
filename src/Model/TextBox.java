package Model;

public class TextBox {
    // If I want to show text on the screen (Not an area to type)
    private String displayText;
    // Type can be Heading, Paragraph, Sub-Heading, whatever else I see as useful
    private String textType;

    public TextBox(String displayText, String textType){
        this.displayText = displayText;
        this.textType = textType;
        
    }
}
