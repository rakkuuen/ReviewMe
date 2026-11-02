package Controller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import Controller.BackEnd.MainView.MainViewLabels;
import Controller.BackEnd.NewReview.NewReviewManager;
import Controller.BackEnd.NewReview.NewReviewRequest;
import Controller.BackEnd.NewReview.NewReviewResult;
import Database.Dao.SeriesDao;
import Model.Library.LibraryGame;
import Model.Library.SteamGameStatus;
import Model.Reviews.GameState;
import Model.Reviews.Series;

// The New review window. For a Steam game the title and state come from Steam (the state is read only) and Shelved and Completed are
// optional marks; for a game that isn't on Steam (steamGame is null) the title and state are typed. Either way a series can be picked,
// which sets the template. NewReviewManager does the work; onCreated gets the new review's title
class NewReviewDialog {
    private static final String none = "No series";
    private static final int fieldColumns = 24;
    private static final int gap = 8;

    static void Show(Component parent, LibraryGame steamGame, Consumer<String> onCreated){
        boolean onSteam = steamGame != null;
        String windowTitle = onSteam ? "Write review" : "Add game manually";
        SteamGameStatus flags = onSteam ? steamGame.GetFlags() : null;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), windowTitle, JDialog.ModalityType.APPLICATION_MODAL);
        JTextField titleField = new JTextField(onSteam ? steamGame.GetTitle() : "", fieldColumns);
        titleField.setEditable(!onSteam);
        List<Series> allSeries = SeriesDao.GetAllSeries();
        List<String> seriesIds = new ArrayList<>();
        JComboBox<String> seriesBox = new JComboBox<>();
        seriesBox.addItem(none);
        for(Series series : allSeries){
            seriesBox.addItem(series.GetName());
            seriesIds.add(series.GetId());
        }

        JPanel fields = new JPanel(new GridLayout(0, 1, gap, gap));
        fields.add(new JLabel(onSteam ? "Owned on Steam" : "Title"));
        fields.add(titleField);
        fields.add(new JLabel("Series (optional, sets the template)"));
        fields.add(seriesBox);

        JCheckBox shelved = new JCheckBox("Shelved", flags != null && flags.IsShelved());
        JTextField reason = new JTextField(flags == null || flags.GetShelvedReason() == null ? "" : flags.GetShelvedReason(), fieldColumns);
        JCheckBox completed = new JCheckBox("Completed", flags != null && flags.IsCompleted());
        JTextField completedOn = new JTextField(flags == null || flags.GetCompletedOn() == null ? "" : flags.GetCompletedOn(), fieldColumns);
        JRadioButton inProgress = new JRadioButton("In progress");
        JRadioButton notStarted = new JRadioButton("Not started", true);
        if(onSteam){
            fields.add(new JLabel("Game state: " + MainViewLabels.StateLabel(steamGame.GetStatus().GetGameState()) + " (from Steam, read only)"));
        } else {
            ButtonGroup group = new ButtonGroup();
            group.add(notStarted);
            group.add(inProgress);
            JPanel states = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0));
            states.add(notStarted);
            states.add(inProgress);
            fields.add(new JLabel("Game state"));
            fields.add(states);
        }
        fields.add(completed);
        fields.add(new JLabel("Completed on (optional, like 2025-03-02)"));
        fields.add(completedOn);
        fields.add(shelved);
        fields.add(new JLabel("Why (optional)"));
        fields.add(reason);
        completedOn.setEnabled(completed.isSelected());
        reason.setEnabled(shelved.isSelected());
        completed.addActionListener(e -> completedOn.setEnabled(completed.isSelected()));
        shelved.addActionListener(e -> reason.setEnabled(shelved.isSelected()));

        JButton cancel = new JButton("Cancel"), create = new JButton("Create and open");
        cancel.addActionListener(e -> dialog.dispose());
        create.addActionListener(e -> {
            int picked = seriesBox.getSelectedIndex();
            NewReviewRequest request = new NewReviewRequest(titleField.getText(), onSteam ? steamGame.GetSteam().GetAppId() : null, picked <= 0 ? null : seriesIds.get(picked - 1),
                    shelved.isSelected(), reason.getText(), completed.isSelected(), completedOn.getText(), inProgress.isSelected());
            NewReviewResult result = NewReviewManager.Create(request);
            if(!result.Succeeded()){
                JOptionPane.showMessageDialog(dialog, String.join("\n", result.GetErrors()), windowTitle, JOptionPane.WARNING_MESSAGE);
                return;
            }
            dialog.dispose();
            onCreated.accept(result.GetTitle());
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, gap, gap));
        buttons.add(cancel);
        buttons.add(create);
        JPanel content = new JPanel(new BorderLayout(gap, gap));
        content.setBorder(new EmptyBorder(gap, gap, gap, gap));
        content.add(fields, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.getRootPane().setDefaultButton(create);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
