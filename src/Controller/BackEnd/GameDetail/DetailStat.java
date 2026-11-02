package Controller.BackEnd.GameDetail;

// One figure in the strip under a game's title: a label, the value, an optional note under it ("first seen by ReviewMe"), an optional
// small suffix after the value ("/ 79"), and for achievements a fraction to fill a bar with (null for none)
public class DetailStat {
    private final String label;
    private final String value;
    private final String suffix;
    private final String note;
    private final Double fraction;
    private final boolean hasViewAll;

    public DetailStat(String label, String value, String suffix, String note, Double fraction, boolean hasViewAll){
        this.label = label;
        this.value = value;
        this.suffix = suffix;
        this.note = note;
        this.fraction = fraction;
        this.hasViewAll = hasViewAll;
    }

    public String GetLabel(){ return label; }
    public String GetValue(){ return value; }
    public String GetSuffix(){ return suffix; }
    public String GetNote(){ return note; }
    public Double GetFraction(){ return fraction; }

    // The achievements figure links to the full list
    public boolean HasViewAll(){ return hasViewAll; }
}
