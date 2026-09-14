package Model;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// A scrollable, top-to-bottom list of labeled EditableFields that each grow to fit their
// own text, reflowing everything below whenever one of them changes height. No JScrollPane -
// content is repositioned by hand via Scroller (same offset/clamp math FrontPage uses),
// clipped for free since Swing already clips child components to their parent's bounds.
public class AutoGrowFieldList {
    private static class FieldRow {
        JLabel headingLabel;
        EditableField field;
    }
    private List<FieldRow> fieldRows = new ArrayList<>();

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

        fieldsContainer = new JPanel();
        fieldsContainer.setLayout(null);

        // GridBagLayout centers the fixed-width fieldsContainer horizontally; NORTH keeps it top-anchored
        centeringWrapper = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        centeringWrapper.add(fieldsContainer, gbc);

        viewport = new JPanel();
        viewport.setLayout(null);
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
        EditableField field = new EditableField(initialText, placeholder, fieldX, 0, fieldWidth, minFieldHeight);
        field.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e){ Relayout(); }
            @Override
            public void removeUpdate(DocumentEvent e){ Relayout(); }
            @Override
            public void changedUpdate(DocumentEvent e){ Relayout(); }
        });

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(Theme.Current.GetHeading().GetFont());
        headingLabel.setForeground(Theme.Current.GetHeading().GetColour());
        fieldsContainer.add(headingLabel);
        fieldsContainer.add(field);

        FieldRow row = new FieldRow();
        row.headingLabel = headingLabel;
        row.field = field;
        fieldRows.add(row);

        return field;
    }

    public void Relayout(){
        int y = 0;
        for(FieldRow row : fieldRows){
            row.headingLabel.setBounds(fieldX, y, fieldWidth, headingHeight);
            y += headingHeight;

            int fieldHeight = ComputePreferredHeight(row.field);
            row.field.setBounds(fieldX, y, fieldWidth, fieldHeight);
            y += fieldHeight + rowGap;
        }

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

    private int ComputePreferredHeight(EditableField field){
        field.setSize(fieldWidth, Short.MAX_VALUE);
        int preferred = field.getPreferredSize().height;
        return Math.max(minFieldHeight, preferred);
    }

    public JPanel GetComponent(){
        return viewport;
    }

    public void SetFieldsEditable(boolean editable){
        for(FieldRow row : fieldRows){
            row.field.SetEditingEnabled(editable);
        }
    }

    public void ScrollToTop(){
        scroller.ScrollToTop();
        UpdateContentPosition();
    }
}
