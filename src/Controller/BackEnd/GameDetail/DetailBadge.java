package Controller.BackEnd.GameDetail;

// One badge under a game's title: what it says and how it should look
public class DetailBadge {

    public enum Look { STATE, REVIEW_OK, REVIEW_PLAIN, WARNING, MORE_ACHIEVEMENTS }

    private final String text;
    private final Look look;

    public DetailBadge(String text, Look look){
        this.text = text;
        this.look = look;
    }

    public String GetText(){ return text; }
    public Look GetLook(){ return look; }
}
