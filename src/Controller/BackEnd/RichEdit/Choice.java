package Controller.BackEnd.RichEdit;

import java.util.Objects;

// What a style button shows for the selection: off, on, or mixed (some of it is). For a colour or font, value is the one in use
// (null for the default) when the whole selection has the same. Immutable
public final class Choice {
    public enum Mode { OFF, ON, MIXED }

    private final Mode mode;
    private final String value;

    private Choice(Mode mode, String value){
        this.mode = mode;
        this.value = value;
    }

    public static Choice Off(){ return new Choice(Mode.OFF, null); }
    public static Choice On(){ return new Choice(Mode.ON, null); }
    public static Choice Mixed(){ return new Choice(Mode.MIXED, null); }
    public static Choice Of(boolean on){ return on ? On() : Off(); }

    // A colour, font or link: the value in use, or null for none
    public static Choice Value(String value){ return new Choice(value == null ? Mode.OFF : Mode.ON, value); }

    public Mode GetMode(){ return mode; }
    public String GetValue(){ return value; }
    public boolean IsOn(){ return mode == Mode.ON; }
    public boolean IsMixed(){ return mode == Mode.MIXED; }

    @Override
    public boolean equals(Object other){
        return other instanceof Choice && ((Choice) other).mode == mode && Objects.equals(((Choice) other).value, value);
    }

    @Override
    public int hashCode(){
        return Objects.hash(mode, value);
    }

    @Override
    public String toString(){
        return mode + (value == null ? "" : "(" + value + ")");
    }
}
