package Controller.BackEnd.Steam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// The parsed GetPlayerAchievements answer for one game. Outcome says why there may be nothing: the game has no stats at all, the
// profile isn't public to this caller, or Steam said something else (message has Steam's own words)
public class PlayerAchievements {
    public enum Outcome { OK, NO_STATS, NOT_PUBLIC, UNAVAILABLE }

    private Outcome outcome;
    private String message;
    private List<PlayerAchievement> achievements;

    public PlayerAchievements(Outcome outcome, String message, List<PlayerAchievement> achievements){
        this.outcome = outcome;
        this.message = message;
        this.achievements = Collections.unmodifiableList(new ArrayList<>(achievements));
    }

    public Outcome GetOutcome(){ return outcome; }

    // Steam's error text, or null when the outcome is OK
    public String GetMessage(){ return message; }

    // Empty unless OK. OK with an empty list is a game that has stats but no achievements
    public List<PlayerAchievement> GetAchievements(){ return achievements; }

    public int GetUnlockedCount(){
        int unlocked = 0;
        for(PlayerAchievement achievement : achievements){
            if(achievement.IsUnlocked()){
                unlocked++;
            }
        }
        return unlocked;
    }
}
