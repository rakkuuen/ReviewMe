package Checks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

// Finds numbers written straight into UI source code. Sizes belong in layout.properties and colours and fonts in the theme, so the only
// numbers a screen may write are 0 and 1 (and 0.0, 1.0, 0f, 1f, 0L, 1L). Comments, strings and characters are ignored, and so are digits
// inside names like utf8. A constant such as "private static final int width = 500;" counts: the 500 is the problem.
// Run:  java -cp bin Checks.NoLiteralCheck <folder or file> ...   (exit code 1 if anything is found)
public class NoLiteralCheck {

    public static class Violation {
        private final String file;
        private final int line;
        private final String literal;

        public Violation(String file, int line, String literal){
            this.file = file;
            this.line = line;
            this.literal = literal;
        }

        public String GetFile(){ return file; }
        public int GetLine(){ return line; }
        public String GetLiteral(){ return literal; }

        @Override
        public String toString(){
            return file + ":" + line + "  " + literal;
        }
    }

    private static final Pattern allowedWhole = Pattern.compile("^[01][lLfFdD]?$");
    private static final Pattern allowedDecimal = Pattern.compile("^[01]\\.0+[fFdD]?$");

    // Every disallowed number in one source file, with its line
    public static List<Violation> ScanSource(String fileName, String source){
        List<Violation> found = new ArrayList<>();
        int line = 1;
        int i = 0;
        int length = source.length();
        while(i < length){
            char c = source.charAt(i);
            if(c == '\n'){
                line++;
                i++;
            } else if(StartsWith(source, i, "//")){
                while(i < length && source.charAt(i) != '\n'){
                    i++;
                }
            } else if(StartsWith(source, i, "/*")){
                i += 2;
                while(i < length && !StartsWith(source, i, "*/")){
                    line += source.charAt(i) == '\n' ? 1 : 0;
                    i++;
                }
                i = Math.min(length, i + 2);
            } else if(StartsWith(source, i, "\"\"\"")){
                i += 3;
                while(i < length && !StartsWith(source, i, "\"\"\"")){
                    if(source.charAt(i) == '\\'){
                        i++;
                    }
                    line += i < length && source.charAt(i) == '\n' ? 1 : 0;
                    i++;
                }
                i = Math.min(length, i + 3);
            } else if(c == '"' || c == '\''){
                i = SkipQuoted(source, i, c);
            } else if(IsIdentifierStart(c)){
                while(i < length && IsIdentifierPart(source.charAt(i))){
                    i++;
                }
            } else if(IsDigit(c) || (c == '.' && i + 1 < length && IsDigit(source.charAt(i + 1)))){
                int end = EndOfNumber(source, i);
                String literal = source.substring(i, end);
                if(!IsAllowed(literal)){
                    found.add(new Violation(fileName, line, literal));
                }
                i = end;
            } else {
                i++;
            }
        }
        return found;
    }

    // Every .java file under the folder (or the one file), in name order
    public static List<Violation> ScanPath(File path) throws IOException {
        List<Violation> found = new ArrayList<>();
        if(path.isDirectory()){
            File[] children = path.listFiles();
            Arrays.sort(children);
            for(File child : children){
                found.addAll(ScanPath(child));
            }
        } else if(path.getName().endsWith(".java")){
            found.addAll(ScanSource(path.getPath(), new String(Files.readAllBytes(path.toPath()), StandardCharsets.UTF_8)));
        }
        return found;
    }

    public static void main(String[] args) throws IOException {
        if(args.length == 0){
            System.out.println("Usage: java -cp bin Checks.NoLiteralCheck <folder or file> ...");
            System.exit(2);
        }
        int total = 0;
        for(String arg : args){
            File path = new File(arg);
            if(!path.exists()){
                System.out.println("Not found: " + arg);
                System.exit(2);
            }
            for(Violation violation : ScanPath(path)){
                System.out.println(violation);
                total++;
            }
        }
        System.out.println(total == 0 ? "No numbers written into the code." : total + " number(s) written into the code. Move them to layout.properties or the theme.");
        System.exit(total == 0 ? 0 : 1);
    }

    private static boolean IsAllowed(String literal){
        return allowedWhole.matcher(literal).matches() || allowedDecimal.matcher(literal).matches();
    }

    private static boolean StartsWith(String source, int at, String text){
        return source.startsWith(text, at);
    }

    // Past the closing quote of a string or character, or to the end of the line if it never closes
    private static int SkipQuoted(String source, int start, char quote){
        int i = start + 1;
        while(i < source.length() && source.charAt(i) != quote && source.charAt(i) != '\n'){
            i += source.charAt(i) == '\\' ? 2 : 1;
        }
        return i < source.length() && source.charAt(i) == quote ? i + 1 : i;
    }

    // One past the last character of the number that starts here, including its suffix and any exponent (1e-3, 0x1p+2)
    private static int EndOfNumber(String source, int start){
        boolean hex = StartsWith(source, start, "0x") || StartsWith(source, start, "0X");
        int i = start;
        while(i < source.length()){
            char c = source.charAt(i);
            if(IsIdentifierPart(c) || c == '.'){
                i++;
                boolean exponent = hex ? (c == 'p' || c == 'P') : (c == 'e' || c == 'E');
                if(exponent && i < source.length() && (source.charAt(i) == '+' || source.charAt(i) == '-')){
                    i++;
                }
            } else {
                break;
            }
        }
        return i;
    }

    private static boolean IsDigit(char c){
        return c >= '0' && c <= '9';
    }

    private static boolean IsIdentifierStart(char c){
        return Character.isLetter(c) || c == '_' || c == '$';
    }

    private static boolean IsIdentifierPart(char c){
        return IsIdentifierStart(c) || IsDigit(c);
    }
}
