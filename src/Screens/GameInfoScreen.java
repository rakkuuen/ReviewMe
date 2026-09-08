package Screens;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import Model.GameReview;
import Model.Button;
import Model.BackButton;
import Model.EditableField;
import Model.AutoGrowFieldList;
import Database.GameReviewDao;

public class GameInfoScreen{
    private GameReview gameReview;
    private BackButton backButton;
    private Button editButton;
    private Button saveButton;
    private boolean isEditing = false;

    private AutoGrowFieldList fieldList;
    private JScrollPane scrollPane;

    private EditableField gameplayField;
    private EditableField storyField;
    private EditableField settingField;
    private EditableField musicField;
    private EditableField voiceActingField; // null if this review's template didn't have it
    private EditableField alternateTitlesField; // null if this review's template didn't have it
    private EditableField achievementsField;
    private EditableField replayabilityField;
    private EditableField finalRatingField;
    private EditableField conclusionField;

    // scrollX/scrollY are fixed; width/height track window size via Reflow()
    private static final int scrollX = 40;
    private static final int scrollY = 90;
    private static final int rightMargin = 20;
    private static final int bottomMargin = 30;

    private static final int fieldX = 20;
    private static final int fieldWidth = 900;
    private static final int headingHeight = 22;
    private static final int minFieldHeight = 60; // Floor for empty/short fields
    private static final int rowGap = 20;

    private static final int saveButtonWidth = 120;
    private static final int editButtonWidth = 100;
    private static final int buttonGap = 10;

    public GameInfoScreen(GameReview gameReview, Dimension windowDimension, Runnable onBack){
        this.gameReview = gameReview;
        backButton = new BackButton(onBack);
        editButton = new Button(editButtonWidth, 50, 0, 20, 10, "Edit"); // x corrected by Reflow below
        saveButton = new Button(saveButtonWidth, 50, 0, 20, 10, "Save"); // x corrected by Reflow below

        fieldList = new AutoGrowFieldList(fieldX, fieldWidth, headingHeight, minFieldHeight, rowGap);

        gameplayField = fieldList.AddField("Gameplay", gameReview.GetGameplay(), "Tell me about the gameplay... is it good or shit");
        storyField = fieldList.AddField("Story", gameReview.GetStory(), "Once upon a time there was a story and it goes like this.");
        settingField = fieldList.AddField("Setting", gameReview.GetSetting(), "Whats the setting of the game");
        musicField = fieldList.AddField("Music/Audio", gameReview.GetMusic(), "Thoughts on the music/audio was it poppin");

        if(gameReview.GetVoiceActing() != null){
            voiceActingField = fieldList.AddField("Voice Acting", gameReview.GetVoiceActing(), "Thoughts on the voice acting...");
        }

        if(gameReview.GetAlternateTitles() != null){
            alternateTitlesField = fieldList.AddField("Alternate Titles", gameReview.GetAlternateTitles(), "<Alternate title here>");
        }

        achievementsField = fieldList.AddField("Achievements", gameReview.GetAchievements(), "Notable achievements...");
        replayabilityField = fieldList.AddField("Replay-ability out of 10", gameReview.GetReplayability(), "Replay-ability out of 10...");
        finalRatingField = fieldList.AddField("Final Rating",
                gameReview.GetFinalRating() == 0 ? null : String.valueOf(gameReview.GetFinalRating()),
                "Final rating (number)");
        conclusionField = fieldList.AddField("Conclusion", gameReview.GetConclusion(), "Final thoughts / conclusion...");

        fieldList.Relayout();
        scrollPane = fieldList.GetComponent();

        // Start read-only - Edit must be clicked before anything's editable
        fieldList.SetFieldsEditable(false);
        saveButton.SetEnabled(false);

        Reflow(windowDimension);
    }

    public void Reflow(Dimension windowDimension){
        saveButton.x = windowDimension.width - saveButtonWidth - rightMargin;
        editButton.x = saveButton.x - editButtonWidth - buttonGap;

        int scrollWidth = windowDimension.width - scrollX - rightMargin;
        int scrollHeight = windowDimension.height - scrollY - bottomMargin;
        scrollPane.setBounds(scrollX, scrollY, scrollWidth, scrollHeight);
    }

    public void paint(Graphics g, Point mousePos, Dimension windowDimension){
        // Back / Edit / Save buttons
        backButton.paint(g, mousePos);
        editButton.paint(g, mousePos);
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

    public void NotifyBackPressed(Point mousePos){
        backButton.HandlePress(mousePos);
    }

    public void NotifyBackReleased(Point mousePos){
        backButton.HandleRelease(mousePos);
    }

    public boolean WasEditClicked(Point mousePos){
        return editButton.contains(mousePos);
    }

    public boolean WasSaveClicked(Point mousePos){
        return saveButton.contains(mousePos);
    }

    public void EnterEditMode(){
        isEditing = true;
        fieldList.SetFieldsEditable(true);
        editButton.SetEnabled(false);
        saveButton.SetEnabled(true);
    }

    public void SaveChanges(){
        gameReview.SetGameplay(gameplayField.GetValue());
        gameReview.SetStory(storyField.GetValue());
        gameReview.SetSetting(settingField.GetValue());
        gameReview.SetMusic(musicField.GetValue());

        if(voiceActingField != null){
            gameReview.SetVoiceActing(voiceActingField.GetValue());
        }
        if(alternateTitlesField != null){
            gameReview.SetAlternateTitles(alternateTitlesField.GetValue());
        }

        gameReview.SetAchievements(achievementsField.GetValue());
        gameReview.SetReplayability(replayabilityField.GetValue());

        try {
            gameReview.SetFinalRating(Integer.parseInt(finalRatingField.GetValue().trim()));
        } catch (NumberFormatException e) {
            gameReview.SetFinalRating(0);
        }

        gameReview.SetConclusion(conclusionField.GetValue());

        GameReviewDao.UpdateGameReview(gameReview);

        isEditing = false;
        fieldList.SetFieldsEditable(false);
        editButton.SetEnabled(true);
        saveButton.SetEnabled(false);
    }

    public void AddComponentsTo(JPanel panel){
        panel.add(scrollPane);
        panel.revalidate();
        panel.repaint();
        fieldList.ScrollToTop();
    }

    public void RemoveComponentsFrom(JPanel panel){
        panel.remove(scrollPane);
        panel.revalidate();
        panel.repaint();
    }
}
