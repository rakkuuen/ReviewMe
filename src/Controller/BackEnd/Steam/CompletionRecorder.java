package Controller.BackEnd.Steam;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Controller.BackEnd.Steam.CompletionAction.Kind;
import Database.Dao.AchievementDao;
import Database.Dao.CompletionDao;
import Database.Dao.GameReviewDao;
import Database.Dao.SteamGameDao;
import Model.Library.SteamGame;

// Records completions from the Steam data already stored (run the achievement sync first). A game at 100% on Steam gets its completion
// filled in; nothing already recorded is overwritten. Games below 100% are left for the user. Makes no Steam calls
// Run:  java -cp "bin;lib/*" Controller.BackEnd.Steam.CompletionRecorder [--write]   (without --write it only shows what it would do)
public class CompletionRecorder {
    private static final int listedPerKind = 15;

    // What would change, worked out from the database. Writes nothing
    public static List<CompletionAction> Plan(ZoneId zone){
        Map<Integer, SteamGame> steamById = new HashMap<>();
        for(SteamGame game : SteamGameDao.GetAllSteamGames()){
            steamById.put(game.GetAppId(), game);
        }
        return CompletionPlanner.Plan(GameReviewDao.GetAllGameReviews(), steamById, AchievementDao.GetLastUnlockTimes(), zone);
    }

    // Saves the changes in a plan, all together. False if nothing could be saved
    public static boolean Apply(List<CompletionAction> actions){
        return CompletionDao.Apply(actions);
    }

    public static int Count(List<CompletionAction> actions, Kind kind){
        int count = 0;
        for(CompletionAction action : actions){
            count += action.GetKind() == kind ? 1 : 0;
        }
        return count;
    }

    public static void main(String[] args){
        boolean write = args.length > 0 && args[0].equals("--write");
        GameReviewDao.Setup();
        List<CompletionAction> plan = Plan(ZoneId.systemDefault());
        Print(plan, write);
        if(write && !Apply(plan)){
            System.out.println("Couldn't save, nothing was changed.");
        }
    }

    private static void Print(List<CompletionAction> plan, boolean write){
        System.out.println((write ? "Recording" : "Dry run (nothing saved)") + ": " + plan.size() + " reviews are linked to a Steam game.");
        System.out.println("  completion recorded: " + Count(plan, Kind.RECORD) + " | missing date/baseline filled: " + Count(plan, Kind.FILL) + " | already complete: " + Count(plan, Kind.ALREADY_COMPLETE));
        System.out.println("  below 100% (left to you): " + Count(plan, Kind.BELOW_FULL) + " | no achievements: " + Count(plan, Kind.NO_ACHIEVEMENTS)
                + " | not synced yet: " + Count(plan, Kind.NO_STEAM_DATA) + " | shelved: " + Count(plan, Kind.SHELVED));
        ListKind(plan, Kind.RECORD, "newly completed");
        ListKind(plan, Kind.FILL, "filled in");
        if(!write){
            System.out.println("Add --write to save it.");
        }
    }

    private static void ListKind(List<CompletionAction> plan, Kind kind, String label){
        int shown = 0, total = Count(plan, kind);
        for(CompletionAction action : plan){
            if(action.GetKind() != kind){
                continue;
            }
            if(shown++ < listedPerKind){
                System.out.println("    " + label + ": " + action.GetReview().GetTitle() + " (date " + (action.GetCompletedOn() == null ? "unknown" : action.GetCompletedOn()) + ", baseline " + action.GetBaseline() + ")");
            }
        }
        if(total > listedPerKind){
            System.out.println("    ... and " + (total - listedPerKind) + " more " + label);
        }
    }
}
