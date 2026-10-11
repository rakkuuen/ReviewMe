package Model.Widgets;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import Model.Themes.Theme;

// A scrollable, top-to-bottom list of labeled boxes: text boxes that each grow to fit their own text, and
// fixed-height rating boxes. Everything below reflows whenever one changes height. No JScrollPane -
// content is repositioned by hand via Scroller (same offset/clamp math FrontPage uses),
// clipped for free since Swing already clips child components to their parent's bounds.
public class AutoGrowFieldList {
    private static class FieldRow {
        JLabel headingLabel;
        JComponent box;
        EditableField textBox;       // null for a rating row, whose height is fixed
        RatingStepper ratingBox;     // null for a text row
        boolean hasHeading;
        int minHeight;
        int level;   // 0 = a normal field, 1 = group, 2 = character inside a group
        int x;
        int width;
    }
    private List<FieldRow> fieldRows = new ArrayList<>();

    private static final int indentStep = 28;
    private static final int subRowGap = 10;           // Sub-boxes sit closer than top-level fields so a group reads as one unit
    private static final float subHeadingScale = 0.85f;

    private JPanel viewport;
    private JPanel centeringWrapper;
    private JPanel fieldsContainer;
    private Scroller scroller;
    private int contentHeight = 0;

    private final int fieldX;
    private final int fieldWidth;
    private final int headingHeight;
    private final int minFieldHeight;
    private final int rowGap;
    private final int contentWidth;

