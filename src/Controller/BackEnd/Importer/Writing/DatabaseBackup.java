package Controller.BackEnd.Importer.Writing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import Database.Dao.Db;

// Puts the current database file aside as a dated backup so a fresh one can be built in its place
public class DatabaseBackup {

    // Returns where the old file went, or null if there was no database to move
    public static Path MoveAside() throws IOException {
        Path current = Paths.get(Db.filePath);
        if(!Files.exists(current)){
            return null;
        }

        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path backup = current.resolveSibling("GameReview.backup-" + stamp + ".db");
        Files.move(current, backup);
        return backup;
    }
}
