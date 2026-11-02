package Controller.BackEnd.ListTab;

// Something that can be done to a game from its row in a list
public enum RowAction {
    WRITE_REVIEW("Write review"),
    SHELVE("Shelve..."),
    UNSHELVE("Unshelve"),
    MARK_COMPLETED("Mark completed"),
    EDIT_REASON("Edit reason");

    private final String label;

    RowAction(String label){
        this.label = label;
    }

    public String GetLabel(){ return label; }
}
