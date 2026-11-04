package Controller.BackEnd.RichEdit;

import Controller.BackEnd.LinkOpener;
import Controller.BackEnd.RichText.Block;
import Controller.BackEnd.RichText.RichDocument;
import Controller.BackEnd.RichText.RichTextParser;
import Controller.BackEnd.RichText.RunStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

// The text of one field being edited, with its caret and selection, everything the toolbar and keys do to it, and undo. Starts from the
// stored Markdown; ToMarkdown gives the text to store (the original, byte for byte, if nothing was changed). No drawing in here: a
// screen asks for the state, shows it, and calls what the user did. Version goes up with every change, so a screen knows to repaint
public class RichEditor {
    private final RichDocument original;
    private final Predicate<String> knownEmoji;
    private final History history = new History();
    private EditState state;
    private int version;

    public RichEditor(String stored, Predicate<String> knownEmoji){
        this.knownEmoji = knownEmoji;
        this.original = RichTextParser.Parse(stored == null ? "" : stored, knownEmoji);
        this.state = new EditState(DocConvert.FromDocument(original), 0, 0, null);
    }

    // ---- what the text is

    public EditState GetState(){ return state; }
    public int GetVersion(){ return version; }
    public int GetCaret(){ return state.GetCaret(); }
    public int GetAnchor(){ return state.GetAnchor(); }
    public boolean HasSelection(){ return state.HasSelection(); }
    public int SelectionStart(){ return state.SelectionStart(); }
    public int SelectionEnd(){ return state.SelectionEnd(); }
    public int Length(){ return state.Length(); }
    public boolean IsEmpty(){ return state.IsEmpty(); }
    public boolean CanUndo(){ return history.CanUndo(); }
    public boolean CanRedo(){ return history.CanRedo(); }

    // The text as it will be stored: empty blocks are gone
    public RichDocument ToDocument(){
        return DocConvert.ToDocument(state.GetBlocks(), false, original);
    }

    // The text as it is shown while editing: an empty block is still there to put the caret in
    public RichDocument ToDisplayDocument(){
        return DocConvert.ToDocument(state.GetBlocks(), true, original);
    }

    // The Markdown to store. If nothing was changed it is the original text exactly (and a text that held no words stays as it was while
    // it still holds none)
    public String ToMarkdown(){
        RichDocument now = ToDocument();
        boolean stillBlank = original.GetBlocks().isEmpty() && !now.HasContent();
        if(!history.CanUndo() || now.SameContentAs(original) || stillBlank){
            return original.ToMarkdown();
        }
        return now.ToMarkdown(knownEmoji);
    }

    // Has the text been changed from what it started as
    public boolean IsChanged(){
        return !ToMarkdown().equals(original.ToMarkdown());
    }

    // Is anything written (words or an emoji), as the MISSING rule counts it
    public boolean HasContent(){
        return ToDocument().HasContent();
    }

    public String SelectedPlainText(){
        return Clipping.PlainText(state, state.SelectionStart(), state.SelectionEnd());
    }

    public String SelectedMarkdown(){
        return Clipping.Markdown(state, state.SelectionStart(), state.SelectionEnd(), knownEmoji);
    }

    // ---- what the buttons show

    public RunStyle TypingStyle(){ return Typing.StyleAt(state); }

    public Choice Bold(){
        return HasSelection() ? StyleEdits.Flag(Showing(), RunStyle::IsBold) : Choice.Of(TypingStyle().IsBold());
    }

    public Choice Italic(){
        return HasSelection() ? StyleEdits.Flag(Showing(), RunStyle::IsItalic) : Choice.Of(TypingStyle().IsItalic());
    }

    // The palette key of the colour in use (value null: the default), or mixed
    public Choice Colour(){
        return HasSelection() ? StyleEdits.Value(Showing(), RunStyle::GetColour) : Choice.Value(TypingStyle().GetColour());
    }

    public Choice Font(){
        return HasSelection() ? StyleEdits.Value(Showing(), RunStyle::GetFont) : Choice.Value(TypingStyle().GetFont());
    }

