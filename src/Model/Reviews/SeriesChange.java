package Model.Reviews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Everything one series operation writes, so it can be saved in one go. Applied in this order: add a series, update a
// series, move games, remove a series. Every operation also builds the SeriesChange that puts things back (its undo)
public class SeriesChange {
    private Series toInsert, toUpdate;
    private String idToDelete;
    private List<SeriesMembership> memberships = new ArrayList<>();

    public Series GetToInsert(){ return toInsert; }
    public void SetToInsert(Series toInsert){ this.toInsert = toInsert; }

    // Matched by id: name, template and notes are replaced
    public Series GetToUpdate(){ return toUpdate; }
    public void SetToUpdate(Series toUpdate){ this.toUpdate = toUpdate; }

    public String GetIdToDelete(){ return idToDelete; }
    public void SetIdToDelete(String idToDelete){ this.idToDelete = idToDelete; }

    public List<SeriesMembership> GetMemberships(){ return Collections.unmodifiableList(memberships); }
    public void AddMemberships(List<SeriesMembership> more){ memberships.addAll(more); }

    public boolean IsEmpty(){
        return toInsert == null && toUpdate == null && idToDelete == null && memberships.isEmpty();
    }
}
