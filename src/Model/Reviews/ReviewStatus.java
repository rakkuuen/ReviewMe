package Model.Reviews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

// Where a review stands on both axes, whether it carries the More achievements tag, and exactly what is wrong with it
public class ReviewStatus {
    private GameState gameState;
    private ReviewState reviewState;
    private List<ReviewGap> gaps;
    private int newAchievements;   // Zero means no tag
    private String shelvedReason;  // Only set for a shelved game that has a reason

    public ReviewStatus(GameState gameState, ReviewState reviewState, List<ReviewGap> gaps, int newAchievements, String shelvedReason){
        this.gameState = gameState;
        this.reviewState = reviewState;
        this.gaps = Collections.unmodifiableList(new ArrayList<>(gaps));
        this.newAchievements = newAchievements;
        this.shelvedReason = shelvedReason;
    }

    public GameState GetGameState(){ return gameState; }
    public ReviewState GetReviewState(){ return reviewState; }

    // Kept even when the review isn't counted, so a muted badge can still say what's missing
    public List<ReviewGap> GetGaps(){ return gaps; }

    // A completed game that Steam now says has more achievements than when it was finished. It stays completed
    public boolean HasMoreAchievements(){ return newAchievements > 0; }
    public int GetNewAchievements(){ return newAchievements; }

    // Why the game was shelved, as typed. Null unless it's shelved with a reason
    public String GetShelvedReason(){ return shelvedReason; }

    // Only a finished game with an unfinished review is a warning. Shelved, in progress and not started games are exempt
    public boolean CountsAsIncomplete(){
        return gameState == GameState.COMPLETED && reviewState == ReviewState.REVIEW_INCOMPLETE;
    }

    // Every state-based tab this game appears in. Most games are in one; a completed game with an unfinished review and new
    // achievements is in two. Completed is only for fully reviewed games without the tag
    public Set<GameTab> Tabs(){
        Set<GameTab> tabs = EnumSet.noneOf(GameTab.class);
        switch(gameState){
            case SHELVED: tabs.add(GameTab.SHELVED); break;
            case IN_PROGRESS: tabs.add(GameTab.IN_PROGRESS); break;
            case NOT_STARTED: tabs.add(GameTab.NOT_STARTED); break;
            default:
                if(reviewState == ReviewState.REVIEW_INCOMPLETE){
                    tabs.add(GameTab.INCOMPLETE);
                }
                if(HasMoreAchievements()){
                    tabs.add(GameTab.MORE_ACHIEVEMENTS);
                } else if(reviewState == ReviewState.REVIEWED){
                    tabs.add(GameTab.COMPLETED);
                }
        }
        return tabs;
    }

    // "Story > Elena; Final Rating"
    public String DescribeGaps(){
        List<String> paths = new ArrayList<>();
        for(ReviewGap gap : gaps){
            paths.add(gap.GetPath());
        }
        return String.join("; ", paths);
    }
}
