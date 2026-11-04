package Screens.RichText;

import Model.Layout.RichSizes;
import Screens.MainView.HitMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.function.Consumer;

// What a screen uses to show formatted text in its page: lays text out, draws it, and makes each link in it clickable (through the
// screen's hit map, so it scrolls and clicks like any button). Several blocks on one page each get a prefix, so their links have
// keys of their own that stay the same from one paint to the next
public class RichBox {
    private static final String linkMark = ":link:";
    private final RichTextKit kit;
    private final RichSizes sizes;
    private final HitMap hits;
    private final Consumer<String> onOpenLink;

    public RichBox(RichTextKit kit, RichSizes sizes, HitMap hits, Consumer<String> onOpenLink){
        this.kit = kit;
        this.sizes = sizes;
        this.hits = hits;
        this.onOpenLink = onOpenLink;
    }

    public RichLayout Lay(Graphics2D g, String stored, int width, Font body, Color text){
        return kit.Lay(g, stored, width, body, text, sizes);
    }

    // Draws the layout with its top left at x, y (the page's own space, where the hit map's frame is). mouse is in that space too
    public void Paint(Graphics2D g, RichLayout layout, int x, int y, Point mouse, String prefix){
        kit.Paint(g, layout, x, y, mouse, sizes);
        for(RichFragment fragment : layout.GetFragments()){
            if(fragment.IsLink()){
                Rectangle bounds = fragment.Bounds();
                bounds.translate(x, y);
                String address = fragment.GetLink();
                hits.Add(prefix + linkMark + fragment.GetLinkId(), bounds, () -> onOpenLink.accept(address));
            }
        }
    }

    // Is that hit key one of a link's (so the pointer should be a hand)?
    public static boolean IsLinkKey(String key){
        return key != null && key.contains(linkMark);
    }
}