    // The address of the link the selection (or caret) is in: on with the address, off, or mixed
    public Choice Link(){
        if(HasSelection()){
            return StyleEdits.Value(Showing(), RunStyle::GetLink);
        }
        StyleEdits.LinkSpan span = StyleEdits.LinkAt(state, state.GetCaret());
        return Choice.Value(span == null ? null : span.Address());
    }

    // Is there text selected, or the caret in a link, so a link can be made or changed
    public boolean CanLink(){
        return HasSelection() || StyleEdits.LinkAt(state, state.GetCaret()) != null;
    }

    // The kind every block the selection touches is, or null when they differ
    public Block.Kind ListKind(){ return ListEdits.KindOfSelection(state); }

    // The letters of an emoji name being typed after a ":", or null
    public String EmojiPrefix(){ return Typing.EmojiPrefix(state); }

    // The link under the caret: where it starts and ends, or null
    public int[] LinkRange(){
        StyleEdits.LinkSpan span = StyleEdits.LinkAt(state, state.GetCaret());
        return span == null ? null : new int[] { span.Start(), span.End() };
    }

    private List<Cell> Showing(){
        return StyleEdits.Showing(state, state.SelectionStart(), state.SelectionEnd());
    }

    // ---- moving the caret and selecting

    private void Move(EditState moved){
        history.Break();
        Set(moved);
    }

    public void MoveTo(int position, boolean extend){
        Move(state.WithSelection(extend ? state.GetAnchor() : position, position));
    }

    // One step left or right. With a selection and no Shift, the caret goes to that end of it
    public void MoveBy(int direction, boolean extend){
        if(!extend && HasSelection()){
            MoveTo(direction < 0 ? state.SelectionStart() : state.SelectionEnd(), false);
            return;
        }
        MoveTo(state.GetCaret() + (direction < 0 ? -1 : 1), extend);
    }

    public void MoveWord(int direction, boolean extend){
        MoveTo(direction < 0 ? Words.Previous(state, state.GetCaret()) : Words.Next(state, state.GetCaret()), extend);
    }

    public void SelectAll(){
        Move(state.WithSelection(0, state.Length()));
    }

    // Double click: the word (or emoji) at a position
    public void SelectWordAt(int position){
        int[] word = Words.Around(state, Math.max(0, Math.min(position, state.Length())));
        Move(state.WithSelection(word[0], word[1]));
    }

    // Triple click: the whole block at a position
    public void SelectBlockAt(int position){
        int index = state.BlockIndexAt(Math.max(0, Math.min(position, state.Length())));
        Move(state.WithSelection(state.BlockStart(index), state.BlockEnd(index)));
    }

    // ---- typing and deleting

    // Types text at the caret (replacing the selection), a character at a time so the Markdown shortcuts work. Control characters are left out
    public void Type(String text){
        text.codePoints().filter(cp -> !Character.isISOControl(cp)).forEach(this::TypeCharacter);
    }

    private void TypeCharacter(int codePoint){
        history.Record("type", state);
        RunStyle style = TypingStyle();
        EditState next = HasSelection() ? TextEdits.Delete(state, state.SelectionStart(), state.SelectionEnd()) : state;
        List<Cell> typed = new ArrayList<>();
        typed.add(Cell.Char(codePoint, style));
        Set(TextEdits.Insert(next, typed));
        EditState converted = Shortcuts.After(state, codePoint, knownEmoji);
        if(converted != null){
            history.Record(null, state);
            Set(converted);
        }
    }

