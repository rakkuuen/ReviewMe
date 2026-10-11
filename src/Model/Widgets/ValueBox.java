package Model.Widgets;

// A box on the detail screen that holds one value as text: a text box or a rating box
public interface ValueBox {
    String GetValue();
    void SetValue(String value);
}
