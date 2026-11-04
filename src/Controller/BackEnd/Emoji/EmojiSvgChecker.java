package Controller.BackEnd.Emoji;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Checks an SVG against the emoji rules: one <svg viewBox="0 0 16 16" width="16" height="16"> holding only <rect x y width height fill>
// with whole numbers inside the grid and solid #rgb or #rrggbb fills, up to 16 KB (which is about 320 rects). Nothing is repaired or converted: a
// file that breaks a rule is refused with a plain list of what's wrong. Reads the text itself rather than with an XML parser, so a
// file can't pull in outside content
public class EmojiSvgChecker {
    public static final int maxBytes = 16 * 1024;

    private static final String svgNamespace = "http://www.w3.org/2000/svg";
    private static final String gridBox = "0 0 16 16";
    private static final String gridSide = "16";
    private static final String crisp = "crispEdges";
    private static final Pattern tag = Pattern.compile("<(/?)([A-Za-z][A-Za-z0-9:_.-]*)((?:\\s+[^\\s=/<>\"']+\\s*=\\s*(?:\"[^\"<]*\"|'[^'<]*'))*)\\s*(/?)>");
    private static final Pattern attribute = Pattern.compile("([^\\s=/<>\"']+)\\s*=\\s*(?:\"([^\"<]*)\"|'([^'<]*)')");
    private static final Pattern declaration = Pattern.compile("^\\s*<\\?xml[^>]*\\?>");
    private static final Pattern comment = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
    private static final Pattern wholeNumber = Pattern.compile("\\d{1,3}");
    private static final Pattern solidColour = Pattern.compile("#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})");
    private static final int shortHexLength = 3;
    private static final int hexRadix = 16;
    private static final int maxShownValue = 24;

    private static final String shapes = "Shapes it cannot draw";
    private static final String paint = "Paint it cannot do";
    private static final String styling = "Styling it cannot use";
    private static final String corners = "Rounded corners";
    private static final String numbers = "Numbers that are not whole";
    private static final String extras = "Extras it cannot use";
    private static final String missing = "Missing";
    private static final String outside = "Outside the 16 by 16 grid";
    private static final Set<String> paintNames = Set.of("stroke", "stroke-width", "stroke-linecap", "stroke-linejoin", "opacity", "fill-opacity",
            "filter", "mask", "clip-path", "fill-rule", "clip-rule");
    private static final Set<String> stylingNames = Set.of("style", "class", "transform");
    private static final Set<String> cornerNames = Set.of("rx", "ry");
    private static final Set<String> svgAttributes = Set.of("xmlns", "viewBox", "width", "height", "shape-rendering");
    private static final Set<String> rectAttributes = Set.of("x", "y", "width", "height", "fill");

    private final Map<String, Set<String>> problems = new LinkedHashMap<>();
    private final List<EmojiRect> rects = new ArrayList<>();
    private boolean svgSeen;
    private boolean svgOpen;
    private boolean svgClosed;
    private boolean rectOpen;
    private boolean badNesting;

    private EmojiSvgChecker(){
    }

    public static EmojiCheck Check(String text){
        int bytes = text.getBytes(StandardCharsets.UTF_8).length;
        EmojiSvgChecker checker = new EmojiSvgChecker();
        if(bytes > maxBytes){
            checker.Problem("Too big", "over " + (maxBytes / 1024) + " KB");
        } else {
            checker.Read(text);
        }
        return checker.Result(bytes);
    }

    private void Read(String text){
        String body = comment.matcher(declaration.matcher(text.startsWith("\uFEFF") ? text.substring(1) : text).replaceFirst("")).replaceAll("");
        Matcher matcher = tag.matcher(body);
        int at = 0;
        boolean strayText = false;
        while(matcher.find()){
            strayText |= !body.substring(at, matcher.start()).isBlank();
            Tag(matcher.group(1).isEmpty(), matcher.group(2), matcher.group(3), !matcher.group(4).isEmpty());
            at = matcher.end();
        }
        strayText |= !body.substring(at).isBlank();
        if(strayText){
            Problem("Not plain SVG tags", "other text or markup");
        }
        if(!svgSeen){
            Problem("Not an SVG file", "no <svg> element");
        } else if(!svgClosed || badNesting){
            Problem("Tags that don't match up", "the <svg> and <rect> tags are not opened and closed properly");
        }
        if(rects.isEmpty() && svgSeen){
            Problem("Draws nothing", "no <rect> elements");
        }
    }

    private void Tag(boolean opening, String name, String attributeText, boolean selfClosing){
        if(name.equals("svg")){
            SvgTag(opening, attributeText, selfClosing);
        } else if(name.equals("rect")){
            RectTag(opening, attributeText, selfClosing);
        } else {
            Problem(shapes, "<" + name + ">");
        }
    }

    private void SvgTag(boolean opening, String attributeText, boolean selfClosing){
        if(!opening){
            badNesting |= !svgOpen || rectOpen;
            svgOpen = false;
            svgClosed = true;
            return;
        }
        badNesting |= svgSeen || selfClosing;
        svgSeen = true;
        svgOpen = true;
        CheckSvgAttributes(Attributes(attributeText));
    }