    // Enter: a new block (a new list item inside a list; an empty list item turns into a paragraph instead)
    public void Enter(){
        history.Record(null, state);
        EditState base = HasSelection() ? TextEdits.Delete(state, state.SelectionStart(), state.SelectionEnd()) : state;
        EditBlock block = base.BlockAt(base.BlockIndexAt(base.GetCaret()));
        if(block.IsListItem() && block.IsEmpty()){
            Set(ListEdits.SetKind(base, base.BlockIndexAt(base.GetCaret()), Block.Kind.PARAGRAPH));
            return;
        }
        RunStyle style = Typing.StyleAt(base);
        EditState split = TextEdits.Split(base);
        int index = split.BlockIndexAt(split.GetCaret());
        List<EditBlock> blocks = new ArrayList<>(split.GetBlocks());
        ListEdits.Renumber(blocks, index - 1, index);
        boolean carryStyle = !style.IsPlain() && blocks.get(index).IsEmpty();
        Set(split.With(blocks, split.GetCaret(), split.GetCaret(), carryStyle ? style : null));
    }

    // Shift+Enter: a line break inside the block
    public void SoftBreak(){
        history.Record(null, state);
        EditState base = HasSelection() ? TextEdits.Delete(state, state.SelectionStart(), state.SelectionEnd()) : state;
        List<Cell> lineBreak = new ArrayList<>();
        lineBreak.add(Cell.Break());
        Set(TextEdits.Insert(base, lineBreak));
    }

    // Backspace: the selection, else the list marker of a list item at its start, else the character before the caret
    public void Backspace(){
        if(HasSelection()){
            DeleteSelection();
            return;
        }
        int caret = state.GetCaret();
        int index = state.BlockIndexAt(caret);
        boolean atStart = caret == state.BlockStart(index);
        if(atStart && state.BlockAt(index).IsListItem()){
            history.Record(null, state);
            Set(ListEdits.SetKind(state, index, Block.Kind.PARAGRAPH));
        } else if(caret > 0){
            Remove("backspace", caret - 1, caret);
        }
    }

    // Delete: the selection, else the character after the caret (at the end of a block, the next block joins this one)
    public void Delete(){
        if(HasSelection()){
            DeleteSelection();
        } else if(state.GetCaret() < state.Length()){
            Remove("delete", state.GetCaret(), state.GetCaret() + 1);
        }
    }

    public void BackspaceWord(){
        if(HasSelection()){
            DeleteSelection();
        } else {
            Remove(null, Words.Previous(state, state.GetCaret()), state.GetCaret());
        }
    }

    public void DeleteWord(){
        if(HasSelection()){
            DeleteSelection();
        } else {
            Remove(null, state.GetCaret(), Words.Next(state, state.GetCaret()));
        }
    }

    // Takes out the selection (cut)
    public void DeleteSelection(){
        if(HasSelection()){
            Remove(null, state.SelectionStart(), state.SelectionEnd());
        }
    }

    private void Remove(String kind, int from, int to){
        history.Record(kind, state);
        EditState removed = TextEdits.Delete(state, from, to);
        Set(state.BlockIndexAt(from) == state.BlockIndexAt(to) ? removed : Renumbered(removed));
    }

    // The state with the numbering of the blocks round where it changed put right
    private static EditState Renumbered(EditState changed){
        List<EditBlock> blocks = new ArrayList<>(changed.GetBlocks());
        int at = changed.BlockIndexAt(changed.GetCaret());
        ListEdits.Renumber(blocks, Math.max(0, at - 1), Math.min(blocks.size() - 1, at + 1));
        return changed.With(blocks, changed.GetAnchor(), changed.GetCaret(), changed.GetPending());
    }

    public void InsertEmoji(String name){
        history.Record(null, state);
        RunStyle style = TypingStyle();
        EditState base = HasSelection() ? TextEdits.Delete(state, state.SelectionStart(), state.SelectionEnd()) : state;
        List<Cell> emoji = new ArrayList<>();
        emoji.add(Cell.Emoji(name, style));
        Set(TextEdits.Insert(base, emoji));
    }

    // Replaces the ":name" being typed with the emoji (what picking one from the list as you type does)
    public void CompleteEmoji(String name){
        String prefix = EmojiPrefix();
        if(prefix == null){
            return;
        }
        int caret = state.GetCaret();
        history.Record(null, state);
        EditState without = TextEdits.Delete(state, caret - prefix.length() - 1, caret);
        List<Cell> emoji = new ArrayList<>();
        emoji.add(Cell.Emoji(name, TypingStyleBefore(caret - prefix.length() - 1)));
        Set(TextEdits.Insert(without, emoji));
    }

