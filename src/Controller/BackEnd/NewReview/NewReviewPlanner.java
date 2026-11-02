package Controller.BackEnd.NewReview;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import Controller.BackEnd.Library.UnreviewedGameRules;
import Controller.BackEnd.SeriesManagement.SeriesChecker;
import Controller.BackEnd.SeriesManagement.SeriesGamesPlanner;
import Model.Reviews.CompletionSource;
import Model.Reviews.GameReview;
import Model.Reviews.Series;

// Works out the review a New review request would create, and what is wrong with the request. No database in here: what exists is
// handed in. A new review has no sections filled in and no optional section ticked (see OptionalSections), takes its series' template
// (Standard otherwise) and goes to the end of its series
public class NewReviewPlanner {
    public static final int maxTitleLength = 100;

    // existingReviews are the reviews that exist, templateIds the templates that exist
    public static NewReviewPlan Plan(NewReviewRequest request, List<GameReview> existingReviews, List<Series> allSeries, Set<String> templateIds){
        List<String> errors = new ArrayList<>();
        String title = SeriesChecker.Clean(request.GetTitle());
        if(title.isEmpty()){
            errors.add("Give the game a title");
        } else if(title.length() > maxTitleLength){
            errors.add("The title can be up to " + maxTitleLength + " characters");
        }
        for(GameReview other : existingReviews){
            if(!title.isEmpty() && other.GetTitle().equalsIgnoreCase(title)){
                errors.add("There is already a review called " + other.GetTitle());
            }
            if(request.GetSteamAppId() != null && request.GetSteamAppId().equals(other.GetSteamAppId())){
                errors.add("This game already has a review: " + other.GetTitle());
            }
        }

        Series series = null;
        if(request.GetSeriesId() != null){
            series = Find(request.GetSeriesId(), allSeries);
            if(series == null){
                errors.add("There is no series with the tag " + request.GetSeriesId());
            }
        }
        String templateId = series != null && series.GetTemplateId() != null ? series.GetTemplateId() : SeriesGamesPlanner.standardTemplateId;
        if(!templateIds.contains(templateId)){
            errors.add("There is no template called " + templateId);
        }

        String reason = UnreviewedGameRules.CleanReason(request.GetShelvedReason());
        if(request.IsShelved() && UnreviewedGameRules.ReasonTooLong(reason)){
            errors.add("The reason can be at most " + UnreviewedGameRules.maxReasonLength + " characters");
        }
        String completedOn = UnreviewedGameRules.CleanDate(request.GetCompletedOn());
        if(request.IsCompleted() && completedOn != null && !UnreviewedGameRules.IsRealDate(completedOn)){
            errors.add("The completed date needs to be like 2025-03-02");
        }
        if(!errors.isEmpty()){
            return NewReviewPlan.Refused(errors);
        }

        GameReview review = new GameReview();
        review.SetTitle(title);
        review.SetTemplateId(templateId);
        review.SetSteamAppId(request.GetSteamAppId());
        if(series != null){
            review.SetSeriesId(series.GetId());
            review.SetSeriesPosition(NextPosition(series.GetId(), existingReviews));
        }
        review.SetShelved(request.IsShelved());
        review.SetShelvedReason(request.IsShelved() ? reason : null);
        review.SetCompleted(request.IsCompleted());
        review.SetCompletedOn(request.IsCompleted() ? completedOn : null);
        review.SetCompletedOnSource(request.IsCompleted() && completedOn != null ? CompletionSource.MANUAL : null);
        review.SetInProgress(request.GetSteamAppId() == null && request.IsInProgress() && !request.IsCompleted() && !request.IsShelved());
        return NewReviewPlan.Of(review);
    }

    private static Series Find(String id, List<Series> allSeries){
        for(Series series : allSeries){
            if(series.GetId().equals(id)){
                return series;
            }
        }
        return null;
    }

    // One after the highest place any game in the series has (1 for the first)
    private static int NextPosition(String seriesId, List<GameReview> existing){
        int highest = 0;
        for(GameReview other : existing){
            if(seriesId.equals(other.GetSeriesId()) && other.GetSeriesPosition() != null){
                highest = Math.max(highest, other.GetSeriesPosition());
            }
        }
        return highest + 1;
    }
}
