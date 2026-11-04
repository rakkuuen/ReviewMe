package Controller.BackEnd.Emoji;

// One solid rectangle of an emoji, in grid squares (0 to 16), with its colour as 0xRRGGBB
public class EmojiRect {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int rgb;

    public EmojiRect(int x, int y, int width, int height, int rgb){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rgb = rgb;
    }

    public int GetX(){ return x; }
    public int GetY(){ return y; }
    public int GetWidth(){ return width; }
    public int GetHeight(){ return height; }
    public int GetRgb(){ return rgb; }
}
