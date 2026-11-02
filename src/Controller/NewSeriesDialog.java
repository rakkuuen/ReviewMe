package Controller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Controller.BackEnd.SeriesEditing.NewSeriesForm;
import Controller.BackEnd.SeriesManagement.SeriesGamesPlanner;
import Controller.BackEnd.SeriesManagement.SeriesManager;
import Controller.BackEnd.SeriesManagement.SeriesResult;
import Database.Dao.SeriesDao;
import Database.Dao.TemplateDao;
import Model.Reviews.Template;

// The New series window: a name (with its tag shown as you type), a template, Cancel and Create. The form's rules are
// NewSeriesForm's; this only shows them and calls SeriesManager when Create is pressed. onCreated gets the new series' id
class NewSeriesDialog {
    private static final String title = "New series";
    private static final String nameCaption = "Name";
    private static final String templateCaption = "Template";
    private static final String templateNote = "Games in this series use this template";
    private static final String cancelWords = "Cancel";
    private static final String createWords = "Create series";
    private static final int nameColumns = 24;
    private static final int gap = 8;

    static void Show(Component parent, Consumer<String> onCreated){
        Map<String, String> templates = new java.util.LinkedHashMap<>();
        for(Template template : TemplateDao.GetAllTemplates()){
            templates.put(template.GetId(), template.GetName());
        }
        NewSeriesForm form = new NewSeriesForm(SeriesDao.GetAllSeries(), templates, SeriesGamesPlanner.standardTemplateId);

        JDialog dialog = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(parent), title, JDialog.ModalityType.APPLICATION_MODAL);
        JTextField nameField = new JTextField(nameColumns);
        JLabel tagLine = new JLabel(form.GetTagLine());
        JLabel problems = new JLabel(" ");
        List<String> ids = new ArrayList<>(templates.keySet());
        JComboBox<String> templateBox = new JComboBox<>(templates.values().toArray(new String[0]));
        templateBox.setSelectedIndex(Math.max(0, ids.indexOf(form.GetTemplateId())));
        JButton cancel = new JButton(cancelWords), create = new JButton(createWords);

        Runnable refresh = () -> {
            form.SetName(nameField.getText());
            form.SetTemplateId(ids.isEmpty() ? null : ids.get(templateBox.getSelectedIndex()));
            tagLine.setText(form.GetTagLine());
            List<String> found = new ArrayList<>(form.GetProblems());
            if(form.GetCounter() != null){
                found.add(form.GetCounter());
            }
            problems.setText(found.isEmpty() ? " " : String.join("; ", found));
            create.setEnabled(form.CanCreate());
        };
        nameField.getDocument().addDocumentListener(new DocumentListener(){
            @Override public void insertUpdate(DocumentEvent e){ refresh.run(); }
            @Override public void removeUpdate(DocumentEvent e){ refresh.run(); }
            @Override public void changedUpdate(DocumentEvent e){ refresh.run(); }
        });
        templateBox.addActionListener(e -> refresh.run());
        cancel.addActionListener(e -> dialog.dispose());
        create.addActionListener(e -> {
            SeriesResult result = SeriesManager.Create(form.GetName(), form.GetTemplateId());
            if(!result.Succeeded()){
                JOptionPane.showMessageDialog(dialog, String.join("\n", result.GetErrors()), title, JOptionPane.WARNING_MESSAGE);
                return;
            }
            dialog.dispose();
            onCreated.accept(SeriesIdOf(form));
        });

        JPanel fields = new JPanel(new GridLayout(0, 1, gap, gap));
        fields.add(new JLabel(nameCaption));
        fields.add(nameField);
        fields.add(tagLine);
        fields.add(problems);
        fields.add(new JLabel(templateCaption));
        fields.add(templateBox);
        fields.add(new JLabel(templateNote));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, gap, gap));
        buttons.add(cancel);
        buttons.add(create);
        JPanel content = new JPanel(new BorderLayout(gap, gap));
        content.setBorder(new EmptyBorder(gap, gap, gap, gap));
        content.add(fields, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.getRootPane().setDefaultButton(create);
        refresh.run();
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    // The new series' id is its tag (see SeriesEditPlanner.Create)
    private static String SeriesIdOf(NewSeriesForm form){
        return Controller.BackEnd.SeriesManagement.SeriesTag.FromName(Controller.BackEnd.SeriesManagement.SeriesChecker.Clean(form.GetName()));
    }
}
