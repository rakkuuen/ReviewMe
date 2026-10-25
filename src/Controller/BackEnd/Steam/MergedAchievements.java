package Controller.BackEnd.Steam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Model.Library.Achievement;

// One game's achievements after the schema, the player's unlocks, the rarity and what was stored have been combined. The counts
// are worked out from the list, so they can't disagree with it. added/removed/newlyUnlocked say what changed against what was stored
public class MergedAchievements {
    private List<Achievement> achievements;
    private int unlockedCount;
    private Long lastUnlockAt;
    private List<String> added, removed, newlyUnlocked;

    public MergedAchievements(List<Achievement> achievements, List<String> added, List<String> removed, List<String> newlyUnlocked){
        this.achievements = Collections.unmodifiableList(new ArrayList<>(achievements));
        this.added = Collections.unmodifiableList(new ArrayList<>(added));
        this.removed = Collections.unmodifiableList(new ArrayList<>(removed));
        this.newlyUnlocked = Collections.unmodifiableList(new ArrayList<>(newlyUnlocked));
        for(Achievement achievement : achievements){
            if(achievement.IsUnlocked()){
                unlockedCount++;
                Long unlockedAt = achievement.GetUnlockedAt();
                if(unlockedAt != null && (lastUnlockAt == null || unlockedAt > lastUnlockAt)){
                    lastUnlockAt = unlockedAt;
                }
            }
        }
    }

    // In the schema's order
    public List<Achievement> GetAchievements(){ return achievements; }

    // A game with no achievements has a total of 0
    public int GetTotal(){ return achievements.size(); }
    public int GetUnlockedCount(){ return unlockedCount; }

    // When the most recent unlock happened (Steam's epoch seconds), or null if nothing is unlocked or no unlock has a time
    public Long GetLastUnlockAt(){ return lastUnlockAt; }

    // Api names of achievements that weren't stored before / were stored but are gone now / are unlocked now but weren't before
    public List<String> GetAdded(){ return added; }
    public List<String> GetRemoved(){ return removed; }
    public List<String> GetNewlyUnlocked(){ return newlyUnlocked; }
}
