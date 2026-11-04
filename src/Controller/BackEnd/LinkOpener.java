package Controller.BackEnd;

import java.awt.Desktop;
import java.net.URI;
import java.util.Locale;

// Opens a link from a review in the user's browser. Only web addresses (http and https) are ever opened
public class LinkOpener {
    private static final String http = "http";
    private static final String https = "https";

    // Is this an http or https address with a host? (The reader only keeps those, but a link is checked again before it is opened)
    public static boolean IsWebAddress(String address){
        if(address == null){
            return false;
        }
        try {
            URI uri = new URI(address.trim());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            return (scheme.equals(http) || scheme.equals(https)) && uri.getHost() != null;
        } catch (java.net.URISyntaxException e) {
            return false;
        }
    }

    // True if the browser was asked to open it
    public static boolean Open(String address){
        if(!IsWebAddress(address) || !Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){
            return false;
        }
        try {
            Desktop.getDesktop().browse(new URI(address.trim()));
            return true;
        } catch (java.io.IOException | java.net.URISyntaxException | RuntimeException e) {
            return false;
        }
    }
}
