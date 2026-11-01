package Model.Layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// The layout settings can't be used. Every problem found is listed, each naming the key (and file) at fault
public class LayoutException extends Exception {
    private final List<String> problems;

    public LayoutException(List<String> problems){
        super(String.join("\n", problems));
        this.problems = Collections.unmodifiableList(new ArrayList<>(problems));
    }

    public List<String> GetProblems(){ return problems; }
}
