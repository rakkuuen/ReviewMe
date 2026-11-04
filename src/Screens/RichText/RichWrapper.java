package Screens.RichText;

import Screens.RichText.RichAtoms.Atom;
import Screens.RichText.RichAtoms.Piece;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

// Breaks a block's words and spaces into lines no wider than a width: at the spaces where it can, and in the middle of a word only when
// one word is wider than a whole line. A forced break starts a new line (an empty one if the line was empty). A space at the start or
// end of a line is dropped (a pending space only counts once a word follows it on the same line)
final class RichWrapper {
    private final Graphics2D g;
    private final int room;
    private final List<List<Piece>> lines = new ArrayList<>();
    private List<Piece> current = new ArrayList<>();
    private int used;
    private Atom pendingSpace;

    private RichWrapper(Graphics2D g, int room){
        this.g = g;
        this.room = room;
    }

    static List<List<Piece>> Wrap(List<Atom> atoms, int room, Graphics2D g){
        RichWrapper wrapper = new RichWrapper(g, room);
        for(Atom atom : atoms){
            switch(atom.GetKind()){
                case BREAK:
                    wrapper.EndLine();
                    break;
                case SPACE:
                    wrapper.pendingSpace = atom;
                    break;
                default:
                    wrapper.AddWord(atom);
                    break;
            }
        }
        if(!wrapper.current.isEmpty()){
            wrapper.lines.add(wrapper.current);
        }
        return wrapper.lines;
    }

    private void EndLine(){
        lines.add(current);
        current = new ArrayList<>();
        used = 0;
    }

    private void AddWord(Atom word){
        int spaceWidth = pendingSpace == null || current.isEmpty() ? 0 : pendingSpace.GetWidth();
        if(!current.isEmpty() && used + spaceWidth + word.GetWidth() > room){
            EndLine();
            spaceWidth = 0;
        }
        if(current.isEmpty() && word.GetWidth() > room){
            SplitWord(word);
            return;
        }
        if(spaceWidth > 0){
            current.addAll(pendingSpace.GetPieces());
            used += spaceWidth;
        }
        pendingSpace = null;
        current.addAll(word.GetPieces());
        used += word.GetWidth();
    }

    // A word wider than the whole line goes across as many lines as it needs
    private void SplitWord(Atom word){
        pendingSpace = null;
        for(Piece piece : word.GetPieces()){
            if(piece.IsEmoji()){
                if(!current.isEmpty() && used + piece.GetWidth() > room){
                    EndLine();
                }
                current.add(piece);
                used += piece.GetWidth();
            } else {
                SplitText(piece);
            }
        }
    }

    private void SplitText(Piece piece){
        FontMetrics metrics = g.getFontMetrics(piece.GetFont());
        String rest = piece.GetText();
        while(!rest.isEmpty()){
            int keep = rest.length();
            while(keep > 0 && used + metrics.stringWidth(rest.substring(0, keep)) > room){
                keep--;
            }
            if(keep == 0 && current.isEmpty()){
                keep = 1;
            }
            if(keep == 0){
                EndLine();
                continue;
            }
            String part = rest.substring(0, keep);
            int width = metrics.stringWidth(part);
            current.add(new Piece(false, part, piece.GetFont(), piece.GetColour(), piece.GetLink(), piece.GetLinkId(), 0, width));
            used += width;
            rest = rest.substring(keep);
        }
    }
}
