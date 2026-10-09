package Controller.BackEnd.Importer.Writing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import Controller.BackEnd.Importer.Results.ImportResult;
import Controller.BackEnd.Importer.Results.MappedReview;
import Database.Dao.GameReviewDao;
import Database.Dao.ReviewFieldDao;
import Database.Dao.ReviewSubsectionDao;
import Database.Dao.ReviewTagDao;
import Database.Dao.SeriesDao;
import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.Subsection;
import Model.Reviews.TagKind;

// Reads everything back out of the database and compares it with what was imported. Returns what differs (empty is good)
public class RebuildVerifier {

    public static List<String> Verify(ImportResult result){
        List<String> differences = new ArrayList<>();

        Map<String, GameReview> stored = new HashMap<>();
        for(GameReview review : GameReviewDao.GetAllGameReviews()){
            stored.put(review.GetTitle(), review);
        }
        if(stored.size() != result.GetReviews().size()){
            differences.add("the database holds " + stored.size() + " reviews, the import had " + result.GetReviews().size());
        }
        int storedSeries = SeriesDao.GetAllSeries().size();
        if(storedSeries != SeriesWriter.CountSeries(result)){
            differences.add("the database holds " + storedSeries + " series, the import had " + SeriesWriter.CountSeries(result));
        }

        for(MappedReview mapped : result.GetReviews()){
            GameReview expected = mapped.GetReview();
            GameReview actual = stored.get(expected.GetTitle());
            if(actual == null){
                differences.add(expected.GetTitle() + ": not in the database");
            } else {
                CompareReview(expected, actual, differences);
            }
        }
        return differences;
    }

    private static void CompareReview(GameReview expected, GameReview actual, List<String> differences){
        String title = expected.GetTitle();
        Same(title, "id", expected.GetId(), actual.GetId(), differences);
        Same(title, "template", expected.GetTemplateId(), actual.GetTemplateId(), differences);
        Same(title, "series", expected.GetSeriesId(), actual.GetSeriesId(), differences);
        Same(title, "source path", expected.GetSourcePath(), actual.GetSourcePath(), differences);
        Same(title, "in progress", expected.IsInProgress(), actual.IsInProgress(), differences);
        Same(title, "completed", expected.IsCompleted(), actual.IsCompleted(), differences);

        int id = actual.GetId();
        Same(title, "genres", expected.GetGenres(), ReviewTagDao.GetTags(id, TagKind.GENRE), differences);
        Same(title, "metadata", expected.GetMetadata(), ReviewTagDao.GetTags(id, TagKind.METADATA), differences);
        Same(title, "platforms", expected.GetPlatforms(), ReviewTagDao.GetTags(id, TagKind.PLATFORM), differences);

        CompareFields(expected, id, differences);
    }

    private static void CompareFields(GameReview expected, int id, List<String> differences){
        String title = expected.GetTitle();
        Map<String, FieldValue> storedFields = ReviewFieldDao.GetFieldValues(id);
        Map<String, List<Subsection>> storedSubsections = ReviewSubsectionDao.GetAllSubsections(id);

        if(!expected.GetFieldValues().keySet().equals(storedFields.keySet())){
            differences.add(title + ": fields differ, imported " + expected.GetFieldValues().keySet() + " but stored " + storedFields.keySet());
        }
        for(Map.Entry<String, FieldValue> field : expected.GetFieldValues().entrySet()){
            FieldValue actual = storedFields.get(field.getKey());
            if(actual == null){
                continue;
            }
            FieldValue want = field.getValue();
            if(!Objects.equals(want.GetText(), actual.GetText()) || !Objects.equals(want.GetComment(), actual.GetComment())
                    || want.GetStatus() != actual.GetStatus()){
                differences.add(title + ": field " + field.getKey() + " differs");
            }
            List<Subsection> storedGroups = storedSubsections.getOrDefault(field.getKey(), new ArrayList<>());
            if(!SameSubsections(want.GetSubsections(), storedGroups)){
                differences.add(title + ": groups/characters in " + field.getKey() + " differ");
            }
        }
    }

    private static boolean SameSubsections(List<Subsection> a, List<Subsection> b){
        if(a.size() != b.size()){
            return false;
        }
        for(int i = 0; i < a.size(); i++){
            if(!Objects.equals(a.get(i).GetName(), b.get(i).GetName()) || !Objects.equals(a.get(i).GetText(), b.get(i).GetText())
                    || !SameSubsections(a.get(i).GetChildren(), b.get(i).GetChildren())){
                return false;
            }
        }
        return true;
    }

    private static void Same(String title, String what, Object expected, Object actual, List<String> differences){
        if(!Objects.equals(expected, actual)){
            differences.add(title + ": " + what + " differs (imported " + expected + ", stored " + actual + ")");
        }
    }
}
