package Screens;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import Model.GameReview;
import Model.Button;
import Model.EditableField;
import Database.GameReviewDao;

public class GameInfoScreen{
    private GameReview gameReview;
    private Button backButton;
    private Button saveButton;

    private JScrollPane scrollPane;
    private JPanel fieldsContainer;

    private EditableField gameplayField;
    private EditableField storyField;
    private EditableField settingField;
    private EditableField musicField;
    private EditableField voiceActingField;
    private EditableField alternateTitlesField;
    private EditableField achievementsField;
    private EditableField replayabilityField;
    private EditableField finalRatingField;
    private EditableField conclusionField;

    // Single column, top to bottom, scrolled - so each field can be given comfortable
    // height for paragraph text instead of being squeezed to fit the window
    private static final int scrollX = 40;
    private static final int scrollY = 90;
    private static final int scrollWidth = 960;
    private static final int scrollHeight = 600;

    private static final int fieldX = 20;
    private static final int fieldWidth = 900;
    private static final int headingHeight = 22;
    private static final int fieldHeight = 140;
    private static final int rowGap = 20;
    private static final int rowPitch = headingHeight + fieldHeight + rowGap;
    private static final int rowCount = 10;
    private static final int contentWidth = fieldX * 2 + fieldWidth;
    private static final int contentHeight = rowPitch * rowCount;

    public GameInfoScreen(GameReview gameReview){
        this.gameReview = gameReview;
        backButton = new Button(100, 50, 20, 20, 10, "Back");
        saveButton = new Button(120, 50, 884, 20, 10, "Save");

        fieldsContainer = new JPanel();
        fieldsContainer.setLayout(null);
        fieldsContainer.setPreferredSize(new Dimension(contentWidth, contentHeight));

        int row0 = 0;
        int row1 = row0 + rowPitch;
        int row2 = row1 + rowPitch;
        int row3 = row2 + rowPitch;
        int row4 = row3 + rowPitch;
        int row5 = row4 + rowPitch;
        int row6 = row5 + rowPitch;
        int row7 = row6 + rowPitch;
        int row8 = row7 + rowPitch;
        int row9 = row8 + rowPitch;

        gameplayField = new EditableField(gameReview.GetGameplay(), "How did the gameplay feel?",
                fieldX, row0 + headingHeight, fieldWidth, fieldHeight);
        AddRow(fieldsContainer, "Gameplay", row0, gameplayField);

        storyField = new EditableField(gameReview.GetStory(), "What did you think of the story?",
                fieldX, row1 + headingHeight, fieldWidth, fieldHeight);
        AddRow(fieldsContainer, "Story", row1, storyField);

        settingField = new EditableField(gameReview.GetSetting(), "Describe the game's setting...",
                fieldX, row2 + headingHeight, fieldWidth, fieldHeight);
        AddRow(fieldsContainer, "Setting", row2, settingField);

        musicField = new EditableField(gameReview.GetMusic(), "Thoughts on the music/audio...",
                fieldX, row3 + headingHeight, fieldWidth, fieldHeight);
        AddRow(fieldsContainer, "Music/Audio", row3, musicField);

        voiceActingField = new EditableField(gameReview.GetVoiceActing(), "Thoughts on the voice acting...",
                fieldX, row4 + headingHeight, fieldWidth, fieldHeight);
        AddRow(fieldsContainer, "Voice Acting", row4, voiceActingField);

        alternateTitlesField = new EditableField(gameReview.GetAlternateTitles(), "Any alternate titles?",
                fieldX, row5 + headingHeight, fieldWidth, fieldHeight);
        AddRow(fieldsContainer, "Alternate Titles", row5, alternateTitlesField);

        achievementsField = new EditableField(gameReview.GetAchievements(), "Notable achievements...",
                fieldX, row6 + headingHeight, fieldWidth, fieldHeight);
        AddRow(fieldsContainer, "Achievements", row6, achievementsField);

        replayabilityField = new EditableField(gameReview.GetReplayability(), "Replay-ability out of 10...",
                fieldX, row7 + headingHeight, fieldWidth, fieldHeight);
        AddRow(fieldsContainer, "Replay-ability out of 10", row7, replayabilityField);

        finalRatingField = new EditableField(
                gameReview.GetFinalRating() == 0 ? null : String.valueOf(gameReview.GetFinalRating()),
                "Final rating (number)", fieldX, row8 + headingHeight, fieldWidth, fieldHeight);
        AddRow(fieldsContainer, "Final Rating", row8, finalRatingField);

        conclusionField = new EditableField(gameReview.GetConclusion(), "Final thoughts / conclusion...",
                fieldX, row9 + headingHeight, fieldWidth, fieldHeight);
        AddRow(fieldsContainer, "Conclusion", row9, conclusionField);

        scrollPane = new JScrollPane(fieldsContainer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(scrollX, scrollY, scrollWidth, scrollHeight);
    }

    // Adds a heading label + its field to the scrollable content, at the given row's y offset
    private void AddRow(JPanel container, String heading, int rowY, EditableField field){
        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headingLabel.setBounds(fieldX, rowY, fieldWidth, headingHeight);
        container.add(headingLabel);
        container.add(field);
    }

    public void paint(Graphics g, Point mousePos, Dimension windowDimension){
        // Back / Save buttons
        backButton.paint(g, mousePos);
        saveButton.paint(g, mousePos);

        // Draw Title text
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 32));

        String title = gameReview.GetTitle();
        if (title == null || title.isEmpty()) {
            title = "Unknown Game"; // Fallback title
        }

        FontMetrics titleMetrics = g.getFontMetrics();
        int titleWidth = titleMetrics.stringWidth(title);
        int titleX = (windowDimension.width - titleWidth) / 2; // Center horizontally
        g.drawString(title, titleX, 55);
    }

    public boolean WasBackClicked(Point mousePos){
        return backButton.contains(mousePos);
    }

    public boolean WasSaveClicked(Point mousePos){
        return saveButton.contains(mousePos);
    }

    public void SaveChanges(){
        gameReview.SetGameplay(gameplayField.GetValue());
        gameReview.SetStory(storyField.GetValue());
        gameReview.SetSetting(settingField.GetValue());
        gameReview.SetMusic(musicField.GetValue());
        gameReview.SetVoiceActing(voiceActingField.GetValue());
        gameReview.SetAlternateTitles(alternateTitlesField.GetValue());
        gameReview.SetAchievements(achievementsField.GetValue());
        gameReview.SetReplayability(replayabilityField.GetValue());

        try {
            gameReview.SetFinalRating(Integer.parseInt(finalRatingField.GetValue().trim()));
        } catch (NumberFormatException e) {
            gameReview.SetFinalRating(0);
        }

        gameReview.SetConclusion(conclusionField.GetValue());

        GameReviewDao.UpdateGameReview(gameReview);
    }

    public void AddComponentsTo(JPanel panel){
        panel.add(scrollPane);
        panel.revalidate();
        panel.repaint();
    }

    public void RemoveComponentsFrom(JPanel panel){
        panel.remove(scrollPane);
        panel.revalidate();
        panel.repaint();
    }
}
