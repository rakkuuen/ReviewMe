package Model.Layout;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Builds the Layout from layout.properties, with a theme file's layout.* lines laid over it. Strict on purpose: a missing, unknown,
// repeated or badly written key is an error that names the key and the file, never a quiet default. Every problem is reported at once
public class LayoutLoader {
    private static final String overridePrefix = "layout.";
    private static final char byteOrderMark = (char) 0xFEFF;   // Some editors put this at the very start of a UTF-8 file

    // themePath may be null for no overrides. Lines in a theme file that don't start with layout. are none of this loader's business
    public static Layout Load(String layoutPath, String themePath) throws LayoutException {
        List<String> problems = new ArrayList<>();
        Map<String, String> base = ReadKeyValues(layoutPath, "", problems);
        Map<String, String> overrides = themePath == null ? new LinkedHashMap<String, String>() : ReadKeyValues(themePath, overridePrefix, problems);

        Map<LayoutKey, String> text = new EnumMap<>(LayoutKey.class);
        Map<LayoutKey, String> origin = new EnumMap<>(LayoutKey.class);
        Collect(base, "", layoutPath, text, origin, problems);
        Collect(overrides, overridePrefix, themePath, text, origin, problems);

        for(LayoutKey key : LayoutKey.values()){
            if(!text.containsKey(key)){
                problems.add("Missing layout key '" + key.GetPropertyName() + "' in " + layoutPath);
            }
        }

        Map<LayoutKey, Integer> numbers = new EnumMap<>(LayoutKey.class);
        for(Map.Entry<LayoutKey, String> entry : text.entrySet()){
            Integer number = Parse(entry.getKey(), entry.getValue(), origin.get(entry.getKey()), problems);
            if(number != null){
                numbers.put(entry.getKey(), number);
            }
        }
        if(numbers.size() == LayoutKey.values().length){
            CheckTogether(numbers, problems);
        }

        if(!problems.isEmpty()){
            throw new LayoutException(problems);
        }
        return new Layout(new LayoutValues(numbers));
    }

    public static Layout Load(String layoutPath) throws LayoutException {
        return Load(layoutPath, null);
    }

    // The lines of a file as name to value, with the prefix taken off the name. Lines that don't start with the prefix are skipped
    private static Map<String, String> ReadKeyValues(String path, String prefix, List<String> problems){
        Map<String, String> found = new LinkedHashMap<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            problems.add("Can't read " + path + ": " + e.getMessage());
            return found;
        }

        for(int i = 0; i < lines.size(); i++){
            String line = lines.get(i);
            if(i == 0 && !line.isEmpty() && line.charAt(0) == byteOrderMark){
                line = line.substring(1);
            }
            line = line.trim();
            if(line.isEmpty() || line.startsWith("#")){
                continue;
            }
            int equals = line.indexOf('=');
            if(equals < 0){
                if(prefix.isEmpty()){
                    problems.add(path + " line " + (i + 1) + " is not 'key = value': " + line);
                }
                continue;
            }
            String name = line.substring(0, equals).trim();
            if(!name.startsWith(prefix)){
                continue;
            }
            String key = name.substring(prefix.length());
            if(found.containsKey(key)){
                problems.add("Layout key '" + name + "' is written twice in " + path + " (line " + (i + 1) + ")");
            } else {
                found.put(key, line.substring(equals + 1).trim());
            }
        }
        return found;
    }

    private static void Collect(Map<String, String> from, String prefix, String path, Map<LayoutKey, String> text, Map<LayoutKey, String> origin, List<String> problems){
        for(Map.Entry<String, String> entry : from.entrySet()){
            LayoutKey key = LayoutKey.FromPropertyName(entry.getKey());
            if(key == null){
                problems.add("Unknown layout key '" + prefix + entry.getKey() + "' in " + path);
            } else {
                text.put(key, entry.getValue());
                origin.put(key, prefix.isEmpty() ? path : path + " (as " + prefix + key.GetPropertyName() + ")");
            }
        }
    }

    // A whole number inside the key's range, or null with the problem recorded
    private static Integer Parse(LayoutKey key, String value, String where, List<String> problems){
        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            problems.add("Layout key '" + key.GetPropertyName() + "' must be a whole number, found '" + value + "' in " + where);
            return null;
        }
        if(number < key.GetMin() || number > key.GetMax()){
            String range = key.GetMax() == Integer.MAX_VALUE ? "at least " + key.GetMin() : "from " + key.GetMin() + " to " + key.GetMax();
            problems.add("Layout key '" + key.GetPropertyName() + "' must be " + range + ", found " + number + " in " + where);
            return null;
        }
        return number;
    }

    // Rules between keys, once each one is a good number on its own
    private static void CheckTogether(Map<LayoutKey, Integer> numbers, List<String> problems){
        CheckNotSmaller(numbers, LayoutKey.WINDOW_START_WIDTH, LayoutKey.WINDOW_MIN_WIDTH, problems);
        CheckNotSmaller(numbers, LayoutKey.WINDOW_START_HEIGHT, LayoutKey.WINDOW_MIN_HEIGHT, problems);
        if(numbers.get(LayoutKey.RICH_BULLET_INSET) + numbers.get(LayoutKey.RICH_BULLET_SIZE) > numbers.get(LayoutKey.RICH_MARKER_COLUMN)){
            problems.add("Layout keys 'rich.bulletInset' and 'rich.bulletSize' together can't be wider than 'rich.markerColumn'");
        }
    }

    private static void CheckNotSmaller(Map<LayoutKey, Integer> numbers, LayoutKey bigger, LayoutKey smaller, List<String> problems){
        if(numbers.get(bigger) < numbers.get(smaller)){
            problems.add("Layout key '" + bigger.GetPropertyName() + "' (" + numbers.get(bigger) + ") can't be smaller than '" + smaller.GetPropertyName() + "' (" + numbers.get(smaller) + ")");
        }
    }
}
