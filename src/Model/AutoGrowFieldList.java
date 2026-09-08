package Model;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// A scrollable, top-to-bottom list of labeled EditableFields that each grow to fit their
// own text, reflowing everything below whenever one of them changes height.
public class AutoGrowFieldList {
    private static class FieldRow {
        JLabel headingLabel;
        EditableField field;
    }
    private List<FieldRow> fieldRows = new ArrayList<>();

    private JPanel fieldsContainer;
    private JScrollPane scrollPane;

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

        fieldsContainer = new JPanel();
        fieldsContainer.setLayout(null);

        // GridBagLayout centers the fixed-width fieldsContainer horizontally; NORTH keeps it top-anchored
        JPanel centeringWrapper = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        centeringWrapper.add(fieldsContainer, gbc);

        scrollPane = new JScrollPane(centeringWrapper,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
        headingLabel.setFont(new Font("Arial", Font.BOLD, 14));
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

        fieldsContainer.setPreferredSize(new Dimension(contentWidth, y));
        fieldsContainer.revalidate();
    }

    private int ComputePreferredHeight(EditableField field){
        field.setSize(fieldWidth, Short.MAX_VALUE);
        int preferred = field.getPreferredSize().height;
        return Math.max(minFieldHeight, preferred);
    }

    public JScrollPane GetComponent(){
        return scrollPane;
    }

    public void SetFieldsEditable(boolean editable){
        for(FieldRow row : fieldRows){
            row.field.SetEditingEnabled(editable);
        }
    }

    public void ScrollToTop(){
        // Force scroll back to top; adding fields can leave it auto-scrolled to the bottom
        SwingUtilities.invokeLater(() -> scrollPane.getViewport().setViewPosition(new Point(0, 0)));
    }
}
