package Controller.BackEnd.RichText;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

// Remembers the document each recently shown text was read into, so a screen painting every frame doesn't read the same text again.
// A document depends on which emoji exist, so the cache must be cleared when they change
public class DocumentCache {
    private static final int maxEntries = 512;

    private final Predicate<String> knownEmoji;
    private final Map<String, RichDocument> documents = new LinkedHashMap<String, RichDocument>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, RichDocument> eldest){
            return size() > maxEntries;
        }
    };

    public DocumentCache(Predicate<String> knownEmoji){
        this.knownEmoji = knownEmoji;
    }

    // The document for a text (null counts as no text)
    public RichDocument Get(String text){
        String key = text == null ? "" : text;
        RichDocument document = documents.get(key);
        if(document == null){
            document = RichTextParser.Parse(key, knownEmoji);
            documents.put(key, document);
        }
        return document;
    }

    public void Clear(){
        documents.clear();
    }

    public int Size(){
        return documents.size();
    }
}