    private void RectTag(boolean opening, String attributeText, boolean selfClosing){
        if(!opening){
            badNesting |= !rectOpen;
            rectOpen = false;
            return;
        }
        badNesting |= !svgOpen;
        rectOpen = !selfClosing;
        ReadRect(Attributes(attributeText));
    }

    // Name and value of each attribute; a repeated name is a problem
    private Map<String, String> Attributes(String attributeText){
        Map<String, String> found = new LinkedHashMap<>();
        Matcher matcher = attribute.matcher(attributeText);
        while(matcher.find()){
            String value = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
            if(found.put(matcher.group(1), value) != null){
                Problem(extras, matcher.group(1) + " twice");
            }
        }
        return found;
    }

    private void CheckSvgAttributes(Map<String, String> attributes){
        for(Map.Entry<String, String> entry : attributes.entrySet()){
            if(!svgAttributes.contains(entry.getKey())){
                UnsupportedAttribute(entry.getKey());
            }
        }
        Expect(attributes, "xmlns", svgNamespace, false);
        Expect(attributes, "viewBox", gridBox, true);
        Expect(attributes, "width", gridSide, true);
        Expect(attributes, "height", gridSide, true);
        Expect(attributes, "shape-rendering", crisp, false);
    }

    // The attribute has to be exactly this value (whitespace in a viewBox aside); it may be left out unless required
    private void Expect(Map<String, String> attributes, String name, String value, boolean required){
        String found = attributes.get(name);
        if(found == null){
            if(required){
                Problem(missing, name + "=\"" + value + "\"");
            }
        } else if(!found.trim().replaceAll("\\s+", " ").equals(value)){
            Problem(name.equals("viewBox") || name.equals("width") || name.equals("height") ? "Not a 16 by 16 grid" : extras, name + "=\"" + Shown(found) + "\"");
        }
    }

    private void ReadRect(Map<String, String> attributes){
        for(Map.Entry<String, String> entry : attributes.entrySet()){
            if(!rectAttributes.contains(entry.getKey())){
                UnsupportedAttribute(entry.getKey());
            }
        }
        int[] numbersRead = new int[] { Whole(attributes, "x"), Whole(attributes, "y"), Whole(attributes, "width"), Whole(attributes, "height") };
        int rgb = Colour(attributes.get("fill"));
        boolean complete = rgb >= 0;
        for(int number : numbersRead){
            complete &= number >= 0;
        }
        if(complete){
            AddRect(numbersRead[0], numbersRead[1], numbersRead[2], numbersRead[3], rgb);
        }
    }

    private void AddRect(int x, int y, int width, int height, int rgb){
        if(width < 1 || height < 1){
            Problem(outside, "a rect with no width or height");
        } else if(x + width > EmojiSvg.gridSize || y + height > EmojiSvg.gridSize){
            Problem(outside, "a rect reaching past " + EmojiSvg.gridSize);
        } else {
            rects.add(new EmojiRect(x, y, width, height, rgb));
        }
    }

    // The attribute as a whole number, or -1 after noting why not
    private int Whole(Map<String, String> attributes, String name){
        String value = attributes.get(name);
        if(value == null){
            Problem(missing, name);
            return -1;
        }
        if(!wholeNumber.matcher(value).matches()){
            Problem(numbers, name + "=\"" + Shown(value) + "\"");
            return -1;
        }
        return Integer.parseInt(value);
    }

    // 0xRRGGBB for a solid #rgb or #rrggbb fill, or -1 after noting why not
    private int Colour(String fill){
        if(fill == null){
            Problem(missing, "fill");
            return -1;
        }
        Matcher matcher = solidColour.matcher(fill);
        if(!matcher.matches()){
            Problem(paint, "fill=\"" + Shown(fill) + "\"");
            return -1;
        }
        String hex = matcher.group(1);
        if(hex.length() == shortHexLength){
            hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
        }
        return Integer.parseInt(hex, hexRadix);
    }

    private void UnsupportedAttribute(String name){
        if(paintNames.contains(name)){
            Problem(paint, name);
        } else if(stylingNames.contains(name) || name.startsWith("on")){
            Problem(styling, name);
        } else if(cornerNames.contains(name)){
            Problem(corners, name);
        } else {
            Problem(extras, name);
        }
    }

    private static String Shown(String value){
        return value.length() > maxShownValue ? value.substring(0, maxShownValue) + "..." : value;
    }

    private void Problem(String heading, String detail){
        problems.computeIfAbsent(heading, k -> new LinkedHashSet<>()).add(detail);
    }

    private EmojiCheck Result(int bytes){
        List<String> lines = new ArrayList<>();
        for(Map.Entry<String, Set<String>> entry : problems.entrySet()){
            lines.add(entry.getKey() + ": " + String.join(", ", entry.getValue()));
        }
        return new EmojiCheck(lines.isEmpty() ? new EmojiSvg(rects) : null, lines, bytes);
    }
}
