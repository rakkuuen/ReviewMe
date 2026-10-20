package Screens.Detail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Model.Reviews.GameState;
import Model.Reviews.ReviewGap;
import Model.Reviews.ReviewState;
import Model.Reviews.ReviewStatus;

// Turns a review's status into one plain line, e.g. "Completed - Review incomplete: Story (empty: Elena, Daria)"
public class StatusText {
    private static final String between = " " + (char) 0x00B7 + " ";   // A middle dot, by code point so the source stays plain ASCII
    private static final String pathSeparator = " > ";

    // "Shelved (Too hard right now) - Review incomplete: Gameplay (not counted) - More achievements added +3"
    public static String Describe(ReviewStatus status){
        StringBuilder text = new StringBuilder(Label(status.GetGameState()));
        String reason = OneLine(status.GetShelvedReason());
        if(!reason.isEmpty()){
            text.append(" (").append(reason).append(")");
        }
        text.append(between).append(Label(status.GetReviewState()));
        if(!status.GetGaps().isEmpty()){
            text.append(": ").append(Gaps(status.GetGaps()));
            if(!status.CountsAsIncomplete()){
                text.append(" (not counted)");
            }
        }
        if(status.HasMoreAchievements()){
            text.append(between).append("More achievements added +").append(status.GetNewAchievements());
        }
        return text.toString();
    }

    // A reason is typed free text, so line breaks and runs of spaces are folded into single spaces
    private static String OneLine(String text){
        return text == null ? "" : text.trim().replaceAll("\\s+", " ");
    }

    public static String Label(GameState state){
        switch(state){
            case COMPLETED: return "Completed";
            case IN_PROGRESS: return "In progress";
            case SHELVED: return "Shelved";
            default: return "Not started";
        }
    }

    public static String Label(ReviewState state){
        switch(state){
            case REVIEWED: return "Reviewed";
            case REVIEW_INCOMPLETE: return "Review incomplete";
            default: return "Not reviewed";
        }
    }

    // A missing section by its heading, the empty sub-headings of one field together, and the manual flag as it is:
    // "Gameplay; Story (empty: Elena, Daria); marked incomplete by you"
    private static String Gaps(List<ReviewGap> gaps){
        Map<String, List<String>> emptyByLabel = new LinkedHashMap<>();
        for(ReviewGap gap : gaps){
            if(gap.GetKind() == ReviewGap.Kind.EMPTY_SUBSECTION){
                int split = gap.GetPath().indexOf(pathSeparator);
                String heading = gap.GetPath().substring(0, split);
                emptyByLabel.computeIfAbsent(heading, key -> new ArrayList<>()).add(gap.GetPath().substring(split + pathSeparator.length()));
            } else {
                emptyByLabel.putIfAbsent(gap.GetPath(), null);
            }
        }

        List<String> parts = new ArrayList<>();
        for(Map.Entry<String, List<String>> entry : emptyByLabel.entrySet()){
            parts.add(entry.getValue() == null ? entry.getKey() : entry.getKey() + " (empty: " + String.join(", ", entry.getValue()) + ")");
        }
        return String.join("; ", parts);
    }
}
