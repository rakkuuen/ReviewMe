package Controller.BackEnd.Steam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Model.Library.SteamGame;

// The parsed owned-games answer. detailsHidden means Steam sent back nothing at all (no count), which is what it does when the
// profile's game details are private to whoever is asking. A real library of zero games still has a count of 0
public class OwnedGamesResult {
    private List<SteamGame> games;
    private boolean detailsHidden;

    public OwnedGamesResult(List<SteamGame> games, boolean detailsHidden){
        this.games = Collections.unmodifiableList(new ArrayList<>(games));
        this.detailsHidden = detailsHidden;
    }

    public List<SteamGame> GetGames(){ return games; }
    public boolean AreDetailsHidden(){ return detailsHidden; }
}
