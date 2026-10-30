package Controller.BackEnd.Reviews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Database.Dao.GameReviewDao;
import Database.Dao.ReviewLinkDao;
import Model.Reviews.GameReview;
import Model.Reviews.SteamLinkChange;

// Sets which Steam game a review is about, and renames reviews (usually to match the Steam name). Everything is checked first and
// saved as one step: if any change is refused, none is made. A Steam game can belong to only one review, and no two reviews can
// share a title (ignoring case). The Steam game doesn't have to be in the owned library
public class SteamLinker {
    public static final int maxTitleLength = 200;

    public static SaveResult Link(int reviewId, int steamAppId){ return Apply(List.of(SteamLinkChange.Link(reviewId, steamAppId))); }
    public static SaveResult Unlink(int reviewId){ return Apply(List.of(SteamLinkChange.Unlink(reviewId))); }
    public static SaveResult Rename(int reviewId, String newTitle){ return Apply(List.of(SteamLinkChange.Rename(reviewId, newTitle))); }

    public static SaveResult Apply(List<SteamLinkChange> changes){
        SaveResult result = new SaveResult();
        if(changes.isEmpty()){
            return result;
        }
        Map<Integer, GameReview> byId = new HashMap<>();
        for(GameReview review : GameReviewDao.GetAllGameReviews()){
            byId.put(review.GetId(), review);
        }
        Check(changes, byId, result);
        if(!result.Succeeded()){
            return result;
        }
        if(!ReviewLinkDao.Apply(WithCleanTitles(changes))){
            result.AddError("Couldn't save the changes, nothing was changed");
        }
        return result;
    }

    // Each problem is added to the result, so one pass tells the user everything that's wrong
    private static void Check(List<SteamLinkChange> changes, Map<Integer, GameReview> byId, SaveResult result){
        Set<Integer> seen = new HashSet<>();
        Map<Integer, String> finalTitle = new HashMap<>();   // reviews being renamed
        Map<Integer, Integer> finalLink = new HashMap<>();   // reviews whose link is being set (value null = removed)
        for(SteamLinkChange change : changes){
            GameReview review = byId.get(change.GetReviewId());
            if(review == null){
                result.AddError("There is no review with id " + change.GetReviewId());
            } else if(!seen.add(change.GetReviewId())){
                result.AddError(review.GetTitle() + " appears more than once in the changes");
            } else {
                CheckTitle(change, review, result);
                CheckLink(change, review, result);
                if(change.GetNewTitle() != null){
                    finalTitle.put(change.GetReviewId(), CleanTitle(change.GetNewTitle()));
                }
                if(change.SetsLink()){
                    finalLink.put(change.GetReviewId(), change.GetSteamAppId());
                }
            }
        }
        if(result.Succeeded()){
            CheckTitlesStayUnique(byId, finalTitle, result);
            CheckLinksStayUnique(byId, finalLink, result);
        }
    }

    private static void CheckTitle(SteamLinkChange change, GameReview review, SaveResult result){
        if(change.GetNewTitle() == null){
            return;
        }
        String cleaned = CleanTitle(change.GetNewTitle());
        if(cleaned.isEmpty()){
            result.AddError("Give " + review.GetTitle() + " a title");
        } else if(cleaned.length() > maxTitleLength){
            result.AddError("The new title for " + review.GetTitle() + " is over " + maxTitleLength + " characters");
        }
    }

    private static void CheckLink(SteamLinkChange change, GameReview review, SaveResult result){
        if(change.SetsLink() && change.GetSteamAppId() != null && change.GetSteamAppId() <= 0){
            result.AddError("Steam app ids are positive numbers (" + review.GetTitle() + ")");
        }
    }

    // A new title can't match any other review's current title or another new title. (Swapping two titles in one go is refused too)
    private static void CheckTitlesStayUnique(Map<Integer, GameReview> byId, Map<Integer, String> finalTitle, SaveResult result){
        Map<String, Integer> claimed = new HashMap<>();
        for(Map.Entry<Integer, String> entry : finalTitle.entrySet()){
            String lower = entry.getValue().toLowerCase();
            if(claimed.containsKey(lower)){
                result.AddError("Two reviews would both be called " + entry.getValue());
            }
            claimed.put(lower, entry.getKey());
        }
        for(GameReview other : byId.values()){
            Integer claimant = claimed.get(other.GetTitle().toLowerCase());
            if(claimant != null && !claimant.equals(other.GetId())){
                result.AddError("There is already a review called " + other.GetTitle());
            }
        }
    }

    // After the changes, no Steam game may belong to two reviews
    private static void CheckLinksStayUnique(Map<Integer, GameReview> byId, Map<Integer, Integer> finalLink, SaveResult result){
        Map<Integer, GameReview> owner = new HashMap<>();
        for(GameReview review : byId.values()){
            Integer appId = finalLink.containsKey(review.GetId()) ? finalLink.get(review.GetId()) : review.GetSteamAppId();
            if(appId == null){
                continue;
            }
            GameReview already = owner.get(appId);
            if(already != null && (finalLink.containsKey(review.GetId()) || finalLink.containsKey(already.GetId()))){
                result.AddError("Steam game " + appId + " would belong to both " + already.GetTitle() + " and " + review.GetTitle());
            }
            owner.put(appId, review);
        }
    }

    // Titles are saved the way they were checked: trimmed, with runs of spaces made single
    private static List<SteamLinkChange> WithCleanTitles(List<SteamLinkChange> changes){
        List<SteamLinkChange> cleaned = new ArrayList<>();
        for(SteamLinkChange change : changes){
            cleaned.add(change.GetNewTitle() == null ? change : change.WithNewTitle(CleanTitle(change.GetNewTitle())));
        }
        return cleaned;
    }

    private static String CleanTitle(String title){
        return title.trim().replaceAll("\\s+", " ");
    }
}
