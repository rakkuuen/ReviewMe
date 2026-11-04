package Screens.RichText;

import Controller.BackEnd.Emoji.EmojiRegistry;
import Controller.BackEnd.RichText.DocumentCache;
import Controller.BackEnd.RichText.RichDocument;
import Model.Layout.RichSizes;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;

// What a screen needs to show formatted text: the emoji, the reader of stored text (with the documents it has read kept), and the
// laying out and drawing. One is made at start up and shared by every screen
public class RichTextKit {
    private final EmojiRegistry registry;
    private final EmojiPainter emojiPainter = new EmojiPainter();
    private final DocumentCache documents;

    public RichTextKit(EmojiRegistry registry){
        this.registry = registry;
        this.documents = new DocumentCache(registry::Has);
    }

    public EmojiRegistry GetRegistry(){ return registry; }

    // Call after the emoji change (another theme's set, or one of the user's added or removed), so text is read again
    public void EmojiChanged(){
        documents.Clear();
    }

    // The stored text laid out to a width, in body (the field's own font) and text (its colour)
    public RichLayout Lay(Graphics2D g, String stored, int width, Font body, Color text, RichSizes sizes){
        return Lay(g, documents.Get(stored), width, body, text, sizes);
    }

    // The same for a document already in hand (one being edited)
    public RichLayout Lay(Graphics2D g, RichDocument document, int width, Font body, Color text, RichSizes sizes){
        return RichLayouter.Lay(document, g, width, body, text, sizes, registry);
    }

    // Draws a layout with its top left at x, y. mouse (or null) is in the same space, and decides which link is lit
    public void Paint(Graphics2D g, RichLayout layout, int x, int y, Point mouse, RichSizes sizes){
        RichPainter.Paint(g, layout, x, y, mouse, sizes, registry, emojiPainter);
    }
}
