package Controller.BackEnd.Importer.Writing;

import java.util.List;
import java.util.Map;

import Controller.BackEnd.Importer.Results.ImportResult;
import Controller.BackEnd.Importer.Results.MappedReview;
import Database.Dao.GameReviewDao;
import Database.Dao.ReviewFieldDao;
import Database.Dao.ReviewSubsectionDao;
import Database.Dao.ReviewTagDao;
import Model.Reviews.FieldValue;
import Model.Reviews.GameReview;
import Model.Reviews.Subsection;
import Model.Reviews.TagKind;

// Stores an import into the database: each review with its fields, groups and characters and tags, then the series.
// Meant for a fresh database. A failure is recorded and the rest carries on
public class ImportWriter {

    public static WriteResult WriteAll(ImportResult result){
        WriteResult written = new WriteResult();
        for(MappedReview mapped : result.GetReviews()){
            WriteReview(mapped.GetReview(), written);
        }
        SeriesWriter.Write(result, written);
        return written;
    }

    private static void WriteReview(GameReview review, WriteResult written){
        Integer id = GameReviewDao.InsertImportedReview(review);
        if(id == null){
            written.Fail(review.GetTitle(), "couldn't insert the review");
            return;
        }
        review.SetId(id);
        written.CountReview();

        WriteFields(id, review, written);
        WriteTags(id, review, written);
    }

    private static void WriteFields(int id, GameReview review, WriteResult written){
        Map<String, FieldValue> fields = review.GetFieldValues();
        if(!ReviewFieldDao.SaveFieldValues(id, fields)){
            written.Fail(review.GetTitle(), "couldn't save its fields");
            return;
        }
        written.CountFieldRows(fields.size());

        for(Map.Entry<String, FieldValue> field : fields.entrySet()){
            List<Subsection> subsections = field.getValue().GetSubsections();
            if(subsections.isEmpty()){
                continue;
            }
            if(ReviewSubsectionDao.ReplaceSubsections(id, field.getKey(), subsections)){
                written.CountSubsectionField();
            } else {
                written.Fail(review.GetTitle(), "couldn't save the groups and characters in " + field.getKey());
            }
        }
    }

    private static void WriteTags(int id, GameReview review, WriteResult written){
        WriteTagKind(id, review, TagKind.GENRE, review.GetGenres(), written);
        WriteTagKind(id, review, TagKind.METADATA, review.GetMetadata(), written);
        WriteTagKind(id, review, TagKind.PLATFORM, review.GetPlatforms(), written);
    }

    private static void WriteTagKind(int id, GameReview review, TagKind kind, List<String> tags, WriteResult written){
        if(ReviewTagDao.ReplaceTags(id, kind, tags)){
            written.CountTagRows(new java.util.HashSet<>(tags).size());
        } else {
            written.Fail(review.GetTitle(), "couldn't save its " + kind.name().toLowerCase() + " tags");
        }
    }
}
