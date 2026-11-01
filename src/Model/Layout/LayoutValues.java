package Model.Layout;

import java.util.EnumMap;
import java.util.Map;

// The checked number for every layout key. Only the loader makes one, and only when every key is there and in range
public class LayoutValues {
    private final Map<LayoutKey, Integer> values;

    LayoutValues(Map<LayoutKey, Integer> values){
        this.values = new EnumMap<>(values);
    }

    public int Get(LayoutKey key){
        return values.get(key);
    }
}
