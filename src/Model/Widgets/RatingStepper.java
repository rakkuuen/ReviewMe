package Model.Widgets;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import Model.Reviews.RatingRules;
import Model.Themes.ButtonTheme;
import Model.Themes.FieldTheme;
import Model.Themes.Theme;

// A rating box: - and + around a number you can step or type, and an Unsure (?) switch. Only what RatingRules
// allows can be typed, so it can't hold a bad rating. Read-only, it's just the number (or ?) in its pill
public class RatingStepper extends JPanel implements ValueBox {
    public static final int boxHeight = 44;

    private enum Part { NONE, MINUS, PLUS, SWITCH }

    private static final int stepWidth = 34;
    private static final int stepHeight = 32;
    private static final int entryMinWidth = 76;   // The pill grows past this for long values
    private static final int gap = 8;
    private static final int switchWidth = 42;
    private static final int switchHeight = 20;
    private static final int knobSize = 14;
    private static final int entryPadding = 10;
    private static final String minusGlyph = String.valueOf((char) 0x2212);   // A real minus sign, by code point so the source stays plain ASCII
    private static final String plusGlyph = "+";
    private static final String switchLabel = "Unsure (?)";
    private static final String normalHint = "any whole number or " + RatingRules.unknown;

    // Everything right of the minus button moves as the pill grows or slides left (read-only), see LayoutParts
    private final Rectangle minusRect = new Rectangle(0, (boxHeight - stepHeight) / 2, stepWidth, stepHeight);
    private final Rectangle entryRect = new Rectangle(stepWidth + gap, 0, entryMinWidth, boxHeight);
    private final Rectangle plusRect = new Rectangle(0, (boxHeight - stepHeight) / 2, stepWidth, stepHeight);
    private final Rectangle switchRect = new Rectangle(0, (boxHeight - switchHeight) / 2, switchWidth, switchHeight);

    private final JTextField entry = new JTextField();
    private String lastValid = "";    // The last value that wasn't a lone "-"
    private String lastNumber = "";   // What Unsure goes back to when it's switched off
    private boolean settingValue = false;
    private boolean editing = true;
    private Part hovered = Part.NONE;
    private Part pressed = Part.NONE;

