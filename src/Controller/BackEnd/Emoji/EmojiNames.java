package Controller.BackEnd.Emoji;

// The rules for an emoji's name (the shortcode between colons, and the file name without .svg): 2 to 24 characters, a-z, 0-9 and _
public class EmojiNames {
    public static final int minLength = 2;
    public static final int maxLength = 24;
    public static final String fileSuffix = ".svg";

    private EmojiNames(){
    }

    public static boolean IsNameChar(char c){
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_';
    }

    public static boolean IsValid(String name){
        if(name == null || name.length() < minLength || name.length() > maxLength){
            return false;
        }
        for(int i = 0; i < name.length(); i++){
            if(!IsNameChar(name.charAt(i))){
                return false;
            }
        }
        return true;
    }

    // "coffee.svg" is "coffee"; null when the file isn't an .svg or its name isn't a valid emoji name
    public static String FromFileName(String fileName){
        if(fileName == null || !fileName.endsWith(fileSuffix)){
            return null;
        }
        String name = fileName.substring(0, fileName.length() - fileSuffix.length());
        return IsValid(name) ? name : null;
    }
}
