package Controller.BackEnd.Reviews;

import java.time.Instant;
import java.time.ZoneOffset;

import Model.Reviews.AddedToLibrary;
import Model.Reviews.AddedToLibrary.Source;
import Model.Reviews.GameReview;

// Works out the "added to library" date shown for a game
public class LibraryDates {

    // A date typed in by hand wins. Otherwise it's the day ReviewMe first saw the game in a sync (firstSeenAt is epoch seconds,
    // null when it has never been synced)
    public static AddedToLibrary Effective(GameReview review, Long firstSeenAt){
        String manual = review == null ? null : review.GetAddedToLibraryOn();
        if(manual != null && !manual.trim().isEmpty()){
            return new AddedToLibrary(manual.trim(), Source.MANUAL);
        }
        if(firstSeenAt != null && firstSeenAt > 0){
            return new AddedToLibrary(Instant.ofEpochSecond(firstSeenAt).atZone(ZoneOffset.UTC).toLocalDate().toString(), Source.FIRST_SEEN);
        }
        return new AddedToLibrary(null, Source.UNKNOWN);
    }
}
