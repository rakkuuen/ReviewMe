package Controller.BackEnd.SeriesView;

import Controller.BackEnd.Overview.GameRow;

// One game in a series' page: where it sits in the order, its row, and a tag when it uses a different template from the series
public class SeriesGameItem {
    private final String position;
    private final GameRow row;
    private final String templateTag;

    public SeriesGameItem(String position, GameRow row, String templateTag){
        this.position = position;
        this.row = row;
        this.templateTag = templateTag;
    }

    public String GetPosition(){ return position; }
    public GameRow GetRow(){ return row; }

    // The template's name when it differs from the series', otherwise null
    public String GetTemplateTag(){ return templateTag; }
}
