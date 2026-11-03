package Controller.BackEnd.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// Backing up the database file and putting a backup back. Backups are dated copies in one folder. Restoring first makes a backup of what
// is there, so a restore can itself be undone. No database connection is held open between calls, so the file can be copied freely
public class BackupManager {
    private static final String prefix = "GameReview-";
    private static final String suffix = ".db";
    private static final String sqliteHeader = "SQLite format 3";
    private static final DateTimeFormatter stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter shown = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", java.util.Locale.ENGLISH);

    // Copies the database into the folder under a dated name and returns the copy
    public static Path Backup(Path database, Path folder, LocalDateTime now) throws IOException {
        if(!Files.isRegularFile(database)){
            throw new IOException("There is no database to back up");
        }
        Files.createDirectories(folder);
        Path copy = folder.resolve(prefix + now.format(stamp) + suffix);
        Files.copy(database, copy, StandardCopyOption.REPLACE_EXISTING);
        return copy;
    }

    // Puts the backup in place of the database, after backing up what is there now. Returns that safety copy (null when there was no database)
    public static Path Restore(Path database, Path backup, Path folder, LocalDateTime now) throws IOException {
        String problem = CheckBackup(backup);
        if(problem != null){
            throw new IOException(problem);
        }
        Path safety = Files.isRegularFile(database) ? Backup(database, folder, now) : null;
        Files.copy(backup, database, StandardCopyOption.REPLACE_EXISTING);
        return safety;
    }

    // Why a file can't be restored, or null when it looks like a database
    public static String CheckBackup(Path file){
        if(file == null || !Files.isRegularFile(file)){
            return "That file doesn't exist";
        }
        try (InputStream in = Files.newInputStream(file)) {
            byte[] head = in.readNBytes(sqliteHeader.length());
            if(!new String(head, StandardCharsets.ISO_8859_1).equals(sqliteHeader)){
                return "That doesn't look like a ReviewMe database";
            }
        } catch (IOException e) {
            return "That file can't be read";
        }
        return null;
    }

    // The backups in the folder, newest first
    public static List<Path> List(Path folder){
        List<Path> found = new ArrayList<>();
        if(!Files.isDirectory(folder)){
            return found;
        }
        try (Stream<Path> files = Files.list(folder)) {
            files.filter(f -> f.getFileName().toString().startsWith(prefix) && f.getFileName().toString().endsWith(suffix)).forEach(found::add);
        } catch (IOException e) {
            return new ArrayList<>();
        }
        found.sort((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()));
        return found;
    }

    // "Last backup 3 Oct 2026, 14:20" from the newest backup's name, or "No backups yet"
    public static String LastBackupText(Path folder){
        List<Path> backups = List(folder);
        if(backups.isEmpty()){
            return "No backups yet";
        }
        String name = backups.get(0).getFileName().toString();
        try {
            LocalDateTime when = LocalDateTime.parse(name.substring(prefix.length(), name.length() - suffix.length()), stamp);
            return "Last backup " + when.format(shown);
        } catch (RuntimeException e) {
            return "Last backup " + name;
        }
    }
}
