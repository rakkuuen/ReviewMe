package Controller.BackEnd.Overview;

// One bar of the completed-per-year chart: its label, its count, and how tall it is as a share of the tallest bar (0 to 1)
public class YearBar {
    private final int year;
    private final String label;
    private final int count;
    private final double fraction;

    public YearBar(int year, String label, int count, double fraction){
        this.year = year;
        this.label = label;
        this.count = count;
        this.fraction = fraction;
    }

    public int GetYear(){ return year; }
    public String GetLabel(){ return label; }
    public int GetCount(){ return count; }
    public double GetFraction(){ return fraction; }
}
