package Screens.RichText;

import Controller.BackEnd.Emoji.EmojiRegistry;
import Controller.BackEnd.RichText.Block;
import Controller.BackEnd.RichText.Inline;
import Controller.BackEnd.RichText.RichDocument;
import Model.Layout.LayoutMath;
import Model.Layout.RichSizes;
import Model.Themes.Theme;
import Screens.RichText.RichAtoms.Piece;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Lays a document out to a width: each block's words wrapped into lines, a line as tall as its tallest piece (an emoji, or text in a
// bigger font), the gaps between paragraphs and between list items, and a marker beside each list item's first line
final class RichLayouter {
    private final Graphics2D g;
    private final Font body;
    private final Color text;
    private final RichSizes sizes;
    private final EmojiRegistry registry;
    private final List<RichFragment> out = new ArrayList<>();
    private final int[] linkCounter = new int[1];
    private int y;

    private RichLayouter(Graphics2D g, Font body, Color text, RichSizes sizes, EmojiRegistry registry){
        this.g = g;
        this.body = body;
        this.text = text;
        this.sizes = sizes;
        this.registry = registry;
    }

    static RichLayout Lay(RichDocument document, Graphics2D g, int width, Font body, Color text, RichSizes sizes, EmojiRegistry registry){
        RichLayouter layouter = new RichLayouter(g, body, text, sizes, registry);
        List<Block> blocks = document.GetBlocks();
        for(int i = 0; i < blocks.size(); i++){
            if(i > 0){
                layouter.y += layouter.GapBetween(blocks.get(i - 1), blocks.get(i));
            }
            layouter.LayBlock(blocks.get(i), width);
        }
        return new RichLayout(layouter.out, width, layouter.y);
    }

    // List items one straight after another are close; anything else (a blank line between items too) gets a paragraph's gap
    private int GapBetween(Block before, Block after){
        boolean together = before.IsListItem() && after.IsListItem() && !after.FollowsBlankLine();
        return together ? sizes.GetItemGap() : sizes.GetBlockGap();
    }

    private void LayBlock(Block block, int width){
        int indent = block.IsListItem() ? sizes.GetMarkerColumn() : 0;
        int scale = OnlyEmoji(block) ? sizes.GetEmojiAloneScale() : sizes.GetEmojiScale();
        List<RichAtoms.Atom> atoms = new RichAtoms(g, body, text, registry, scale, linkCounter).Build(block.GetInlines());
        List<List<Piece>> lines = RichWrapper.Wrap(atoms, Math.max(1, width - indent), g);
        int firstTop = y;
        int firstBaseline = y;
        int firstHeight = 0;
        for(int i = 0; i < lines.size(); i++){
            int top = y;
            int[] box = Reach(lines.get(i));
            y = PlaceLine(lines.get(i), indent, top, box);
            if(i == 0){
                firstBaseline = top + box[0];
                firstHeight = box[0] + box[1];
            }
        }
        if(block.IsListItem() && !lines.isEmpty()){
            AddMarker(block, firstTop, firstBaseline, firstHeight);
        }
    }

    // A paragraph of nothing but emoji (and spaces) draws them larger
    private boolean OnlyEmoji(Block block){
        if(block.GetKind() != Block.Kind.PARAGRAPH){
            return false;
        }
        for(Inline inline : block.GetInlines()){
            if(inline.GetKind() == Inline.Kind.EMOJI && !registry.Has(inline.GetText())){
                return false;
            }
            if(inline.GetKind() == Inline.Kind.TEXT && !inline.GetText().trim().isEmpty()){
                return false;
            }
        }
        return true;
    }

    // {how far the line reaches above its baseline, how far below} for the tallest thing on it. An empty line is as tall as body text
    private int[] Reach(List<Piece> line){
        if(line.isEmpty()){
            return TextBox(body);
        }
        int above = 0;
        int below = 0;
        for(Piece piece : line){
            int[] box = piece.IsEmoji() ? EmojiBox(piece.GetScale()) : TextBox(piece.GetFont());
            above = Math.max(above, box[0]);
            below = Math.max(below, box[1]);
        }
        return new int[] { above, below };
    }

    // Text sits in a line of the theme's height, centred in it, but never so tight its letters are cut
    private int[] TextBox(Font font){
        FontMetrics metrics = g.getFontMetrics(font);
        int lineHeight = Math.max(metrics.getHeight(), LayoutMath.Percent(font.getSize2D(), sizes.GetLineHeightPercent()));
        int above = metrics.getAscent() + LayoutMath.Half(lineHeight - metrics.getHeight());
        return new int[] { above, lineHeight - above };
    }

    private int[] EmojiBox(int scale){
        int size = EmojiPainter.SizeAt(scale);
        int below = sizes.GetEmojiBelowBaseline() * scale;
        return new int[] { size - below, below };
    }

    // Puts a line's pieces side by side from the indent, joining neighbours that look the same. Returns where the next line starts
    private int PlaceLine(List<Piece> line, int indent, int top, int[] box){
        int baseline = top + box[0];
        int height = box[0] + box[1];
        int x = indent;
        StringBuilder run = new StringBuilder();
        Piece runPiece = null;
        for(Piece piece : line){
            if(runPiece != null && !Joins(runPiece, piece)){
                x += AddText(run, runPiece, x, baseline, top, height);
                run.setLength(0);
                runPiece = null;
            }
            if(piece.IsEmoji()){
                out.add(new RichFragment(RichFragment.Kind.EMOJI, piece.GetText(), x, baseline, piece.GetWidth(), top, height, piece.GetFont(), piece.GetColour(),
                        piece.GetScale(), piece.GetLink(), piece.GetLinkId()));
                x += piece.GetWidth();
            } else {
                runPiece = runPiece == null ? piece : runPiece;
                run.append(piece.GetText());
            }
        }
        if(runPiece != null){
            AddText(run, runPiece, x, baseline, top, height);
        }
        return top + height;
    }

    private static boolean Joins(Piece a, Piece b){
        return !b.IsEmoji() && a.GetFont().equals(b.GetFont()) && a.GetColour().equals(b.GetColour()) && Objects.equals(a.GetLink(), b.GetLink());
    }

    // A run of text is drawn in one go, so its width is measured in one go (the words measured one by one, each rounded to a pixel, add up
    // to a little more or less over a long run). Returns that width
    private int AddText(StringBuilder run, Piece first, int x, int baseline, int top, int height){
        int width = g.getFontMetrics(first.GetFont()).stringWidth(run.toString());
        out.add(new RichFragment(RichFragment.Kind.TEXT, run.toString(), x, baseline, width, top, height, first.GetFont(), first.GetColour(), 0, first.GetLink(),
                first.GetLinkId()));
        return width;
    }

    // A bullet or "3." in the marker column, level with the item's first line
    private void AddMarker(Block block, int top, int baseline, int height){
        Color colour = Theme.Current.GetRich().GetMarker();
        if(block.GetKind() == Block.Kind.BULLET){
            out.add(new RichFragment(RichFragment.Kind.BULLET, "", sizes.GetBulletInset(), baseline, sizes.GetBulletSize(), top, height, body, colour, 0, null,
                    RichFragment.noLink));
        } else {
            Font font = Theme.Current.GetRich().GetMarkerFont();
            String number = block.GetNumber() + ".";
            out.add(new RichFragment(RichFragment.Kind.NUMBER, number, 0, baseline, g.getFontMetrics(font).stringWidth(number), top, height, font, colour, 0, null,
                    RichFragment.noLink));
        }
    }
}
