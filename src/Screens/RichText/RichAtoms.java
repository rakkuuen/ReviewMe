package Screens.RichText;

import Controller.BackEnd.Emoji.EmojiRegistry;
import Controller.BackEnd.RichText.Inline;
import Controller.BackEnd.RichText.RunStyle;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

// Cuts a block's pieces into the units a line is built from: words (which can't be split between lines, though one word may be made of
// pieces in several styles, like wor**ld**), single spaces, and forced line breaks. Each piece is measured here
final class RichAtoms {
    enum AtomKind { WORD, SPACE, BREAK }

    // One measured piece of a word or space: some characters in one font, or an emoji
    static final class Piece {
        private final boolean emoji;
        private final String text;
        private final Font font;
        private final Color colour;
        private final String link;
        private final int linkId;
        private final int scale;
        private final int width;

        Piece(boolean emoji, String text, Font font, Color colour, String link, int linkId, int scale, int width){
            this.emoji = emoji;
            this.text = text;
            this.font = font;
            this.colour = colour;
            this.link = link;
            this.linkId = linkId;
            this.scale = scale;
            this.width = width;
        }

        boolean IsEmoji(){ return emoji; }
        String GetText(){ return text; }
        Font GetFont(){ return font; }
        Color GetColour(){ return colour; }
        String GetLink(){ return link; }
        int GetLinkId(){ return linkId; }
        int GetScale(){ return scale; }
        int GetWidth(){ return width; }
    }

    static final class Atom {
        private final AtomKind kind;
        private final List<Piece> pieces = new ArrayList<>();
        private int width;

        Atom(AtomKind kind){
            this.kind = kind;
        }

        void Add(Piece piece){
            pieces.add(piece);
            width += piece.GetWidth();
        }

        AtomKind GetKind(){ return kind; }
        List<Piece> GetPieces(){ return pieces; }
        int GetWidth(){ return width; }
    }

    private final Graphics2D g;
    private final Font body;
    private final Color text;
    private final EmojiRegistry registry;
    private final int emojiScale;
    private final List<Atom> atoms = new ArrayList<>();
    private Atom word;
    private String lastLink;
    private int linkId = RichFragment.noLink;
    private final int[] linkCounter;

    // linkCounter is shared by every block of a document, so each link has its own number
    RichAtoms(Graphics2D g, Font body, Color text, EmojiRegistry registry, int emojiScale, int[] linkCounter){
        this.g = g;
        this.body = body;
        this.text = text;
        this.registry = registry;
        this.emojiScale = emojiScale;
        this.linkCounter = linkCounter;
    }

    List<Atom> Build(List<Inline> inlines){
        for(Inline inline : inlines){
            if(inline.GetKind() == Inline.Kind.BREAK){
                word = null;
                atoms.add(new Atom(AtomKind.BREAK));
                continue;
            }
            NoteLink(inline.GetStyle().GetLink());
            if(inline.GetKind() == Inline.Kind.EMOJI && registry.Has(inline.GetText())){
                AddEmoji(inline);
            } else {
                AddText(inline.GetKind() == Inline.Kind.EMOJI ? ":" + inline.GetText() + ":" : inline.GetText(), inline.GetStyle());
            }
        }
        return atoms;
    }

    // A link keeps its number across the pieces it is made of, and gets a new one when a different (or no) link comes between
    private void NoteLink(String link){
        if(link == null){
            linkId = RichFragment.noLink;
        } else if(!link.equals(lastLink)){
            linkId = linkCounter[0]++;
        }
        lastLink = link;
    }

    private void AddEmoji(Inline inline){
        RunStyle style = inline.GetStyle();
        if(word == null){
            word = new Atom(AtomKind.WORD);
            atoms.add(word);
        }
        word.Add(new Piece(true, inline.GetText(), body, RichStyles.ColourOf(style, text), style.GetLink(), linkId, emojiScale, EmojiPainter.SizeAt(emojiScale)));
    }

    private void AddText(String characters, RunStyle style){
        Font font = RichStyles.FontOf(style, body);
        Color colour = RichStyles.ColourOf(style, text);
        FontMetrics metrics = g.getFontMetrics(font);
        int i = 0;
        while(i < characters.length()){
            boolean space = Character.isWhitespace(characters.charAt(i));
            int end = i;
            while(end < characters.length() && Character.isWhitespace(characters.charAt(end)) == space){
                end++;
            }
            if(space){
                AddSpace(font, colour, metrics, style.GetLink());
            } else {
                String piece = characters.substring(i, end);
                if(word == null){
                    word = new Atom(AtomKind.WORD);
                    atoms.add(word);
                }
                word.Add(new Piece(false, piece, font, colour, style.GetLink(), linkId, 0, metrics.stringWidth(piece)));
            }
            i = end;
        }
    }

    // Any run of white space is one space (two in a row, in different styles, are fine: a line only keeps the last)
    private void AddSpace(Font font, Color colour, FontMetrics metrics, String link){
        word = null;
        Atom space = new Atom(AtomKind.SPACE);
        String one = " ";
        space.Add(new Piece(false, one, font, colour, link, linkId, 0, metrics.stringWidth(one)));
        atoms.add(space);
    }
}
