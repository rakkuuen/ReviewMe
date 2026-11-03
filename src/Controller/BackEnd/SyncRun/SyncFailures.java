package Controller.BackEnd.SyncRun;

import java.io.IOException;

import Controller.BackEnd.Steam.SteamException;

// Works out which kind of failure a sync problem is, from what the Steam code reports (the exception's words)
public class SyncFailures {
    private static final String httpMarker = "HTTP ";
    private static final String notSetMarker = "is not set in";
    private static final String couldntReadMarker = "Couldn't read";

    public static SyncState Classify(IOException problem){
        String message = problem.getMessage() == null ? "" : problem.getMessage();
        if(!(problem instanceof SteamException)){
            // Reading the key and id from local.properties failed: nothing is set up yet
            return message.contains(notSetMarker) || message.contains(couldntReadMarker) ? SyncState.INVALID_KEY : SyncState.FAILED;
        }
        int at = message.indexOf(httpMarker);
        if(at >= 0){
            int status = StatusAt(message, at + httpMarker.length());
            if(status == 401 || status == 403){
                return SyncState.INVALID_KEY;
            }
            if(status == 429){
                return SyncState.RATE_LIMITED;
            }
            return SyncState.FAILED;
        }
        if(message.contains("interrupted")){
            return SyncState.FAILED;
        }
        return SyncState.OFFLINE;   // Failed after its tries with no answer at all
    }

    // The number that starts at that place in the text, or zero
    private static int StatusAt(String text, int from){
        int end = from;
        while(end < text.length() && Character.isDigit(text.charAt(end))){
            end++;
        }
        return end == from ? 0 : Integer.parseInt(text.substring(from, end));
    }
}
