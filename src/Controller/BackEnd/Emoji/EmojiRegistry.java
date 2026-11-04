package Controller.BackEnd.Emoji;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Every emoji the app can draw. The built-in ones come in a set per theme (built-in/base, built-in/fallout, the same names drawn
// in that theme's colours); the user's own sit in mine/ and are used on every theme. The file name is the emoji's name. A file that
// doesn't pass the checker is left out and its problems kept, so one bad file never stops the rest loading
public class EmojiRegistry {
    public static final String builtInFolder = "built-in";
    public static final String mineFolder = "mine";
    public static final String baseSet = "base";

    private final Map<String, Map<String, EmojiSvg>> builtIn = new TreeMap<>();
    private final Map<String, EmojiSvg> mine = new TreeMap<>();
    private final Map<String, List<String>> problems = new TreeMap<>();
    private String activeSet = baseSet;

    private EmojiRegistry(){
    }

    // Reads the emoji folder (missing folders just mean no emoji). Built-in names are reserved: a file in mine/ that reuses one is left out
    public static EmojiRegistry Load(Path root){
        EmojiRegistry registry = new EmojiRegistry();
        Path builtInRoot = root.resolve(builtInFolder);
        for(Path setFolder : Entries(builtInRoot, true)){
            registry.builtIn.put(setFolder.getFileName().toString(), registry.ReadFolder(setFolder));
        }
        for(Map.Entry<String, EmojiSvg> entry : registry.ReadFolder(root.resolve(mineFolder)).entrySet()){
            registry.AddMine(entry.getKey(), entry.getValue(), root.resolve(mineFolder).resolve(entry.getKey() + EmojiNames.fileSuffix));
        }
        registry.activeSet = registry.builtIn.containsKey(baseSet) || registry.builtIn.isEmpty() ? baseSet : registry.builtIn.keySet().iterator().next();
        return registry;
    }

    private void AddMine(String name, EmojiSvg svg, Path file){
        if(IsBuiltIn(name)){
            problems.put(file.toString(), List.of("Name already used by a built-in emoji: " + name));
        } else {
            mine.put(name, svg);
        }
    }

    // Name and drawing of every good file in the folder
    private Map<String, EmojiSvg> ReadFolder(Path folder){
        Map<String, EmojiSvg> found = new TreeMap<>();
        for(Path file : Entries(folder, false)){
            String name = EmojiNames.FromFileName(file.getFileName().toString());
            if(name == null){
                problems.put(file.toString(), List.of("Not a valid emoji file name (2 to 24 of a-z, 0-9, _ then .svg)"));
                continue;
            }
            EmojiCheck check = Checked(file);
            if(check.IsOk()){
                found.put(name, check.GetSvg());
            } else {
                problems.put(file.toString(), check.GetProblems());
            }
        }
        return found;
    }

    private static EmojiCheck Checked(Path file){
        try {
            return EmojiSvgChecker.Check(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return new EmojiCheck(null, List.of("Can't read the file: " + e.getMessage()), 0);
        }
    }

    // The folders (or .svg files) directly inside a folder, in name order; empty when the folder isn't there or can't be read
    private static List<Path> Entries(Path folder, boolean folders){
        List<Path> found = new ArrayList<>();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path entry : stream){
                if(Files.isDirectory(entry) == folders && (folders || entry.getFileName().toString().endsWith(EmojiNames.fileSuffix))){
                    found.add(entry);
                }
            }
        } catch (IOException e) {
            return found;
        }
        Collections.sort(found);
        return found;
    }

    // Chooses which built-in set is drawn; false (and no change) when there is no such set
    public boolean UseSet(String set){
        if(!builtIn.containsKey(set)){
            return false;
        }
        activeSet = set;
        return true;
    }

    public String GetActiveSet(){ return activeSet; }
    public List<String> GetSetNames(){ return new ArrayList<>(builtIn.keySet()); }

    // True when the name can be drawn now (one of mine, or built-in in the chosen set); this is what the text reader asks
    public boolean Has(String name){
        return Get(name) != null;
    }

    // The drawing for a name, or null
    public EmojiSvg Get(String name){
        if(name == null){
            return null;
        }
        EmojiSvg own = mine.get(name);
        if(own != null){
            return own;
        }
        Map<String, EmojiSvg> set = builtIn.get(activeSet);
        return set == null ? null : set.get(name);
    }

    public boolean IsBuiltIn(String name){
        for(Map<String, EmojiSvg> set : builtIn.values()){
            if(set.containsKey(name)){
                return true;
            }
        }
        return false;
    }

    public boolean IsMine(String name){
        return mine.containsKey(name);
    }

    public List<String> GetMineNames(){ return new ArrayList<>(mine.keySet()); }

    // Built-in names of the chosen set
    public List<String> GetBuiltInNames(){
        Map<String, EmojiSvg> set = builtIn.get(activeSet);
        return set == null ? new ArrayList<>() : new ArrayList<>(set.keySet());
    }

    // Mine first, then the built-in ones, each in name order
    public List<String> GetNames(){
        List<String> names = GetMineNames();
        names.addAll(GetBuiltInNames());
        return names;
    }

    // What was wrong with each file that was left out: path, then the plain list
    public Map<String, List<String>> GetProblems(){ return Collections.unmodifiableMap(problems); }
}
