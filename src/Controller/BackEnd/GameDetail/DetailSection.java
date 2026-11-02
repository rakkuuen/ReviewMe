package Controller.BackEnd.GameDetail;

import java.util.Collections;
import java.util.List;

// One section of the review as the page shows it: its heading, what is in it, and whether it is filled in, missing or not applicable.
// A rating section carries the rating as typed (the pill) and the note under it; a text section carries its text and its parts
public class DetailSection {

    public enum State { FILLED, MISSING, EMPTY, NOT_APPLICABLE }   // EMPTY: nothing written, but the review does not need it

    private final String fieldKey;
    private final String heading;
    private final State state;
    private final boolean isRating;
    private final String text;
    private final String comment;
    private final boolean unknownRating;
    private final List<DetailPart> parts;

    public DetailSection(String fieldKey, String heading, State state, boolean isRating, String text, String comment, boolean unknownRating, List<DetailPart> parts){
        this.fieldKey = fieldKey;
        this.heading = heading;
        this.state = state;
        this.isRating = isRating;
        this.text = text;
        this.comment = comment;
        this.unknownRating = unknownRating;
        this.parts = Collections.unmodifiableList(parts);
    }

    public String GetFieldKey(){ return fieldKey; }
    public String GetHeading(){ return heading; }
    public State GetState(){ return state; }
    public boolean IsRating(){ return isRating; }

    // The text, or for a rating the number as typed. Empty when there is nothing
    public String GetText(){ return text; }

    // The note under a rating (empty for none)
    public String GetComment(){ return comment; }

    // A rating that is "?"
    public boolean IsUnknownRating(){ return unknownRating; }

    public List<DetailPart> GetParts(){ return parts; }
}
