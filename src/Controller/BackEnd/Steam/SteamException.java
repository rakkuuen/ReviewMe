package Controller.BackEnd.Steam;

import java.io.IOException;

// A Steam call that couldn't be completed (no network, still failing after retries). The message never contains the API key
public class SteamException extends IOException {
    public SteamException(String message){
        super(message);
    }
}