    public AutoGrowFieldList(int fieldX, int fieldWidth, int headingHeight, int minFieldHeight, int rowGap){
        this.fieldX = fieldX;
        this.fieldWidth = fieldWidth;
        this.headingHeight = headingHeight;
        this.minFieldHeight = minFieldHeight;
        this.rowGap = rowGap;
        this.contentWidth = fieldX * 2 + fieldWidth;

        scroller = new Scroller(0, 0);

        // These are pure layout containers with no look of their own - transparent so
        // Main.App's theme-driven canvas background shows through underneath them
        fieldsContainer = new JPanel();
        fieldsContainer.setLayout(null);
        fieldsContainer.setOpaque(false);

        // GridBagLayout centers the fixed-width fieldsContainer horizontally; NORTH keeps it top-anchored
        centeringWrapper = new JPanel(new GridBagLayout());
        centeringWrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        centeringWrapper.add(fieldsContainer, gbc);

        viewport = new JPanel();
        viewport.setLayout(null);
        viewport.setOpaque(false);
        viewport.add(centeringWrapper);

        viewport.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e){
                scroller.SetContentHeight(contentHeight, viewport.getHeight());
                UpdateContentPosition();
            }
        });

        viewport.addMouseWheelListener(new MouseWheelListener(){
            @Override
            public void mouseWheelMoved(MouseWheelEvent e){
                scroller.Scroll(e.getWheelRotation());
                UpdateContentPosition();
            }
        });
    }

    public EditableField AddField(String heading, String initialText, String placeholder){
        return AddField(heading, initialText, placeholder, minFieldHeight);
    }

    // minHeight is this row's floor (a comment box is shorter than a paragraph). An empty heading adds no heading space,
    // so a row can sit directly under the one above it
    public EditableField AddField(String heading, String initialText, String placeholder, int minHeight){
        return AddField(heading, initialText, placeholder, minHeight, 0);
    }

    // level indents the row from the left edge (the right edge stays put)
    public EditableField AddField(String heading, String initialText, String placeholder, int minHeight, int level){
        int rowX = fieldX + level * indentStep;
        int rowWidth = fieldWidth - level * indentStep;
        EditableField field = new EditableField(initialText, placeholder, rowX, 0, rowWidth, minHeight);
        field.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e){ Relayout(); }
            @Override
            public void removeUpdate(DocumentEvent e){ Relayout(); }
            @Override
            public void changedUpdate(DocumentEvent e){ Relayout(); }
        });

        FieldRow row = AddRow(heading, field, minHeight, level, rowX, rowWidth);
        row.textBox = field;
        return field;
    }

    // A rating row: -/+, a number you can type, and the Unsure switch. Always a top-level row at a fixed height
    public RatingStepper AddRating(String heading, String initialValue){
        RatingStepper stepper = new RatingStepper(initialValue, fieldX, 0, fieldWidth);
        FieldRow row = AddRow(heading, stepper, RatingStepper.boxHeight, 0, fieldX, fieldWidth);
        row.ratingBox = stepper;
        return stepper;
    }

    private FieldRow AddRow(String heading, JComponent box, int minHeight, int level, int rowX, int rowWidth){
        JLabel headingLabel = new JLabel(heading);
        fieldsContainer.add(headingLabel);
        fieldsContainer.add(box);

        FieldRow row = new FieldRow();
        row.headingLabel = headingLabel;
        row.box = box;
        row.hasHeading = heading != null && !heading.isEmpty();
        row.minHeight = minHeight;
        row.level = level;
        row.x = rowX;
        row.width = rowWidth;
        StyleHeading(row);
        headingLabel.setVisible(row.hasHeading);
        fieldRows.add(row);
        return row;
    }

    // Sub-level headings use the theme heading font, a touch smaller
    private void StyleHeading(FieldRow row){
        Font font = Theme.Current.GetHeading().GetFont();
        row.headingLabel.setFont(row.level == 0 ? font : font.deriveFont(font.getSize2D() * subHeadingScale));
        row.headingLabel.setForeground(Theme.Current.GetHeading().GetColour());
    }

    public void Relayout(){
        int y = 0;
        boolean first = true;
        for(FieldRow row : fieldRows){
            if(!first){
                y += row.level > 0 ? subRowGap : rowGap;
            }
            first = false;

            if(row.hasHeading){
                row.headingLabel.setBounds(row.x, y, row.width, headingHeight);
                y += headingHeight;
            }

            int boxHeight = ComputePreferredHeight(row);
            row.box.setBounds(row.x, y, row.width, boxHeight);
            y += boxHeight;
        }
        y += rowGap; // Breathing room under the last row

        contentHeight = y;
        fieldsContainer.setPreferredSize(new Dimension(contentWidth, contentHeight));
        fieldsContainer.revalidate();

        scroller.SetContentHeight(contentHeight, viewport.getHeight());
        UpdateContentPosition();
    }

    // Repositions centeringWrapper within viewport to reflect the current scroll offset;
    // Swing clips it to viewport's own bounds automatically, so nothing else is needed
    private void UpdateContentPosition(){
        centeringWrapper.setBounds(0, -scroller.GetOffsetY(), viewport.getWidth(), contentHeight);
    }

    private int ComputePreferredHeight(FieldRow row){
        if(row.textBox == null){
            return row.minHeight;
        }
        row.textBox.setSize(row.width, Short.MAX_VALUE);
        int preferred = row.textBox.getPreferredSize().height;
        return Math.max(row.minHeight, preferred);
    }

    public JPanel GetComponent(){
        return viewport;
    }

    public void SetFieldsEditable(boolean editable){
        for(FieldRow row : fieldRows){
            if(row.textBox != null){
                row.textBox.SetEditingEnabled(editable);
            } else {
                row.ratingBox.SetEditingEnabled(editable);
            }
        }
    }

    // Re-applies Theme.Current to every existing row (font/colour changes can shift
    // preferred heights, so this re-layouts afterward) - lets a live theme switch reach
    // fields already on screen without losing their typed content
    public void ReapplyTheme(){
        for(FieldRow row : fieldRows){
            if(row.textBox != null){
                row.textBox.ReapplyTheme();
            } else {
                row.ratingBox.ReapplyTheme();
            }
            StyleHeading(row);
        }
        Relayout();
    }

    public void ScrollToTop(){
        scroller.ScrollToTop();
        UpdateContentPosition();
    }
}
