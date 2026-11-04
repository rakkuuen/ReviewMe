package Screens.RichText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Formatted text laid out to a width: every piece with where it goes, and how tall the whole thing is
public class RichLayout {
    private final List<RichFragment> fragments;
    private final int width;
    private final int height;

    RichLayout(List<RichFragment> fragments, int width, int height){
        this.fragments = Collections.unmodifiableList(new ArrayList<>(fragments));
        this.width = width;
        this.height = height;
    }

    public List<RichFragment> GetFragments(){ return fragments; }
    public int GetWidth(){ return width; }
    public int GetHeight(){ return height; }

    // The link under a point (from the top left of the block), or null when there is none or the text there isn't a link
    public String LinkAt(int x, int y){
        RichFragment hit = LinkFragmentAt(x, y);
        return hit == null ? null : hit.GetLink();
    }

    // Which link is under a point, or RichFragment.noLink
    public int LinkIdAt(int x, int y){
        RichFragment hit = LinkFragmentAt(x, y);
        return hit == null ? RichFragment.noLink : hit.GetLinkId();
    }

    private RichFragment LinkFragmentAt(int x, int y){
        for(RichFragment fragment : fragments){
            if(fragment.Bounds().contains(x, y)){
                return fragment;
            }
        }
        return null;
    }
}
