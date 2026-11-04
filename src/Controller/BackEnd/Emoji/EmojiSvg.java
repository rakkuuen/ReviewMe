package Controller.BackEnd.Emoji;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A checked emoji drawing: rectangles painted in order on a square grid
public class EmojiSvg {
    public static final int gridSize = 16;

    private final List<EmojiRect> rects;

    public EmojiSvg(List<EmojiRect> rects){
        this.rects = Collections.unmodifiableList(new ArrayList<>(rects));
    }

    public List<EmojiRect> GetRects(){ return rects; }
}
