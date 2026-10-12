package Model.Reviews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Where a review stands on both axes, and exactly what is wrong with it
public class ReviewStatus {
    private GameState gameState;
    private ReviewState reviewState;
    private List<ReviewGap> gaps;

    public ReviewStatus(GameState gameState, ReviewState reviewState, List<ReviewGap> gaps){
        this.gameState = gameState;
        this.reviewState = reviewState;
        this.gaps = Collections.unmodifiableList(new ArrayList<>(gaps));
    }

    public GameState GetGameState(){ return gameState; }
    public ReviewState GetReviewState(){ return reviewState; }

    // Kept even when the review isn't counted, so a muted badge can still say what's missing
    public List<ReviewGap> GetGaps(){ return gaps; }

    // Only a finished game with an unfinished review is a warning. In progress, not completed and not started games are exempt
    public boolean CountsAsIncomplete(){
        return gameState == GameState.COMPLETED && reviewState == ReviewState.REVIEW_INCOMPLETE;
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
