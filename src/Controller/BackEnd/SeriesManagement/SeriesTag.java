package Controller.BackEnd.SeriesManagement;

// A series tag is worked out from its name and never changes after the series is created (a rename only changes the name)
public class SeriesTag {

    // Steamworld Dig -> SteamworldDig, house flipper -> HouseFlipper, Yakuza (PS2) -> YakuzaPS2.
    // Anything that isn't a letter or digit is dropped, each word starts with a capital and the rest is left as typed
    public static String FromName(String name){
        StringBuilder tag = new StringBuilder();
        boolean startOfWord = true;
        for(int i = 0; i < name.length(); i++){
            char letter = name.charAt(i);
            if(!Character.isLetterOrDigit(letter)){
                startOfWord = true;
                continue;
            }
            tag.append(startOfWord ? Character.toUpperCase(letter) : letter);
            startOfWord = false;
        }
        return tag.toString();
    }

    // A tag made only of digits isn't a tag in the vault, so it needs a letter somewhere
    public static boolean HasLetter(String tag){
        for(int i = 0; i < tag.length(); i++){
            if(Character.isLetter(tag.charAt(i))){
                return true;
            }
        }
        return false;
    }
}
