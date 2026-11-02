package Controller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Controller.BackEnd.MainView.LibraryGameLoader;
import Controller.BackEnd.SeriesEditing.AssignConfirmation;
import Controller.BackEnd.SeriesEditing.AssignGamesForm;
import Controller.BackEnd.SeriesManagement.SeriesManager;
import Controller.BackEnd.SeriesManagement.SeriesResult;
import Database.Dao.SeriesDao;
import Database.Dao.TemplateDao;
import Model.Reviews.Series;
import Model.Reviews.Template;

// The assign picker: search your games, pick several, Assign. The form's rules are AssignGamesForm's; this shows them, asks to
// confirm when games would switch template, and calls SeriesManager.Assign. onAssigned runs after games were added
class AssignGamesDialog {
    private static final int searchColumns = 24;
    private static final int listRows = 12;
    private static final int gap = 8;

    static void Show(Component parent, String seriesId, Runnable onAssigned){
        Series series = SeriesDao.GetSeries(seriesId);
        if(series == null){
            return;
        }
        Template template = series.GetTemplateId() == null ? null : TemplateDao.GetTemplate(series.GetTemplateId());
        String templateName = template == null ? String.valueOf(series.GetTemplateId()) : template.GetName();
        AssignGamesForm form = new AssignGamesForm(seriesId, LibraryGameLoader.Load(), SeriesDao.GetAllSeries());
        String windowTitle = "Assign games to " + series.GetName();

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), windowTitle, JDialog.ModalityType.APPLICATION_MODAL);
        JTextField searchField = new JTextField(searchColumns);
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(listRows);
        JLabel emptyLine = new JLabel(" ");
        JLabel countLine = new JLabel();
        JButton cancel = new JButton("Cancel"), assign = new JButton();
        boolean[] refreshing = {false};

        Runnable refresh = () -> {
            refreshing[0] = true;
            List<AssignGamesForm.Candidate> visible = form.GetVisible();
            model.clear();
            List<Integer> ticked = new ArrayList<>();
            for(int i = 0; i < visible.size(); i++){
                model.addElement(visible.get(i).GetTitle() + "   -   " + visible.get(i).GetDetail());
                if(form.IsPicked(visible.get(i).GetTitle())){
                    ticked.add(i);
                }
            }
            for(int index : ticked){
                list.addSelectionInterval(index, index);
            }
            String empty = form.GetEmptyTitle();
            emptyLine.setText(empty.isEmpty() ? " " : empty + (form.GetEmptyHint().isEmpty() ? "" : ". " + form.GetEmptyHint()));
            countLine.setText(form.GetCountLine());
            assign.setText(form.GetAssignWords());
            assign.setEnabled(form.CanAssign());
            refreshing[0] = false;
        };
        searchField.getDocument().addDocumentListener(new DocumentListener(){
            @Override public void insertUpdate(DocumentEvent e){ Search(form, searchField, refresh); }
            @Override public void removeUpdate(DocumentEvent e){ Search(form, searchField, refresh); }
            @Override public void changedUpdate(DocumentEvent e){ Search(form, searchField, refresh); }
        });
        list.addListSelectionListener(e -> {
            if(refreshing[0] || e.getValueIsAdjusting()){
                return;
            }
            List<String> selected = new ArrayList<>();
            List<AssignGamesForm.Candidate> visible = form.GetVisible();
            for(int index : list.getSelectedIndices()){
                selected.add(visible.get(index).GetTitle());
            }
            form.SetVisiblePicked(selected);
            countLine.setText(form.GetCountLine());
            assign.setText(form.GetAssignWords());
            assign.setEnabled(form.CanAssign());
        });
        cancel.addActionListener(e -> dialog.dispose());
        assign.addActionListener(e -> {
            String ask = AssignConfirmation.Text(seriesId, templateName, form.GetPicked());
            if(ask != null && JOptionPane.showConfirmDialog(dialog, ask, form.GetAssignWords() + "?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION){
                return;
            }
            SeriesResult result = SeriesManager.Assign(seriesId, form.GetPicked());
            if(!result.Succeeded()){
                JOptionPane.showMessageDialog(dialog, String.join("\n", result.GetErrors()), windowTitle, JOptionPane.WARNING_MESSAGE);
                return;
            }
            dialog.dispose();
            onAssigned.run();
        });

        JPanel top = new JPanel(new BorderLayout(gap, gap));
        top.add(searchField, BorderLayout.NORTH);
        top.add(emptyLine, BorderLayout.SOUTH);
        JPanel bottom = new JPanel(new BorderLayout(gap, gap));
        bottom.add(countLine, BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, gap, gap));
        buttons.add(cancel);
        buttons.add(assign);
        bottom.add(buttons, BorderLayout.EAST);
        JPanel content = new JPanel(new BorderLayout(gap, gap));
        content.setBorder(new EmptyBorder(gap, gap, gap, gap));
        content.add(top, BorderLayout.NORTH);
        content.add(new JScrollPane(list), BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        refresh.run();
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void Search(AssignGamesForm form, JTextField field, Runnable refresh){
        form.SetSearch(field.getText());
        refresh.run();
    }
}
