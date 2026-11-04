package Screens.GameDetail;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import Controller.BackEnd.GameDetail.DetailPart;
import Controller.BackEnd.GameDetail.DetailSection;
import Model.Layout.Layout;
import Model.Layout.OverviewLayout;
import Model.Themes.TextStyle;
import Model.Themes.Theme;
import Screens.MainView.CardBox;
import Screens.RichText.RichBox;
import Screens.RichText.RichLayout;

// One section of a game's review: its heading (with MISSING when it is), then a card with what is in it. Text written in the review
// is shown formatted (bold, lists, links, emoji); the words that stand in for nothing written are plain
class DetailSectionPainter {
    private static final String keyPrefix = "detail:text:";
    private static final String separator = ":";
    private final Layout layout;
    private final RichBox rich;

    DetailSectionPainter(Layout layout, RichBox rich){
        this.layout = layout;
        this.rich = rich;
    }

    // What goes in the card, from the card's padding: a line of plain text, or a block of formatted text
    private static class Entry {
        private final String text;
        private final RichLayout block;
        private final int x, y;
        private final Font font;
        private final Color colour;

        private Entry(String text, RichLayout block, int x, int y, Font font, Color colour){
            this.text = text;
            this.block = block;
            this.x = x;
            this.y = y;
            this.font = font;
            this.colour = colour;
        }
    }

    // Everything one section is about to show, so it can be measured before it is drawn
    private class Body {
        private final List<Entry> entries = new ArrayList<>();
        private final Graphics2D g;
        private final int room;
        private final Font font;
        private final Color text, quiet;
        private int y;

        private Body(Graphics2D g, int room){
            this.g = g;
            this.room = room;
            this.font = Theme.Current.GetField().GetFont();
            this.text = Theme.Current.GetCell().GetText();
            this.quiet = Theme.Current.GetNav().GetDisabledText();
        }

        private void Plain(String words, int x, Font wordsFont, Color colour){
            int height = g.getFontMetrics(wordsFont).getHeight();
            entries.add(new Entry(words, null, x, y, wordsFont, colour));
            y += height;
        }

        private void Formatted(String stored, int x){
            RichLayout block = rich.Lay(g, stored, Math.max(1, room - x), font, text);
            entries.add(new Entry(null, block, x, y, font, text));
            y += block.GetHeight();
        }
    }

    int Paint(Graphics2D g, int top, int width, DetailSection section, Point mouse){
        OverviewLayout overview = layout.GetOverview();
        TextStyle heading = Theme.Current.GetHeading();
        FontMetrics headingMetrics = g.getFontMetrics(heading.GetFont());
        int y = top;
        g.setFont(heading.GetFont());
        g.setColor(heading.GetColour());
        g.drawString(section.GetHeading(), 0, y + headingMetrics.getAscent());
        if(section.GetState() == DetailSection.State.MISSING){
            g.setColor(Theme.Current.GetNav().GetSelected());
            g.drawString(GameDetailScreen.missingWord, headingMetrics.stringWidth(section.GetHeading()) + layout.GetShell().GetItemGap(), y + headingMetrics.getAscent());
        }
        y += Math.max(headingMetrics.getHeight(), overview.GetSectionHeaderHeight());

        int pad = overview.GetCardPadding();
        Body body = BodyOf(g, section, Math.max(1, width - pad - pad));
        Rectangle box = new Rectangle(0, y, width, pad + body.y + pad);
        CardBox.Paint(g, box, false);
        Graphics2D inside = (Graphics2D) g.create();
        try {
            inside.clipRect(box.x, box.y, box.width, box.height);
            PaintEntries(inside, body, box.x + pad, box.y + pad, mouse, section.GetFieldKey());
        } finally {
            inside.dispose();
        }
        return box.y + box.height - top;
    }

    private void PaintEntries(Graphics2D g, Body body, int left, int top, Point mouse, String key){
        int blocks = 0;
        for(Entry entry : body.entries){
            if(entry.block != null){
                rich.Paint(g, entry.block, left + entry.x, top + entry.y, mouse, keyPrefix + key + separator + blocks);
                blocks++;
            } else {
                g.setFont(entry.font);
                g.setColor(entry.colour);
                g.drawString(entry.text, left + entry.x, top + entry.y + g.getFontMetrics(entry.font).getAscent());
            }
        }
    }

    private Body BodyOf(Graphics2D g, DetailSection section, int room){
        Body body = new Body(g, room);
        switch(section.GetState()){
            case NOT_APPLICABLE:
                body.Plain(GameDetailScreen.notApplicableText, 0, body.font, body.quiet);
                return body;
            case MISSING:
                body.Plain(GameDetailScreen.missingText, 0, body.font, Theme.Current.GetNav().GetSelected());
                return body;
            case EMPTY:
                body.Plain(GameDetailScreen.emptyText, 0, body.font, body.quiet);
                return body;
            default:
                break;
        }
        if(section.IsRating()){
            body.Plain(section.GetText(), 0, Theme.Current.GetCell().GetFont(), section.IsUnknownRating() ? body.quiet : body.text);
            if(!section.GetComment().isEmpty()){
                body.Formatted(section.GetComment(), 0);
            }
            return body;
        }
        if(!section.GetText().isEmpty()){
            body.Formatted(section.GetText(), 0);
        }
        for(DetailPart part : section.GetParts()){
            PartEntries(body, part, 0);
        }
        return body;
    }

    // A part's name, its text, and its nested parts, each level in by the layout's indent
    private void PartEntries(Body body, DetailPart part, int depth){
        int indent = depth * layout.GetOverview().GetSeriesIndent();
        body.Plain(part.GetName(), indent, Theme.Current.GetHeading().GetFont(), Theme.Current.GetHeading().GetColour());
        if(part.IsEmpty()){
            body.Plain(GameDetailScreen.emptyPartText, indent, body.font, body.quiet);
            return;
        }
        if(!part.GetText().isEmpty()){
            body.Formatted(part.GetText(), indent);
        }
        for(DetailPart child : part.GetChildren()){
            PartEntries(body, child, depth + 1);
        }
    }
}
