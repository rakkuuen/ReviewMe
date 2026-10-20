package Controller.BackEnd.Importer.Report;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Controller.BackEnd.Importer.Results.ImportResult;
import Controller.BackEnd.Importer.Results.MappedReview;
import Controller.BackEnd.Importer.Results.MappedReview.TagState;
import Controller.BackEnd.Reviews.ReviewStatusCalculator;
import Model.Reviews.GameState;
import Model.Reviews.GameTab;
import Model.Reviews.ReviewState;
import Model.Reviews.ReviewStatus;
import Model.Reviews.Template;

// What the app would say about each review, worked out from its content (the tags only say what the md claims)
public class StatusSection implements ReportSection {

    @Override
    public List<String> Lines(ImportResult result){
        Map<String, Integer> combinations = new LinkedHashMap<>();
        List<String> warnings = new ArrayList<>();
        List<String> muted = new ArrayList<>();
        List<String> completedTagButIncomplete = new ArrayList<>();
        List<String> incompleteTagButReviewed = new ArrayList<>();
        List<String> flaggedByHand = new ArrayList<>();
        Map<GameTab, Integer> tabCounts = new EnumMap<>(GameTab.class);

        for(MappedReview mapped : result.GetReviews()){
            String title = mapped.GetReview().GetTitle();
            Template template = result.GetTemplates().get(mapped.GetReview().GetTemplateId());
            ReviewStatus status = ReviewStatusCalculator.Calculate(mapped.GetReview(), template, null);
            String gaps = mapped.GetFilled() == 0 ? "all sections blank" : status.DescribeGaps();

            combinations.merge(Label(status.GetGameState()) + " + " + Label(status.GetReviewState()), 1, Integer::sum);
            for(GameTab tab : status.Tabs()){
                tabCounts.merge(tab, 1, Integer::sum);
            }
            if(status.CountsAsIncomplete()){
                warnings.add(title + ": " + gaps);
            } else if(!status.GetGaps().isEmpty()){
                muted.add(title + " (" + Label(status.GetGameState()) + "): " + gaps);
            }

            if(mapped.GetReview().IsReviewIncompleteFlagged()){
                flaggedByHand.add(title);
            }
            if(mapped.GetTagState() == TagState.COMPLETED && status.GetReviewState() == ReviewState.REVIEW_INCOMPLETE){
                completedTagButIncomplete.add(title + " (" + gaps + ")");
            } else if(mapped.GetTagState() == TagState.REVIEW_INCOMPLETE && status.GetReviewState() == ReviewState.REVIEWED){
                incompleteTagButReviewed.add(title);
            }
        }

        List<String> out = new ArrayList<>();
        out.add("== STATE (worked out from the content) ==");
        for(Map.Entry<String, Integer> entry : combinations.entrySet()){
            out.add("    " + entry.getKey() + ": " + entry.getValue());
        }
        out.add("main view tabs (a game can be in more than one; no Steam data yet, so no More achievements or unsynced states):");
        for(GameTab tab : GameTab.values()){
            out.add("    " + Label(tab) + ": " + tabCounts.getOrDefault(tab, 0));
        }
        out.add("counts as an Incomplete warning (completed game, unfinished review): " + warnings.size());
        for(String warning : warnings){
            out.add("    " + ReportFormat.Shorten(warning, 150));
        }
        out.add("unfinished but exempt (not a completed game): " + muted.size());
        for(String line : muted){
            out.add("    " + ReportFormat.Shorten(line, 150));
        }
        out.add("tagged #Completed but the content says incomplete: " + Describe(completedTagButIncomplete));
        out.add("tagged #REVIEWINCOMPLETE with nothing blank, stored as the manual flag: " + Describe(flaggedByHand));
        out.add("tagged #REVIEWINCOMPLETE but the content says reviewed (should always be none): " + Describe(incompleteTagButReviewed));
        out.add("");
        return out;
    }

    private String Label(GameState state){
        return state.name().replace('_', ' ').toLowerCase();
    }

    private String Label(ReviewState state){
        return state.name().replace('_', ' ').toLowerCase();
    }

    private String Label(GameTab tab){
        return tab.name().replace('_', ' ').toLowerCase();
    }

    private String Describe(List<String> items){
        return items.isEmpty() ? "none" : items.size() + " -> " + String.join(", ", items);
    }
}