    private RunStyle TypingStyleBefore(int position){
        Cell cell = state.CellAfter(position);
        return cell == null ? RunStyle.plain : cell.GetStyle();
    }

    // ---- the clipboard

    // Pastes plain text (":name:" of a known emoji comes in as the emoji)
    public void PastePlain(String text){
        Paste(Clipping.FromPlain(text, TypingStyle(), knownEmoji));
    }

    // Pastes Markdown (what the editor itself copies), styles and all
    public void PasteMarkdown(String markdown){
        Paste(Clipping.FromDocument(RichTextParser.Parse(markdown, knownEmoji)));
    }

    private void Paste(List<EditBlock> blocks){
        if(blocks.isEmpty()){
            return;
        }
        history.Record(null, state);
        EditState base = HasSelection() ? TextEdits.Delete(state, state.SelectionStart(), state.SelectionEnd()) : state;
        EditState pasted = TextEdits.InsertBlocks(base, blocks);
        Set(blocks.size() == 1 ? pasted : Renumbered(pasted));
    }

    // ---- styles

    public void ToggleBold(){
        boolean on = Bold().IsOn();
        Restyle(style -> style.WithBold(!on));
    }

    public void ToggleItalic(){
        boolean on = Italic().IsOn();
        Restyle(style -> style.WithItalic(!on));
    }

    // key is a palette key, or null for the default colour
    public void SetColour(String key){ Restyle(style -> style.WithColour(key)); }

    // key is a font key (serif, mono), or null for the body font
    public void SetFont(String key){ Restyle(style -> style.WithFont(key)); }

    public void ClearFormatting(){
        Restyle(style -> style.WithBold(false).WithItalic(false).WithColour(null).WithFont(null));
    }

    // Changes the style of the selection, or what the next character typed takes
    private void Restyle(UnaryOperator<RunStyle> change){
        history.Record(null, state);
        if(HasSelection()){
            Set(StyleEdits.Apply(state, state.SelectionStart(), state.SelectionEnd(), change));
        } else {
            Set(state.WithPending(change.apply(TypingStyle())));
        }
    }

    // Can this address be stored as a link: http or https, with no brackets in it (a space is already refused as not an address)
    public static boolean IsLinkable(String address){
        return address != null && LinkOpener.IsWebAddress(address) && address.indexOf('(') < 0 && address.indexOf(')') < 0;
    }

    // Makes the selection a link (or changes the address of the link the caret is in). False if the address can't be stored or there is
    // nothing to link
    public boolean SetLink(String address){
        if(!IsLinkable(address) || !CanLink()){
            return false;
        }
        int[] range = HasSelection() ? new int[] { state.SelectionStart(), state.SelectionEnd() } : LinkRange();
        history.Record(null, state);
        Set(StyleEdits.Apply(state, range[0], range[1], style -> style.WithLink(address)));
        return true;
    }

    // Takes the link off the selection, or off the whole link the caret is in
    public void RemoveLink(){
        int[] range = HasSelection() ? new int[] { state.SelectionStart(), state.SelectionEnd() } : LinkRange();
        if(range == null){
            return;
        }
        history.Record(null, state);
        Set(StyleEdits.Apply(state, range[0], range[1], style -> style.WithLink(null)));
    }

    public void ToggleBullet(){ ToggleList(Block.Kind.BULLET); }
    public void ToggleNumbered(){ ToggleList(Block.Kind.NUMBERED); }

    private void ToggleList(Block.Kind kind){
        history.Record(null, state);
        Set(ListEdits.Toggle(state, kind));
    }

    // ---- undo

    public void Undo(){
        EditState back = history.Undo(state);
        if(back != null){
            Set(back);
        }
    }

    public void Redo(){
        EditState forward = history.Redo(state);
        if(forward != null){
            Set(forward);
        }
    }

    private void Set(EditState changed){
        state = Normal.Of(changed);
        version++;
    }
}
