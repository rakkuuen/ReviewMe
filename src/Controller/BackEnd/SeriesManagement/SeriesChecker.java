package Controller.BackEnd.SeriesManagement;

import java.util.ArrayList;
import java.util.List;

import Model.Reviews.Series;

// The rules for a series name. Each check returns what is wrong in words for the screen to show (empty list means fine)
public class SeriesChecker {
    public static final int maxNameLength = 40;

    // Trimmed, with any run of spaces turned into one
    public static String Clean(String name){
        return name == null ? "" : name.trim().replaceAll("\\s+", " ");
    }

    // A new series: the name has to be usable, and neither the name nor the tag it would get can belong to another series
    public static List<String> CheckNew(String name, List<Series> existing){
        String cleaned = Clean(name);
        List<String> errors = CheckName(cleaned, existing, null);
        String tag = SeriesTag.FromName(cleaned);
        if(errors.isEmpty() && !SeriesTag.HasLetter(tag)){
            errors.add("The name needs at least one letter");
        }
        if(errors.isEmpty() && IdIsTaken(tag, existing)){
            errors.add("Another series already uses the tag #" + tag);
        }
        return errors;
    }

    // A rename only changes the display name, so the tag isn't checked. ownId is the series being renamed
    public static List<String> CheckRename(String name, List<Series> existing, String ownId){
        return CheckName(Clean(name), existing, ownId);
    }

    private static List<String> CheckName(String cleaned, List<Series> existing, String ownId){
        List<String> errors = new ArrayList<>();
        if(cleaned.isEmpty()){
            errors.add("Give the series a name");
            return errors;
        }
        if(cleaned.length() > maxNameLength){
            errors.add("The name is over " + maxNameLength + " characters");
            return errors;
        }
        for(Series other : existing){
            if(!other.GetId().equals(ownId) && other.GetName().equalsIgnoreCase(cleaned)){
                errors.add("There is already a series called " + other.GetName());
            }
        }
        return errors;
    }

    private static boolean IdIsTaken(String tag, List<Series> existing){
        for(Series other : existing){
            if(other.GetId().equalsIgnoreCase(tag)){
                return true;
            }
        }
        return false;
    }
}
