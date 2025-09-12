package Model;

import java.util.List;
import java.util.ArrayList;
import Model.TextBox;
import Model.GameReview;

public class TextBlock {
    // This class will be a collection of textbox objects and how they need to be ordered or displayed
    // In a page, this is the object that will be called 
    private List<TextBox> listOfText;
    public TextBlock(){
        // I need to parse the list of strings and create a text box for each one
        
    }

    public List<TextBox> GetStrings(List<GameReview> review, int num){
        List<TextBox> result = new ArrayList<TextBox>();

        return result;
    }

    public List<TextBox> GetStrings(List<String> text){
        List<TextBox> result = new ArrayList<TextBox>();

        return result;
    }

}
