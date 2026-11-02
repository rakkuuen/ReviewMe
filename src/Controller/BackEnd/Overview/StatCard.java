package Controller.BackEnd.Overview;

// One figure on the Overview: the big value, what it is, and an optional line under that (null for none). A card can be wider than
// the others (span 2) when it has more to show
public class StatCard {
    private final String value;
    private final String label;
    private final String detail;
    private final boolean showsYearChart;
    private final int span;

    public StatCard(String value, String label, String detail, boolean showsYearChart, int span){
        this.value = value;
        this.label = label;
        this.detail = detail;
        this.showsYearChart = showsYearChart;
        this.span = span;
    }

    public String GetValue(){ return value; }
    public String GetLabel(){ return label; }
    public String GetDetail(){ return detail; }

    // The Completed card carries the per-year bars
    public boolean ShowsYearChart(){ return showsYearChart; }

    // How many card-widths it takes up
    public int GetSpan(){ return span; }
}
