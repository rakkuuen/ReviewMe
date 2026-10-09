package Controller.BackEnd.Importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Controller.BackEnd.Importer.MappedReview.TagState;

// Sorts the tags line (#Review #Game #PC [#Series] #State) into platforms, series and state
public class TagReader {
    private static final Set<String> platformTags = new HashSet<>(Arrays.asList("pc", "switch", "ds", "wii", "gameboy", "gamecube", "ps4", "ps5", "xbox", "mobile", "mac", "psp"));

    public static boolean IsPlatformTag(String lowerCaseName){
        return platformTags.contains(lowerCaseName);
    }

    // Problems are added to issues
    public static TagInfo Read(List<String> tokens, String file, List<ImportIssue> issues){
        List<String> platforms = new ArrayList<>();
        List<String> seriesTags = new ArrayList<>();
        int inProgressTags = 0, incompleteTags = 0, completedTags = 0;

        for(String token : tokens){
            String name = StripHash(token);
            String lower = name.toLowerCase();
            if(lower.equals("review") || lower.equals("game")){
                continue;
            }
            if(lower.equals("gameinprogress")){
                inProgressTags++;
            } else if(lower.equals("reviewincomplete")){
                incompleteTags++;
            } else if(lower.equals("completed")){
                completedTags++;
            } else if(lower.equals("incomplete")){
                issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "old #Incomplete tag, remove it"));
            } else if(IsPlatformTag(lower)){
                platforms.add(name);
            } else {
                seriesTags.add(name);   // anything left over is the series
            }
        }

        TagState tagState = ResolveState(inProgressTags, incompleteTags, completedTags, file, issues);
        if(seriesTags.size() > 1){
            issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "more than one series tag: " + String.join(", ", seriesTags)));
        }

        // #REVIEWINCOMPLETE still means the game is completed. In progress wins over completed
        boolean inProgress = inProgressTags > 0;
        boolean completed = inProgressTags == 0 && (completedTags > 0 || incompleteTags > 0);
        return new TagInfo(platforms, seriesTags.isEmpty() ? null : seriesTags.get(0), tagState, inProgress, completed);
    }

    // Exactly one state tag is expected
    private static TagState ResolveState(int inProgressTags, int incompleteTags, int completedTags, String file, List<ImportIssue> issues){
        int stateTags = (inProgressTags > 0 ? 1 : 0) + (incompleteTags > 0 ? 1 : 0) + (completedTags > 0 ? 1 : 0);
        if(stateTags == 0){
            issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "no state tag (#Completed, #REVIEWINCOMPLETE or #GameInProgress)"));
            return TagState.NONE;
        }
        if(stateTags > 1){
            issues.add(new ImportIssue(file, IssueSeverity.PROBLEM, "more than one state tag"));
            return TagState.CONFLICT;
        }
        if(inProgressTags > 0){
            return TagState.IN_PROGRESS;
        }
        return incompleteTags > 0 ? TagState.REVIEW_INCOMPLETE : TagState.COMPLETED;
    }

    public static String StripHash(String token){
        return token.startsWith("#") ? token.substring(1) : token;
    }

    public static List<String> StripHashes(List<String> tokens){
        List<String> names = new ArrayList<>();
        for(String token : tokens){
            String name = StripHash(token);
            if(!name.isEmpty()){
                names.add(name);
            }
        }
        return Collections.unmodifiableList(names);
    }
}
