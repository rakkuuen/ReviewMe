package Controller;
import Model.GameReview;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class MarkdownProcessor {
    // Need to read content from md files and place them into database/json 
    public MarkdownProcessor() {
        String reviewFolderPath = "D:\\stuff\\0 Vault\\All_Encompassing\\Games\\Reviews";
        List<GameReview> gameReviews = FindMDFiles(reviewFolderPath);
        // Here after I get the list of GameReview objects, I need to place them into a json file or sql database. (XML is for losers)
        
        // Temp console print for testing:

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
                        System.out.println(child.getName());
                        GameReview gameReview = ParseMDContents(child.getAbsolutePath());
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

    public static GameReview ParseMDContents(String reviewFilePath){
        GameReview currentGameReview = new GameReview();
        // Here we parse individual md files following the template structure.

        return currentGameReview;
    }
}
