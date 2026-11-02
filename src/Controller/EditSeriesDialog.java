package Controller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Controller.BackEnd.SeriesEditing.EditSeriesForm;
import Controller.BackEnd.SeriesEditing.EditSeriesSaver;
import Controller.BackEnd.SeriesView.SeriesGameItem;
import Controller.BackEnd.SeriesView.SeriesViewData;
import Controller.BackEnd.SeriesView.SeriesViewLoader;
import Database.Dao.SeriesDao;
import Database.Dao.TemplateDao;
import Model.Reviews.Series;
import Model.Reviews.Template;

// The Edit series window: name (its tag shown, fixed), template, notes and the games with up, down and remove. The form's rules are
// EditSeriesForm's and the saving is EditSeriesSaver's; this shows them, asks to confirm anything that moves games, and calls
// onSaved after something was saved
class EditSeriesDialog {
    private static final String title = "Edit series";
    private static final int nameColumns = 24;
    private static final int notesRows = 6;
    private static final int gamesRows = 8;
    private static final int gap = 8;

    static void Show(Component parent, String seriesId, Runnable onSaved){
        Series series = SeriesDao.GetSeries(seriesId);
        SeriesViewData page = SeriesViewLoader.Load(seriesId);
        if(series == null || page == null){
            return;
        }
        List<String> titles = new ArrayList<>();
        for(SeriesGameItem item : page.GetGames()){
            titles.add(item.GetRow().GetTitle());
        }
        Map<String, String> templates = new LinkedHashMap<>();
        for(Template template : TemplateDao.GetAllTemplates()){
            templates.put(template.GetId(), template.GetName());
        }
        EditSeriesForm form = new EditSeriesForm(series, titles, SeriesDao.GetAllSeries(), templates);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, JDialog.ModalityType.APPLICATION_MODAL);
        JTextField nameField = new JTextField(form.GetName(), nameColumns);
        JLabel problems = new JLabel(" ");
        List<String> ids = new ArrayList<>(templates.keySet());
        JComboBox<String> templateBox = new JComboBox<>(templates.values().toArray(new String[0]));
        templateBox.setSelectedIndex(Math.max(0, ids.indexOf(form.GetTemplateId())));
        JTextArea notes = new JTextArea(form.GetNotes(), notesRows, nameColumns);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> games = new JList<>(listModel);
        games.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        games.setVisibleRowCount(gamesRows);
        JButton up = new JButton("Up"), down = new JButton("Down"), remove = new JButton("Remove");
        JButton cancel = new JButton("Cancel"), save = new JButton("Save series");

        Runnable refresh = () -> {
            form.SetName(nameField.getText());
            form.SetTemplateId(ids.isEmpty() ? null : ids.get(templateBox.getSelectedIndex()));
            form.SetNotes(notes.getText());
            String selected = games.getSelectedValue();
            int selectedIndex = games.getSelectedIndex();
            listModel.clear();
            for(int i = 0; i < form.GetTitles().size(); i++){
                listModel.addElement((i + 1) + ". " + form.GetTitles().get(i));
            }
            if(selectedIndex >= 0 && selectedIndex < listModel.size()){
                games.setSelectedIndex(selectedIndex);
            }
            String current = SelectedTitle(games, form);
            up.setEnabled(current != null && form.CanMoveUp(current));
            down.setEnabled(current != null && form.CanMoveDown(current));
            remove.setEnabled(current != null);
            List<String> found = new ArrayList<>(form.GetProblems());
            if(form.GetCounter() != null){
                found.add(form.GetCounter());
            }
            problems.setText(found.isEmpty() ? " " : String.join("; ", found));
            save.setEnabled(form.CanSave());
        };
        DocumentListener typing = new DocumentListener(){
            @Override public void insertUpdate(DocumentEvent e){ refresh.run(); }
            @Override public void removeUpdate(DocumentEvent e){ refresh.run(); }
            @Override public void changedUpdate(DocumentEvent e){ refresh.run(); }
        };
        nameField.getDocument().addDocumentListener(typing);
        notes.getDocument().addDocumentListener(typing);
        templateBox.addActionListener(e -> refresh.run());
        games.addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()){
                String current = SelectedTitle(games, form);
                up.setEnabled(current != null && form.CanMoveUp(current));
                down.setEnabled(current != null && form.CanMoveDown(current));
                remove.setEnabled(current != null);
            }
        });
        up.addActionListener(e -> Move(games, form, true, refresh));
        down.addActionListener(e -> Move(games, form, false, refresh));
        remove.addActionListener(e -> {
            String current = SelectedTitle(games, form);
            if(current != null){
                form.Remove(current);
                refresh.run();
            }
        });
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            String ask = EditSeriesSaver.Confirmation(form);
            if(ask != null && JOptionPane.showConfirmDialog(dialog, ask, "Save changes?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION){
                return;
            }
            List<String> errors = EditSeriesSaver.Save(form);
            if(!errors.isEmpty()){
                JOptionPane.showMessageDialog(dialog, String.join("\n", errors), title, JOptionPane.WARNING_MESSAGE);
            }
            dialog.dispose();
            onSaved.run();
        });

        JPanel fields = new JPanel(new GridLayout(0, 1, gap, gap));
        fields.add(new JLabel("Name"));
        fields.add(nameField);
        fields.add(new JLabel(form.GetTagLine() + "   (renaming changes the display name only)"));
        fields.add(problems);
        fields.add(new JLabel("Template (games in this series use it)"));
        fields.add(templateBox);
        JPanel gameButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, gap));
        gameButtons.add(up);
        gameButtons.add(down);
        gameButtons.add(remove);
        JPanel gamesPanel = new JPanel(new BorderLayout(gap, gap));
        gamesPanel.add(new JLabel("Games in this series"), BorderLayout.NORTH);
        gamesPanel.add(new JScrollPane(games), BorderLayout.CENTER);
        gamesPanel.add(gameButtons, BorderLayout.SOUTH);
        JPanel notesPanel = new JPanel(new BorderLayout(gap, gap));
        notesPanel.add(new JLabel("Notes"), BorderLayout.NORTH);
        notesPanel.add(new JScrollPane(notes), BorderLayout.CENTER);
        JPanel middle = new JPanel(new GridLayout(0, 1, gap, gap));
        middle.add(notesPanel);
        middle.add(gamesPanel);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, gap, gap));
        buttons.add(cancel);
        buttons.add(save);
        JPanel content = new JPanel(new BorderLayout(gap, gap));
        content.setBorder(new EmptyBorder(gap, gap, gap, gap));
        content.add(fields, BorderLayout.NORTH);
        content.add(middle, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        refresh.run();
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    // The title of the game picked in the list, or null when none is
    private static String SelectedTitle(JList<String> games, EditSeriesForm form){
        int at = games.getSelectedIndex();
        return at >= 0 && at < form.GetTitles().size() ? form.GetTitles().get(at) : null;
    }

    private static void Move(JList<String> games, EditSeriesForm form, boolean upwards, Runnable refresh){
        String current = SelectedTitle(games, form);
        if(current == null){
            return;
        }
        int at = games.getSelectedIndex();
        if(upwards){
            form.MoveUp(current);
        } else {
            form.MoveDown(current);
        }
        refresh.run();
        games.setSelectedIndex(upwards ? at - 1 : at + 1);
    }
}
