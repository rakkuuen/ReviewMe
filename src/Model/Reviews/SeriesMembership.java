package Model.Reviews;

import java.util.Objects;

// Which series a stored review belongs to, where it sits in it, and which template it uses. These three always change
// together, so they are written together (and kept together as an undo snapshot)
public class SeriesMembership {
    private int reviewId;
    private String seriesId, templateId;
    private Integer position;

    // seriesId and position are null for a game in no series
    public SeriesMembership(int reviewId, String seriesId, Integer position, String templateId){
        this.reviewId = reviewId;
        this.seriesId = seriesId;
        this.position = position;
        this.templateId = templateId;
    }

    public static SeriesMembership Of(GameReview review){
        return new SeriesMembership(review.GetId(), review.GetSeriesId(), review.GetSeriesPosition(), review.GetTemplateId());
    }

    public int GetReviewId(){ return reviewId; }
    public String GetSeriesId(){ return seriesId; }
    public Integer GetPosition(){ return position; }
    public String GetTemplateId(){ return templateId; }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof SeriesMembership)){
            return false;
        }
        SeriesMembership that = (SeriesMembership) other;
        return reviewId == that.reviewId && Objects.equals(seriesId, that.seriesId)
                && Objects.equals(position, that.position) && Objects.equals(templateId, that.templateId);
    }

    @Override
    public int hashCode(){
        return Objects.hash(reviewId, seriesId, position, templateId);
    }
}
