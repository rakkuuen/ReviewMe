package Screens.RichText;

import Controller.BackEnd.Emoji.EmojiRect;
import Controller.BackEnd.Emoji.EmojiSvg;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

// Draws an emoji one grid square at a time, each square a whole number of pixels, so every edge stays sharp. Each drawing is made into
// an image once per scale and kept
public class EmojiPainter {
    private final Map<EmojiSvg, Map<Integer, BufferedImage>> images = new WeakHashMap<>();

    // The emoji's pixel size at a scale (pixels per grid square)
    public static int SizeAt(int scale){
        return EmojiSvg.gridSize * scale;
    }

    // Draws the emoji with its top left corner at x, y
    public void Paint(Graphics2D g, EmojiSvg svg, int x, int y, int scale){
        g.drawImage(ImageOf(svg, scale), x, y, null);
    }

    public BufferedImage ImageOf(EmojiSvg svg, int scale){
        return images.computeIfAbsent(svg, k -> new HashMap<>()).computeIfAbsent(scale, k -> Draw(svg, scale));
    }

    private static BufferedImage Draw(EmojiSvg svg, int scale){
        BufferedImage image = new BufferedImage(SizeAt(scale), SizeAt(scale), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        for(EmojiRect rect : svg.GetRects()){
            g.setColor(new Color(rect.GetRgb()));
            g.fillRect(rect.GetX() * scale, rect.GetY() * scale, rect.GetWidth() * scale, rect.GetHeight() * scale);
        }
        g.dispose();
        return image;
    }
}
