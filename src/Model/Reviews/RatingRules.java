package Model.Reviews;

import java.util.regex.Pattern;

// What a rating field may hold: a whole number from -1 to 11, or ?
public class RatingRules {
    public static final int min = -1;
    public static final int max = 11;
    public static final String unknown = "?";

    public enum RatingState { MISSING, NUMBER, UNKNOWN, INVALID }

    // No leading zeros, no plus sign, no -0
    private static final Pattern wholeNumber = Pattern.compile("^(0|-?[1-9][0-9]{0,2})$");

    public static RatingState Classify(String text){
        String trimmed = text == null ? "" : text.trim();
        if(trimmed.isEmpty()){
            return RatingState.MISSING;
        }
        if(trimmed.equals(unknown)){
            return RatingState.UNKNOWN;
        }
        if(wholeNumber.matcher(trimmed).matches()){
            int value = Integer.parseInt(trimmed);
            return value >= min && value <= max ? RatingState.NUMBER : RatingState.INVALID;
        }
        return RatingState.INVALID;
    }

    // Blank (missing) is accepted here; whether missing is allowed is the template's required flag
    public static boolean IsAcceptable(String text){
        return Classify(text) != RatingState.INVALID;
    }

    // Null unless text is a valid number
    public static Integer ToNumber(String text){
        return Classify(text) == RatingState.NUMBER ? Integer.valueOf(text.trim()) : null;
    }

    // Numbers first, then ?, then missing. Missing stays last whichever way the numbers go
    public static int Compare(String a, String b, boolean highestFirst){
        RatingState stateA = Classify(a);
        RatingState stateB = Classify(b);
        int rankA = Rank(stateA);
        int rankB = Rank(stateB);
        if(rankA != rankB){
            return Integer.compare(rankA, rankB);
        }
        if(stateA == RatingState.NUMBER){
            int byNumber = Integer.compare(ToNumber(a), ToNumber(b));
            return highestFirst ? -byNumber : byNumber;
        }
        return 0;
    }

    private static int Rank(RatingState state){
        switch(state){
            case NUMBER: return 0;
            case UNKNOWN: return 1;
            default: return 2;
        }
    }
}
