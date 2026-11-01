package Model.Layout;

// The little bit of arithmetic layout code needs, so screens never write a literal 2 to centre something
public class LayoutMath {
    private static final int two = 2;

    // Half of a length, rounded down
    public static int Half(int length){
        return length / two;
    }

    // Where to start a piece of the given size so it sits in the middle of the space. Negative when the piece is bigger than the space
    public static int Centre(int space, int size){
        return Half(space - size);
    }
}
