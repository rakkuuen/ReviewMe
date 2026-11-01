package Controller.BackEnd.Overview;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import Model.Library.LibraryGame;

// Puts the Overview together from the games already read. Pure: the same input always gives the same Overview
public class OverviewCalculator {

    public static OverviewData Calculate(OverviewInput input){
        List<LibraryGame> games = input.GetGames();
        Map<OverviewSectionKind, OverviewSection> sections = new EnumMap<>(OverviewSectionKind.class);
        for(OverviewSectionKind kind : OverviewSectionKind.values()){
            sections.put(kind, OverviewSectionBuilder.Build(games, kind, input.GetRowsPerSection()));
        }
        return new OverviewData(
                OverviewStatsCalculator.Calculate(games, input.GetManualUnlockedByReviewId()),
                TopPicks(games, input.GetTopPickSlots()),
                sections,
                OverviewSeriesBuilder.Build(input.GetSeries(), games, input.GetRowsPerSection()),
                input.GetSeries().size());
    }

    // One entry per slot, 1 to slotCount. A pick in a slot outside that range is ignored, and if two games claim a slot the first wins
    private static List<LibraryGame> TopPicks(List<LibraryGame> games, int slotCount){
        List<LibraryGame> picks = new ArrayList<>();
        for(int slot = 0; slot < Math.max(slotCount, 0); slot++){
            picks.add(null);
        }
        for(LibraryGame game : games){
            Integer slot = game.HasReview() ? game.GetReview().GetTopPickSlot() : null;
            if(slot != null && slot >= 1 && slot <= picks.size() && picks.get(slot - 1) == null){
                picks.set(slot - 1, game);
            }
        }
        return picks;
    }
}
