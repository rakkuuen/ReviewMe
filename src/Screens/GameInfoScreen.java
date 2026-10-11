package Screens;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import Controller.BackEnd.Reviews.LoadedReview;
import Controller.BackEnd.Reviews.ReviewLoader;
import Controller.BackEnd.Reviews.ReviewSaver;
import Controller.BackEnd.Reviews.SaveResult;
import Model.Widgets.Button;
import Model.Widgets.BackButton;
import Model.Widgets.AutoGrowFieldList;
import Model.Themes.Theme;
import Screens.Detail.SectionBinding;
import Screens.Detail.SectionBuilder;

public class GameInfoScreen{
    private LoadedReview loadedReview;
    private BackButton backButton;
    private Button editButton;
    private Button saveButton;
    private Button cancelButton;
    private boolean isEditing = false;

    // One line under the title: "Saved", or why a save was refused
    private String statusMessage;
    private boolean statusIsError;
    private static final Color errorColour = new Color(0xD04040); // The themes have no error colour yet

    private AutoGrowFieldList fieldList;
    private JPanel fieldListPanel;

    private String title;
    private List<String> titleLines = new ArrayList<>();
    private Dimension windowDimension; // Remembered so ReapplyTheme() can re-wrap for a new font

    // What's on screen for each section, so 7c can read the boxes back when saving
    private List<SectionBinding> sections;

    // scrollX is fixed; scrollY grows if the title wraps to more than one line, so it
    // never collides with the field list; width/height track window size via Reflow()
    private static final int scrollX = 40;
    private static final int minScrollY = 102; // Clears the three stacked buttons on the right
    private static final int rightMargin = 20;
    private static final int statusLineGap = 10; // Status line sits this far above the field list
    private static final int bottomMargin = 30;

    private static final int titleFirstLineBaselineY = 55;
    private static final int titleMargin = 20; // Gap kept between the title text and the buttons either side
    private static final int backButtonRightEdge = 20 + 100; // BackButton's fixed x + width

    private static final int fieldX = 20;
    private static final int fieldWidth = 900;
    private static final int headingHeight = 22;
    private static final int minFieldHeight = 60; // Floor for empty/short fields
    private static final int rowGap = 20;

    // Edit/Save/Cancel stack in one column rather than sitting side by side, freeing up
    // horizontal room for the title
    private static final int rightButtonWidth = 120;
    private static final int rightButtonHeight = 24;
    private static final int rightButtonGap = 2;

    public GameInfoScreen(LoadedReview loadedReview, Dimension windowDimension, Runnable onBack){
        this.loadedReview = loadedReview;
        title = loadedReview.GetReview().GetTitle();
        if(title == null || title.isEmpty()){
            title = "Unknown Game";
        }
        backButton = new BackButton(onBack);
        editButton = new Button(rightButtonWidth, rightButtonHeight, 0, 20, "Edit"); // x corrected by Reflow below
        saveButton = new Button(rightButtonWidth, rightButtonHeight, 0, 20 + rightButtonHeight + rightButtonGap, "Save"); // x corrected by Reflow below
        cancelButton = new Button(rightButtonWidth, rightButtonHeight, 0, 20 + 2 * (rightButtonHeight + rightButtonGap), "Cancel"); // x corrected by Reflow below

        fieldList = new AutoGrowFieldList(fieldX, fieldWidth, headingHeight, minFieldHeight, rowGap);

        // One section per field of this review's template, in template order
        sections = SectionBuilder.Build(loadedReview, fieldList);

        fieldList.Relayout();
        fieldListPanel = fieldList.GetComponent();

        // Read-only until Edit is pressed
        LeaveEditMode();

        Reflow(windowDimension);
    }

    public void Reflow(Dimension windowDimension){
        this.windowDimension = windowDimension;
        saveButton.x = windowDimension.width - rightButtonWidth - rightMargin;
        editButton.x = saveButton.x; // Same column, stacked above Save
        cancelButton.x = saveButton.x;

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
        // Back / Edit / Save / Cancel buttons
        backButton.paint(g, mousePos);
        editButton.paint(g, mousePos);
        saveButton.paint(g, mousePos);
        cancelButton.paint(g, mousePos);

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

        PaintStatusLine(g, windowDimension);
    }

    // Centred just above the field list, cut short with ... if it's wider than the list
    private void PaintStatusLine(Graphics g, Dimension windowDimension){
        if(statusMessage == null){
            return;
        }
        g.setFont(Theme.Current.GetField().GetFont());
        g.setColor(statusIsError ? errorColour : Theme.Current.GetHeading().GetColour());
        FontMetrics metrics = g.getFontMetrics();

        int maxWidth = 2 * (cancelButton.x - titleMargin - windowDimension.width / 2); // Stays clear of the buttons, like the title
        String shown = statusMessage;
        while(shown.length() > 1 && metrics.stringWidth(shown) > maxWidth){
            shown = shown.substring(0, shown.length() - 2) + "…";
        }
        g.drawString(shown, (windowDimension.width - metrics.stringWidth(shown)) / 2, fieldListPanel.getY() - statusLineGap);
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

    public boolean WasCancelClicked(Point mousePos){
        return cancelButton.contains(mousePos);
    }

    public void EnterEditMode(){
        isEditing = true;
        statusMessage = null;
        fieldList.SetFieldsEditable(true);
        editButton.SetEnabled(false);
        saveButton.SetEnabled(true);
        cancelButton.SetEnabled(true);
    }

    // Reads the boxes back into the review and saves it. If the save is refused the boxes stay as they are, in edit mode
    public void SaveChanges(){
        if(!isEditing){
            return;
        }
        for(SectionBinding section : sections){
            section.ReadInto(loadedReview.GetReview());
        }

        SaveResult result = ReviewSaver.Save(loadedReview);
        if(!result.Succeeded()){
            ShowStatus("Not saved: " + String.join("; ", result.GetErrors()), true);
            return;
        }
        LeaveEditMode();
        ShowStatus("Saved", false);
    }

    // Throws away what was typed: the boxes go back to what is stored
    public void CancelChanges(){
        if(!isEditing){
            return;
        }
        LoadedReview stored = ReviewLoader.Load(loadedReview.GetReview().GetTitle());
        if(stored != null){
            loadedReview = stored;
            for(SectionBinding section : sections){
                section.ShowFrom(stored.GetReview());
            }
        }
        LeaveEditMode();
        statusMessage = null;
    }

    private void LeaveEditMode(){
        isEditing = false;
        fieldList.SetFieldsEditable(false);
        editButton.SetEnabled(true);
        saveButton.SetEnabled(false);
        cancelButton.SetEnabled(false);
    }

    private void ShowStatus(String message, boolean isError){
        statusMessage = message;
        statusIsError = isError;
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