    public RatingStepper(String initialValue, int locX, int locY, int width){
        setLayout(null);
        setOpaque(false);
        setBounds(locX, locY, width, boxHeight);

        entry.setOpaque(false);
        entry.setBorder(BorderFactory.createEmptyBorder());
        entry.setHorizontalAlignment(JTextField.CENTER);
        PlaceEntry();
        ((AbstractDocument) entry.getDocument()).setDocumentFilter(new TypingFilter());
        entry.addFocusListener(new FocusAdapter(){
            @Override
            public void focusLost(FocusEvent e){
                if(entry.getText().equals("-")){
                    SetValue(lastValid);   // A minus on its own isn't a rating
                }
                repaint();
            }
            @Override
            public void focusGained(FocusEvent e){ repaint(); }
        });
        entry.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode() == KeyEvent.VK_UP){
                    Step(1);
                } else if(e.getKeyCode() == KeyEvent.VK_DOWN){
                    Step(-1);
                }
            }
        });
        add(entry);

        MouseAdapter mouse = new MouseAdapter(){
            @Override
            public void mouseMoved(MouseEvent e){ SetHovered(PartAt(e.getPoint())); }
            @Override
            public void mouseExited(MouseEvent e){ SetHovered(Part.NONE); }
            @Override
            public void mousePressed(MouseEvent e){ pressed = PartAt(e.getPoint()); }
            @Override
            public void mouseReleased(MouseEvent e){
                Part released = PartAt(e.getPoint());
                if(released != Part.NONE && released == pressed){
                    Activate(released);
                }
                pressed = Part.NONE;
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        ReapplyTheme();
        SetValue(initialValue);
    }

    @Override
    public String GetValue(){
        String text = entry.getText();
        return text.equals("-") ? lastValid : text;
    }

    @Override
    public void SetValue(String value){
        settingValue = true;
        entry.setText(value == null ? "" : value);
        settingValue = false;
        repaint();
    }

    public boolean IsEditingEnabled(){
        return editing;
    }

    public void SetEditingEnabled(boolean enabled){
        editing = enabled;
        PlaceEntry();
        entry.setEditable(enabled);
        entry.setFocusable(enabled);
        SetHovered(Part.NONE);
        repaint();
    }

    // The pill is as wide as its text needs. It sits between - and + while editing, and at the left edge when it's
    // just showing the number; + and the switch follow it
    private void PlaceEntry(){
        int textWidth = entry.getFontMetrics(entry.getFont()).stringWidth(entry.getText());
        entryRect.width = Math.max(entryMinWidth, textWidth + entryPadding * 2 + 6);
        entryRect.x = editing ? stepWidth + gap : 0;
        entry.setBounds(entryRect.x + entryPadding, entryRect.y + 4, entryRect.width - entryPadding * 2, entryRect.height - 8);

        plusRect.x = entryRect.x + entryRect.width + gap;
        switchRect.x = plusRect.x + stepWidth + gap * 2;
    }

    // Says what an out-of-range number counts as, since the box shows exactly what was typed
    private String HintText(){
        String value = GetValue();
        return RatingRules.CountsAsDifferentNumber(value) ? "counts as " + RatingRules.ToNumber(value) + " when sorting" : normalHint;
    }

    public void ReapplyTheme(){
        FieldTheme theme = Theme.Current.GetField();
        Font font = theme.GetFont();
        entry.setFont(font.deriveFont(Font.BOLD, font.getSize2D() + 5f));
        entry.setForeground(theme.GetText());
        entry.setCaretColor(theme.GetCaret());
        entry.setSelectionColor(theme.GetSelection());
        entry.setSelectedTextColor(theme.GetSelectedText());
        PlaceEntry();   // A new font changes how wide the text is
        repaint();
    }

    // Called once per whole edit (a replace is a remove then an insert, and the half-way text would look like a cleared box)
    private void TextChanged(){
        String text = entry.getText();
        if(!text.equals("-")){
            lastValid = text;
        }
        if(text.isEmpty() || RatingRules.ToNumber(text) != null){
            lastNumber = text;   // Blank counts: Unsure off after clearing the box goes back to blank
        }
        PlaceEntry();
        repaint();
    }

    private void Step(int delta){
        SetValue(RatingRules.Step(GetValue(), delta));
    }

    private boolean IsUnknown(){
        return GetValue().equals(RatingRules.unknown);
    }

    private void ToggleUnknown(){
        SetValue(IsUnknown() ? lastNumber : RatingRules.unknown);
    }

    private void Activate(Part part){
        switch(part){
            case MINUS: Step(-1); break;
            case PLUS: Step(1); break;
            case SWITCH: ToggleUnknown(); break;
            default: break;
        }
    }

    private Part PartAt(Point p){
        if(!editing){
            return Part.NONE;
        }
        if(minusRect.contains(p) && RatingRules.CanStep(GetValue(), -1)){
            return Part.MINUS;
        }
        if(plusRect.contains(p) && RatingRules.CanStep(GetValue(), 1)){
            return Part.PLUS;
        }
        return switchRect.contains(p) ? Part.SWITCH : Part.NONE;
    }

    private void SetHovered(Part part){
        if(hovered != part){
            hovered = part;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FieldTheme field = Theme.Current.GetField();

        g2.setColor(editing ? field.GetBackground() : field.GetReadOnly());
        g2.fillRoundRect(entryRect.x, entryRect.y, entryRect.width, entryRect.height, boxHeight, boxHeight);
        g2.setColor(entry.hasFocus() ? field.GetBorderFocused() : field.GetBorder());
        g2.drawRoundRect(entryRect.x, entryRect.y, entryRect.width - 1, entryRect.height - 1, boxHeight, boxHeight);

        if(editing){
            DrawStepButton(g2, minusRect, minusGlyph, Part.MINUS, RatingRules.CanStep(GetValue(), -1));
            DrawStepButton(g2, plusRect, plusGlyph, Part.PLUS, RatingRules.CanStep(GetValue(), 1));
            DrawSwitch(g2, field);
        }
        g2.dispose();
    }

    private void DrawStepButton(Graphics2D g2, Rectangle bounds, String glyph, Part part, boolean enabled){
        ButtonTheme theme = Theme.Current.GetButton();
        if(!enabled){
            g2.setColor(theme.GetDisabled());
        } else if(hovered == part){
            g2.setColor(theme.GetHover());
        } else {
            g2.setColor(theme.GetMain());
        }
        int arc = theme.GetCornerArc();
        g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, arc, arc);
        g2.setColor(theme.GetBorder());
        g2.drawRoundRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, arc, arc);

        // Centred on the glyph's ink, same as Button, so + and the minus sit in the middle whatever the font
        Font font = theme.GetFont();
        g2.setFont(font);
        g2.setColor(theme.GetText());
        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D ink = font.createGlyphVector(frc, glyph).getVisualBounds();
        int textX = (int) Math.round(bounds.getCenterX() - ink.getWidth() / 2 - ink.getX());
        int textY = (int) Math.round(bounds.getCenterY() - ink.getHeight() / 2 - ink.getY());
        g2.drawString(glyph, textX, textY);
    }

    private void DrawSwitch(Graphics2D g2, FieldTheme field){
        boolean on = IsUnknown();
        g2.setColor(on ? field.GetBorderFocused() : field.GetReadOnly());
        g2.fillRoundRect(switchRect.x, switchRect.y, switchRect.width, switchRect.height, switchHeight, switchHeight);
        g2.setColor(hovered == Part.SWITCH ? field.GetBorderFocused() : field.GetBorder());
        g2.drawRoundRect(switchRect.x, switchRect.y, switchRect.width - 1, switchRect.height - 1, switchHeight, switchHeight);

        int knobY = switchRect.y + (switchHeight - knobSize) / 2;
        int knobX = on ? switchRect.x + switchWidth - knobSize - 3 : switchRect.x + 3;
        g2.setColor(on ? field.GetBackground() : field.GetPlaceholder());
        g2.fillOval(knobX, knobY, knobSize, knobSize);

        g2.setFont(field.GetFont());
        FontMetrics metrics = g2.getFontMetrics();
        int baseline = (boxHeight + metrics.getAscent() - metrics.getDescent()) / 2;
        int labelX = switchRect.x + switchWidth + gap;
        g2.setColor(field.GetText());
        g2.drawString(switchLabel, labelX, baseline);
        g2.setColor(field.GetPlaceholder());
        g2.drawString(HintText(), labelX + metrics.stringWidth(switchLabel) + gap * 2, baseline);
    }

    // Refuses any edit that would leave text that isn't on the way to a valid rating
    private class TypingFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs) throws BadLocationException {
            replace(fb, offset, 0, text, attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            replace(fb, offset, length, "", null);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String result = new StringBuilder(current).replace(offset, offset + length, text == null ? "" : text).toString();
            if(settingValue || RatingRules.IsTypable(result)){
                super.replace(fb, offset, length, text, attrs);
                TextChanged();
            }
        }
    }
}
