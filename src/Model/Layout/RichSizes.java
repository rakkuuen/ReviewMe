package Model.Layout;

// The sizes of formatted text: line height (as a percentage of the font size), the gaps between paragraphs and between list items, the
// list marker column and its bullet, and how emoji sit beside text
public class RichSizes {
    private final int lineHeightPercent, blockGap, itemGap, markerColumn, bulletSize, bulletInset;
    private final int emojiBelowBaseline, emojiScale, emojiAloneScale, linkUnderline, linkUnderlineHover;

    public RichSizes(LayoutValues values){
        lineHeightPercent = values.Get(LayoutKey.RICH_LINE_HEIGHT_PERCENT);
        blockGap = values.Get(LayoutKey.RICH_BLOCK_GAP);
        itemGap = values.Get(LayoutKey.RICH_ITEM_GAP);
        markerColumn = values.Get(LayoutKey.RICH_MARKER_COLUMN);
        bulletSize = values.Get(LayoutKey.RICH_BULLET_SIZE);
        bulletInset = values.Get(LayoutKey.RICH_BULLET_INSET);
        emojiBelowBaseline = values.Get(LayoutKey.RICH_EMOJI_BELOW_BASELINE);
        emojiScale = values.Get(LayoutKey.RICH_EMOJI_SCALE);
        emojiAloneScale = values.Get(LayoutKey.RICH_EMOJI_ALONE_SCALE);
        linkUnderline = values.Get(LayoutKey.RICH_LINK_UNDERLINE);
        linkUnderlineHover = values.Get(LayoutKey.RICH_LINK_UNDERLINE_HOVER);
    }

    public int GetLineHeightPercent(){ return lineHeightPercent; }
    public int GetBlockGap(){ return blockGap; }
    public int GetItemGap(){ return itemGap; }
    public int GetMarkerColumn(){ return markerColumn; }
    public int GetBulletSize(){ return bulletSize; }
    public int GetBulletInset(){ return bulletInset; }

    // How far an emoji reaches below the text baseline at scale 1 (more at larger scales)
    public int GetEmojiBelowBaseline(){ return emojiBelowBaseline; }

    // Pixels per emoji grid square beside text, and when a paragraph holds nothing but emoji
    public int GetEmojiScale(){ return emojiScale; }
    public int GetEmojiAloneScale(){ return emojiAloneScale; }

    public int GetLinkUnderline(){ return linkUnderline; }
    public int GetLinkUnderlineHover(){ return linkUnderlineHover; }
}
