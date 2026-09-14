package Screens;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import Model.GameReview;
import Model.Button;
import Model.BackButton;
import Model.EditableField;
import Model.AutoGrowFieldList;
import Model.Theme;
import Database.GameReviewDao;

public class GameInfoScreen{
    private GameReview gameReview;
    private BackButton backButton;
    private Button editButton;
    private Button saveButton;
    private boolean isEditing = false;

    private AutoGrowFieldList fieldList;
    private JPanel fieldListPanel;

    private String title;
    private List<String> titleLines = new ArrayList<>();
    private Dimension windowDimension; // Remembered so ReapplyTheme() can re-wrap for a new font

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

    // scrollX is fixed; scrollY grows if the title wraps to more than one line, so it
    // never collides with the field list; width/height track window size via Reflow()
    private static final int scrollX = 40;
    private static final int minScrollY = 90;
    private static final int rightMargin = 20;
    private static final int bottomMargin = 30;

    private static final int titleFirstLineBaselineY = 55;
    private static final int titleMargin = 20; // Gap kept between the title text and the buttons either side
    private static final int backButtonRightEdge = 20 + 100; // BackButton's fixed x + width

    private static final int fieldX = 20;
    private static final int fieldWidth = 900;
    private static final int headingHeight = 22;
    private static final int minFieldHeight = 60; // Floor for empty/short fields
    private static final int rowGap = 20;

    // Edit/Save stack in one column (Edit on top, Save below) rather than sitting side
    // by side, freeing up horizontal room for the title
    private static final int rightButtonWidth = 120;
    private static final int rightButtonHeight = 24;
    private static final int rightButtonGap = 2;

    public GameInfoScreen(GameReview gameReview, Dimension windowDimension, Runnable onBack){
        this.gameReview = gameReview;
        title = gameReview.GetTitle();
        if(title == null || title.isEmpty()){
            title = "Unknown Game";
        }
        backButton = new BackButton(onBack);
        editButton = new Button(rightButtonWidth, rightButtonHeight, 0, 20, 10, "Edit"); // x corrected by Reflow below
        saveButton = new Button(rightButtonWidth, rightButtonHeight, 0, 20 + rightButtonHeight + rightButtonGap, 10, "Save"); // x corrected by Reflow below

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
        fieldListPanel = fieldList.GetComponent();

        // Start read-only - Edit must be clicked before anything's editable
        fieldList.SetFieldsEditable(false);
        saveButton.SetEnabled(false);

        Reflow(windowDimension);
    }

    public void Reflow(Dimension windowDimension){
        this.windowDimension = windowDimension;
        saveButton.x = windowDimension.width - rightButtonWidth - rightMargin;
        editButton.x = saveButton.x; // Same column, stacked above Save

        FontMetrics titleMetrics = GetFontMetrics(Theme.Current.GetTitle().GetFont());

        // Title is centered on the FULL window width, not on the gap between the two
        // buttons - Back sits much closer to the left edge than Edit/Save sit to the
        // right edge, so the binding constraint is whichever side has less room relative
        // to the window's true center, not the raw gap between the buttons themselves
        int windowCenter = windowDimension.width / 2;
        int leftHalfAvailable = windowCenter - (backButtonRightEdge + titleMargin);
        int rightHalfAvailable = (editButton.x - titleMargin) - windowCenter;
        int titleAvailableWidth = 2 * Math.min(leftHalfAvailable, rightHalfAvailable);

        titleLines = WrapText(title, titleMetrics, titleAvailableWidth);

        // scrollY stays at its usual spot for a single-line title (the common case); a
        // wrapped title just pushes the field list down however far it actually needs
        int titleBlockBottom = titleFirstLineBaselineY + titleMetrics.getDescent()
                + (titleLines.size() - 1) * titleMetrics.getHeight();
        int scrollY = Math.max(minScrollY, titleBlockBottom + titleMargin);

        int scrollWidth = windowDimension.width - scrollX - rightMargin;
        int scrollHeight = windowDimension.height - scrollY - bottomMargin;
        fieldListPanel.setBounds(scrollX, scrollY, scrollWidth, scrollHeight);
    }

    // FontMetrics normally needs a live Graphics context; this gets one without needing
    // an actual on-screen paint, so Reflow() can wrap the title independent of painting
    private FontMetrics GetFontMetrics(Font font){
        BufferedImage scratch = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics scratchG = scratch.getGraphics();
        scratchG.setFont(font);
        FontMetrics fm = scratchG.getFontMetrics();
        scratchG.dispose();
        return fm;
    }

    // Greedy word-wrap: packs words onto a line until the next one would exceed maxWidth
    private List<String> WrapText(String text, FontMetrics fm, int maxWidth){
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for(String word : text.split(" ")){
            String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
            if(currentLine.length() == 0 || fm.stringWidth(candidate) <= maxWidth){
                currentLine = new StringBuilder(candidate);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }
        if(currentLine.length() > 0){
            lines.add(currentLine.toString());
        }
        return lines;
    }

    public void paint(Graphics g, Point mousePos, Dimension windowDimension){
        // Back / Edit / Save buttons
        backButton.paint(g, mousePos);
        editButton.paint(g, mousePos);
        saveButton.paint(g, mousePos);

        // Draw Title text, one or more centered lines
        g.setColor(Theme.Current.GetTitle().GetColour());
        g.setFont(Theme.Current.GetTitle().GetFont());
        FontMetrics titleMetrics = g.getFontMetrics();

        int lineY = titleFirstLineBaselineY;
        for(String line : titleLines){
            int lineWidth = titleMetrics.stringWidth(line);
            int lineX = (windowDimension.width - lineWidth) / 2; // Center horizontally
            g.drawString(line, lineX, lineY);
            lineY += titleMetrics.getHeight();
        }
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
        panel.add(fieldListPanel);
        panel.revalidate();
        panel.repaint();
        fieldList.ScrollToTop();
    }

    public void RemoveComponentsFrom(JPanel panel){
        panel.remove(fieldListPanel);
        panel.revalidate();
        panel.repaint();
    }

    // Re-applies Theme.Current to the parts that can't be looked up lazily: real Swing
    // text properties in the field list, and the title's word-wrap (a font/size change
    // shifts wrapped width even though colour/font itself repaints fresh automatically)
    public void ReapplyTheme(){
        fieldList.ReapplyTheme();
        Reflow(windowDimension);
    }
}
