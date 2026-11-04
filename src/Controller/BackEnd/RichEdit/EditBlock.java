package Controller.BackEnd.RichEdit;

import Controller.BackEnd.RichText.Block;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// One block being edited: a paragraph, bullet item or numbered item and its cells. newlinesBefore is as in Block (how many line breaks
// separate it from the block before). A numbered item keeps its number. Immutable
public final class EditBlock {
    private final Block.Kind kind;
    private final int number;
    private final int newlinesBefore;
    private final List<Cell> cells;

    public EditBlock(Block.Kind kind, int number, int newlinesBefore, List<Cell> cells){
        this.kind = kind;
        this.number = kind == Block.Kind.NUMBERED ? number : 0;
        this.newlinesBefore = newlinesBefore;
        this.cells = Collections.unmodifiableList(new ArrayList<>(cells));
    }

    public static EditBlock Paragraph(int newlinesBefore, List<Cell> cells){ return new EditBlock(Block.Kind.PARAGRAPH, 0, newlinesBefore, cells); }

    public Block.Kind GetKind(){ return kind; }
    public int GetNumber(){ return number; }
    public int GetNewlinesBefore(){ return newlinesBefore; }
    public List<Cell> GetCells(){ return cells; }

    public int Length(){ return cells.size(); }
    public boolean IsEmpty(){ return cells.isEmpty(); }
    public boolean IsListItem(){ return kind != Block.Kind.PARAGRAPH; }

    public EditBlock WithCells(List<Cell> changed){ return new EditBlock(kind, number, newlinesBefore, changed); }
    public EditBlock WithKind(Block.Kind changed, int changedNumber){ return new EditBlock(changed, changedNumber, newlinesBefore, cells); }
    public EditBlock WithNumber(int changed){ return new EditBlock(kind, changed, newlinesBefore, cells); }
    public EditBlock WithNewlinesBefore(int changed){ return new EditBlock(kind, number, changed, cells); }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof EditBlock)){
            return false;
        }
        EditBlock that = (EditBlock) other;
        return kind == that.kind && number == that.number && newlinesBefore == that.newlinesBefore && cells.equals(that.cells);
    }

    @Override
    public int hashCode(){
        return Objects.hash(kind, number, newlinesBefore, cells);
    }

    @Override
    public String toString(){
        return kind + "(" + newlinesBefore + (number > 0 ? "," + number : "") + ")" + cells;
    }
}
