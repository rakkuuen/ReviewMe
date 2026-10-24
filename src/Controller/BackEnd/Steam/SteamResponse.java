package Controller.BackEnd.Steam;

// What Steam answered: the HTTP status and the body as text. Steam puts useful JSON in some error answers too (a game with no
// stats is a 400, a private profile a 403), so the parsers get both and decide
public class SteamResponse {
    private int status;
    private String body;

    public SteamResponse(int status, String body){
        this.status = status;
        this.body = body;
    }

    public int GetStatus(){ return status; }
    public String GetBody(){ return body; }
}
