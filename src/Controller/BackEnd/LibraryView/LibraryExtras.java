package Controller.BackEnd.LibraryView;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import Controller.BackEnd.Reviews.LibraryDates;
import Model.Library.LibraryGame;
import Model.Reviews.GameReview;

// The Library's other filters: series, genres, platforms (each one or more picked; a game needs one of the ones picked in each kind
// that is used) and the date it was completed and the date it was added to the library. Immutable
public class LibraryExtras {
    private final Set<String> seriesIds;
    private final Set<String> genres;
    private final Set<String> platforms;
    private final DateRange completed;
    private final DateRange added;

    private LibraryExtras(Set<String> seriesIds, Set<String> genres, Set<String> platforms, DateRange completed, DateRange added){
        this.seriesIds = Collections.unmodifiableSet(new LinkedHashSet<>(seriesIds));
        this.genres = Collections.unmodifiableSet(new LinkedHashSet<>(genres));
        this.platforms = Collections.unmodifiableSet(new LinkedHashSet<>(platforms));
        this.completed = completed;
        this.added = added;
    }

    public static LibraryExtras None(){
        return new LibraryExtras(new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), DateRange.None(), DateRange.None());
    }

    public Set<String> GetSeriesIds(){ return seriesIds; }
    public Set<String> GetGenres(){ return genres; }
    public Set<String> GetPlatforms(){ return platforms; }
    public DateRange GetCompleted(){ return completed; }
    public DateRange GetAdded(){ return added; }

    public boolean IsEmpty(){
        return seriesIds.isEmpty() && genres.isEmpty() && platforms.isEmpty() && !completed.IsSet() && !added.IsSet();
    }

    public LibraryExtras WithSeries(String id, boolean on){ return new LibraryExtras(Toggled(seriesIds, id, on), genres, platforms, completed, added); }
    public LibraryExtras WithGenre(String genre, boolean on){ return new LibraryExtras(seriesIds, Toggled(genres, genre, on), platforms, completed, added); }
    public LibraryExtras WithPlatform(String platform, boolean on){ return new LibraryExtras(seriesIds, genres, Toggled(platforms, platform, on), completed, added); }
    public LibraryExtras WithCompleted(DateRange range){ return new LibraryExtras(seriesIds, genres, platforms, range, added); }
    public LibraryExtras WithAdded(DateRange range){ return new LibraryExtras(seriesIds, genres, platforms, completed, range); }
    public LibraryExtras WithoutSeries(){ return new LibraryExtras(new LinkedHashSet<>(), genres, platforms, completed, added); }
    public LibraryExtras WithoutGenres(){ return new LibraryExtras(seriesIds, new LinkedHashSet<>(), platforms, completed, added); }
    public LibraryExtras WithoutPlatforms(){ return new LibraryExtras(seriesIds, genres, new LinkedHashSet<>(), completed, added); }

    private static Set<String> Toggled(Set<String> set, String value, boolean on){
        Set<String> next = new LinkedHashSet<>(set);
        if(on){
            next.add(value);
        } else {
            next.remove(value);
        }
        return next;
    }

    // The day the game was added to the library (like 2025-03-02), or null when not known
    public static String AddedOn(LibraryGame game){
        Long firstSeen = game.GetSteam() == null ? null : game.GetSteam().GetFirstSeenAt();
        return LibraryDates.Effective(game.GetReview(), firstSeen).GetDate();
    }

    public boolean Matches(LibraryGame game){
        GameReview review = game.GetReview();
        if(!seriesIds.isEmpty() && (review == null || !seriesIds.contains(review.GetSeriesId()))){
            return false;
        }
        if(!genres.isEmpty() && !SharesOne(genres, review == null ? null : review.GetGenres())){
            return false;
        }
        if(!platforms.isEmpty() && !SharesOne(platforms, review == null ? null : review.GetPlatforms())){
            return false;
        }
        return completed.Contains(game.GetCompletedOn()) && added.Contains(AddedOn(game));
    }

    private static boolean SharesOne(Set<String> wanted, java.util.List<String> has){
        if(has == null){
            return false;
        }
        for(String value : has){
            if(wanted.contains(value)){
                return true;
            }
        }
        return false;
    }
}
