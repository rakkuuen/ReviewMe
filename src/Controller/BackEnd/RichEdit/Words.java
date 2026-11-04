package Controller.BackEnd.RichEdit;

// Where words begin and end, for moving and selecting by word. A word is a run of letters, digits and underscores, a run of other
// characters is one, an emoji and a line break are each one
final class Words {
    private enum Class { WORD, SPACE, OTHER, SINGLE }

    private Words(){
    }

    private static Class ClassOf(Cell cell){
        if(cell.IsEmoji() || cell.IsBreak()){
            return Class.SINGLE;
        }
        if(cell.IsSpace()){
            return Class.SPACE;
        }
        return cell.IsWordChar() ? Class.WORD : Class.OTHER;
    }

    // The start of the next word after a position (stepping over the end of a block counts as one step)
    static int Next(EditState state, int position){
        Cell cell = state.CellAfter(position);
        if(cell == null){
            return Math.min(state.Length(), position + 1);
        }
        int at = position + 1;
        if(ClassOf(cell) != Class.SINGLE){
            at = RunEnd(state, position, ClassOf(cell));
        }
        while(state.CellAfter(at) != null && ClassOf(state.CellAfter(at)) == Class.SPACE){
            at++;
        }
        return at;
    }

    // The start of the word before a position, or of the one it is inside
    static int Previous(EditState state, int position){
        int at = position;
        while(state.CellBefore(at) != null && ClassOf(state.CellBefore(at)) == Class.SPACE){
            at--;
        }
        Cell cell = state.CellBefore(at);
        if(cell == null){
            return Math.max(0, at - 1);
        }
        return ClassOf(cell) == Class.SINGLE ? at - 1 : RunStart(state, at, ClassOf(cell));
    }

    // The word at a position, for a double click: from, to. A space or a mark is its own run; an emoji is itself. At the end of a block the
    // word before is taken
    static int[] Around(EditState state, int position){
        Cell cell = state.CellAfter(position) != null ? state.CellAfter(position) : state.CellBefore(position);
        if(cell == null){
            return new int[] { position, position };
        }
        int inside = state.CellAfter(position) != null ? position : position - 1;
        if(ClassOf(cell) == Class.SINGLE){
            return new int[] { inside, inside + 1 };
        }
        return new int[] { RunStart(state, inside + 1, ClassOf(cell)), RunEnd(state, inside, ClassOf(cell)) };
    }

    private static int RunEnd(EditState state, int position, Class kind){
        int at = position;
        while(state.CellAfter(at) != null && ClassOf(state.CellAfter(at)) == kind){
            at++;
        }
        return at;
    }

    private static int RunStart(EditState state, int position, Class kind){
        int at = position;
        while(state.CellBefore(at) != null && ClassOf(state.CellBefore(at)) == kind){
            at--;
        }
        return at;
    }
}
