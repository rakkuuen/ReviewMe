package Controller.BackEnd.Emoji;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// The result of checking an SVG: the drawing when it passed, otherwise a plain list of what is wrong
public class EmojiCheck {
    private final EmojiSvg svg;
    private final List<String> problems;
    private final int byteCount;

    EmojiCheck(EmojiSvg svg, List<String> problems, int byteCount){
        this.svg = svg;
        this.problems = Collections.unmodifiableList(new ArrayList<>(problems));
        this.byteCount = byteCount;
    }

    public boolean IsOk(){ return svg != null; }

    // The drawing, or null when the file didn't pass
    public EmojiSvg GetSvg(){ return svg; }

    public List<String> GetProblems(){ return problems; }
    public int GetByteCount(){ return byteCount; }
    public int GetRectCount(){ return svg == null ? 0 : svg.GetRects().size(); }
}
