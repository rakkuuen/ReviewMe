package Model;

import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

// A real Swing text component reskinned to match GameCell/Button's rounded-rect look,
// so editable review fields keep Swing's built-in text editing (caret, selection,
// copy/paste, word-wrap) instead of it being hand-rolled.
public class EditableField extends JTextArea {
    public static final int archWAndH = 10;
    Color mainColour;
    Color borderColour;
    Color focusedBorderColour;
    Color placeholderColour;
    String placeholderText;

    // initialText is the field's real starting value. If null, there's no value yet,
    // so placeholderText is shown as a hint instead (not actual editable content) and
    // disappears the moment real text is typed.
    public EditableField(String initialText, String placeholderText, int locX, int locY, int sizeX, int sizeY){
        super(initialText == null ? "" : initialText);
        setBounds(locX, locY, sizeX, sizeY);

        this.placeholderText = initialText == null ? placeholderText : null;

        mainColour = Color.WHITE;
        borderColour = Color.BLACK;
        focusedBorderColour = new Color(30, 120, 220);
        placeholderColour = Color.GRAY;

        setLineWrap(true);
        setWrapStyleWord(true);
        setFont(new Font("Arial", Font.PLAIN, 13));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) { repaint(); }
            @Override
            public void focusLost(FocusEvent e) { repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(mainColour);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), archWAndH, archWAndH);
        g2.dispose();
        super.paintComponent(g);

        if(placeholderText != null && getText().isEmpty()){
            Graphics2D placeholderG2 = (Graphics2D) g.create();
            placeholderG2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            placeholderG2.setColor(placeholderColour);
            placeholderG2.setFont(getFont().deriveFont(Font.ITALIC));
            Insets insets = getInsets();
            FontMetrics fm = placeholderG2.getFontMetrics();
            placeholderG2.drawString(placeholderText, insets.left, insets.top + fm.getAscent());
            placeholderG2.dispose();
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(hasFocus() ? focusedBorderColour : borderColour);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, archWAndH, archWAndH);
        g2.dispose();
    }

    public String GetValue(){
        return getText();
    }

    public void SetValue(String value){
        setText(value == null ? "" : value);
    }
}
