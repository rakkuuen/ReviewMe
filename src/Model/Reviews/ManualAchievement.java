package Model.Reviews;

// An achievement the user keeps by hand for one review: a game outside Steam, or an extra goal on a Steam game. unlockedOn is a
// date typed in (like 2025-03-02), or null. position is its place in that review's list, counting from 1
public class ManualAchievement {
    private Integer id;
    private int reviewId, position;
    private String title, description, unlockedOn;
    private boolean unlocked;

    public ManualAchievement(Integer id, int reviewId, String title, String description, boolean unlocked, String unlockedOn, int position){
        this.id = id;
        this.reviewId = reviewId;
        this.title = title;
        this.description = description;
        this.unlocked = unlocked;
        this.unlockedOn = unlockedOn;
        this.position = position;
    }

    // Null until it has been stored
    public Integer GetId(){ return id; }
    public int GetReviewId(){ return reviewId; }
    public String GetTitle(){ return title; }
    public String GetDescription(){ return description; }
    public boolean IsUnlocked(){ return unlocked; }
    public String GetUnlockedOn(){ return unlockedOn; }
    public int GetPosition(){ return position; }
}
