package Model.Reviews;

import java.util.regex.Pattern;

// What a rating field may hold: any whole number (joke values like -129412890473987 or 00000000000000000 included), or ?
// The text is stored and shown exactly as typed. Only sorting and maths use ToNumber, which treats
// anything below -1 as -1 and anything above 10 as 11
public class RatingRules {
    public static final int min = -1;
    public static final int max = 11;
    public static final String unknown = "?";
    public static final int maxLength = 40;   // Longest rating text the box takes, minus sign included

    public enum RatingState { MISSING, NUMBER, UNKNOWN, INVALID }

    // Digits with an optional minus: any length, leading zeros fine. No plus sign, no decimals
    private static final Pattern wholeNumber = Pattern.compile("^-?[0-9]+$");

    public static RatingState Classify(String text){
        String trimmed = text == null ? "" : text.trim();
        if(trimmed.isEmpty()){
            return RatingState.MISSING;
        }
        if(trimmed.equals(unknown)){
            return RatingState.UNKNOWN;
        }
        if(trimmed.length() <= maxLength && wholeNumber.matcher(trimmed).matches()){
            return RatingState.NUMBER;
        }
        return RatingState.INVALID;
    }

    // Blank (missing) is accepted here; whether missing is allowed is the template's required flag
    public static boolean IsAcceptable(String text){
        return Classify(text) != RatingState.INVALID;
    }

    // A whole number outside -1..11: a joke value. It's kept as typed and counts as -1 or 11
    public static boolean IsOutOfRange(String text){
        if(Classify(text) != RatingState.NUMBER){
            return false;
        }
        String trimmed = text.trim();
        String digits = DigitsWithoutZeros(trimmed);
        if(digits.isEmpty()){
            return false;
        }
        if(trimmed.startsWith("-")){
            return !digits.equals("1");
        }
        return digits.length() > 2 || Integer.parseInt(digits) > max;
    }

    // What the rating counts as for sorting and maths, always -1 to 11. Null unless text is a whole number
    public static Integer ToNumber(String text){
        if(Classify(text) != RatingState.NUMBER){
            return null;
        }
        String trimmed = text.trim();
        String digits = DigitsWithoutZeros(trimmed);
        if(digits.isEmpty()){
            return 0;   // 0, 000, -0 ...
        }
        if(trimmed.startsWith("-")){
            return min;
        }
        return digits.length() > 2 ? max : Math.min(Integer.parseInt(digits), max);
    }

    // A number whose text isn't just what it counts as (-129412890473987, 12, 007, 00000000000000000, -0)
    public static boolean CountsAsDifferentNumber(String text){
        Integer counted = ToNumber(text);
        return counted != null && !text.trim().equals(String.valueOf(counted));
    }

    // The digits of a whole number with the minus and any leading zeros taken off. Empty means zero
    private static String DigitsWithoutZeros(String trimmed){
        String digits = trimmed.startsWith("-") ? trimmed.substring(1) : trimmed;
        int firstNonZero = 0;
        while(firstNonZero < digits.length() && digits.charAt(firstNonZero) == '0'){
            firstNonZero++;
        }
        return digits.substring(firstNonZero);
    }

    // Text a rating box can hold on the way to a valid value: blank, a lone minus, ?, or a whole number.
    // Anything else (5x, 1.5, +5, spaces) is refused as it is typed
    public static boolean IsTypable(String text){
        if(text == null || text.isEmpty() || text.equals("-")){
            return true;
        }
        return text.equals(text.trim()) && Classify(text) != RatingState.INVALID;
    }

    // Blank can only step up (to 0). A number steps within -1..11. A joke value only steps back to the nearest end
    // of the range, and ? doesn't step
    public static boolean CanStep(String current, int delta){
        switch(Classify(current)){
            case MISSING: return delta > 0;
            case NUMBER:
                if(IsOutOfRange(current)){
                    return ToNumber(current) == min ? delta > 0 : delta < 0;
                }
                int stepped = ToNumber(current) + delta;
                return stepped >= min && stepped <= max;
            default: return false;
        }
    }

    // The rating one step up (delta 1) or down (-1), or the same text if it can't step that way
    public static String Step(String current, int delta){
        if(!CanStep(current, delta)){
            return current;
        }
        switch(Classify(current)){
            case MISSING: return "0";
            default: return String.valueOf(IsOutOfRange(current) ? ToNumber(current) : ToNumber(current) + delta);
        }
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
