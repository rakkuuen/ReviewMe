package Controller;
import Model.GameReview;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MarkdownProcessor {
    // Need to read content from md files and place them into database/json 
    public MarkdownProcessor() {
        String reviewFolderPath = "D:\\stuff\\0 Vault\\All_Encompassing\\Games\\Reviews";
        List<GameReview> gameReviews = FindMDFiles(reviewFolderPath);
        // Here after I get the list of GameReview objects, I need to place them into a json file or sql database. (XML is for losers)
        
        // Temp console print for testing:
        // for (GameReview gameReview : gameReviews) {
        //     System.out.println("---------- Game Review ----------");
        //     System.out.println("Gameplay: " + gameReview.GetGameplay());
        //     System.out.println("Story: " + gameReview.GetStory());
        //     System.out.println("Setting: " + gameReview.GetSetting());
        //     System.out.println("Music/Audio: " + gameReview.GetMusic());
        //     System.out.println("Alternate Titles: " + gameReview.GetAlternateTitles());
        //     System.out.println("Achievements: " + gameReview.GetAchievements());
        //     System.out.println("Replayability: " + gameReview.GetReplayability());
        //     System.out.println("Final Rating: " + gameReview.GetFinalRating());
        //     System.out.println("Conclusion: " + gameReview.GetConclusion());
        //     System.out.println("---------------------------------");
        // }

    }

    public static List<GameReview> FindMDFiles(String reviewFolderPath){
        List<GameReview> gameReviews = new ArrayList<>();
        // Here we find and go through md files en-mass
        File dir = new File(reviewFolderPath);
        if(dir.exists() && dir.isDirectory()){
            File[] directoryFiles = dir.listFiles();
            if(directoryFiles != null){
                for(File child: directoryFiles){
                    // Make sure to only check md files
                    if(child.isFile() && child.getName().endsWith(".md")){
                        //System.out.println(child);
                        GameReview gameReview = ParseMDContents(child);
                        gameReviews.add(gameReview);
                    }
                }
            }
            else{
                System.err.println("Directory is null. Check path.");
            }
        }
        else{
            System.err.println("Provided path is not a valid directory.");
        }

        return gameReviews;
    }

    public static GameReview ParseMDContents(File reviewFilePath){
        GameReview currentGameReview = new GameReview();
        // Here we parse individual md files following the template structure.
        // First I want to map content under any specific heading to be associated with the heading itself.
        Map<String, StringBuilder> headingsMap = new HashMap<>();

        try{
            List<String> lines = Files.readAllLines(reviewFilePath.toPath());
            // We need to keep track of current heading to correctly attribute the content below each heading
            String currentHeading = null;

            for(String line : lines){
                line = line.trim();
                // Remove any formating specifiers from line
                line = RemoveFormatingSpecifiers(line);
                // Check if the line is a heading
                if (line.startsWith("#")) {
                    currentHeading = line.replaceAll("#+", "").trim(); // Remove '#' characters
                    // Initialize an empty StringBuilder for the heading
                    headingsMap.putIfAbsent(currentHeading, new StringBuilder());
                } else if (currentHeading != null && !line.isEmpty()) {
                    // Append content under the current heading
                    headingsMap.get(currentHeading).append(line).append("\n");
                }
            }
            // Map the headings to GameReview properties
            currentGameReview.SetGameplay(getContent(headingsMap, "Gameplay"));
            currentGameReview.SetStory(getContent(headingsMap, "Story"));
            currentGameReview.SetSetting(getContent(headingsMap, "Setting"));
            currentGameReview.SetMusic(getContent(headingsMap, "Music/Audio"));
            currentGameReview.SetAlternateTitles(getContent(headingsMap, "Alternate Titles"));
            currentGameReview.SetAchievements(getContent(headingsMap, "Achievements"));
            currentGameReview.SetReplayability(getContent(headingsMap, "Replay-ability out of 10"));
            String finalRatingText = getContent(headingsMap, "Final Rating").replaceAll("<.*?>", "").trim();
            try {
                currentGameReview.SetFinalRating(Integer.parseInt(finalRatingText));
            } catch (NumberFormatException e) {
                // Maybe change default so its more obvious when it fails.
                currentGameReview.SetFinalRating(0); // Default if parsing fails
            }

            currentGameReview.SetConclusion(getContent(headingsMap, "Conclusion"));

        } catch (IOException e) {
            e.printStackTrace();
        
        }
        return currentGameReview;
    }

    // Helper method to safely retrieve content for a given heading
    private static String getContent(Map<String, StringBuilder> map, String key) {
        // Safely retrieve the content for the specified heading
        return map.getOrDefault(key, new StringBuilder()).toString().trim();
    }

    private static String RemoveFormatingSpecifiers(String input) {
        // Remove everything between < and >, including the angle brackets
        return input.replaceAll("<.*?>", "");
    }
}
