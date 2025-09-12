import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//testing...
public class TexPdfTh implements Runnable {
    InputStream input;
    String name;
    boolean verbose;
    static boolean debug;
    boolean merge;
    boolean free;
    String resizeFactor;
    static String buildDirectory = "aspect-output-aux";
    static boolean pdfBuild = true;
    static boolean graph;
    // <fileId, frameNumber>
    static TreeMap<Integer, Integer> files = new TreeMap<>();
    static String beforeFilename = "before.tex";
    static String afterFilename = "after.tex";
    // HashMap per InputStream
    static Map<String, String> dlMap = new HashMap<>();                     //creazione HashMap per contenere corrispondenze trovate di dl
    static Map<String, Map<String, String>> styleMap = new HashMap<>();     //creazione HashMap per contenere corrispondenze trovate di aspect_style
    static Map<String, String> layerMap = new HashMap<>();                  //creazione HashMap per contenere corrispondenze trovate di aspect_layer
    static Map<String, List<List<String>>> labelMap = new HashMap<>();      //creazione HashMap per contenere corrispondenze trovate di aspect_label
    static Map<String, List<List<String>>> imgLabelMap = new HashMap<>();   //creazione HashMap per contenere corrispondenze trovate di aspect_image_label
    // Costanti per DEBUG
    static final String ANSI_RESET = "\u001B[0m";
    static final String ANSI_CYAN = "\u001B[36m";
    static final String ANSI_GREEN = "\u001B[32m";
    static final String ANSI_YELLOW = "\u001B[33m";
    static final String ANSI_RED = "\u001B[31m";

    //definizione dell'input, argomenti in ingresso
    public TexPdfTh(InputStream is, String filename, boolean verbose, boolean debug, boolean merge, boolean free, String resizeFactor) {
        this.input = is;
        this.name = filename;
        this.verbose = verbose;
        this.merge = merge;
        this.free = free;
        this.resizeFactor = resizeFactor;

        TexPdfTh.debug = debug;
    }

    //gestione della conversione di una stringa in un intero:
    public static boolean isInteger(String str){
        try{
            Integer.parseInt(str);
            return true;
        } catch(Exception ex){
            return false;
        }
    }


    private static Map<String, Object> processCommand(String command) {

        if (debug) System.out.println(ANSI_CYAN + "[DEBUG] Comando ricevuto: " + command + ANSI_RESET);

        // identificatori, numeri, stringhe tra doppi apici con escape
        final String IDENT   = "[a-zA-Z_][a-zA-Z0-9_]*";
        final String NUMBER  = "\\d+";
        final String QUOTED  = "\"(?:[^\"\\\\]|\\\\.)*\""; // consente \" dentro stringa

        // argomenti "comodi" per funzioni: niente virgole di top-level; consente sotto-parentesi
        // (NON è ricorsiva infinita, ma copre bene casi comuni come text(yellow), text("a,b"), text(func(x)))
        final String FUNARG  = "(?:[^()\",]+|" + QUOTED + "|\\((?:[^()\"\\\\]|\\\\.|" + QUOTED + ")*\\))";

        // funzione: IDENT( ... ) con argomenti "comodi"
        final String FUNC    = IDENT + "\\(" + "(?:" + FUNARG + "(?:," + FUNARG + ")*)?" + "\\)";

        // un atomo può essere: numero | stringa | funzione | identificatore semplice
        final String ATOM    = "(?:" + NUMBER + "|" + QUOTED + "|" + FUNC + "|" + IDENT + ")";

        final String COORD        = "(\\d{1,3})";
        final String STRING_GROUP = "(" + ATOM + ")";
        final String STYLE_ITEM   = ATOM;
        final String STYLE_LIST   = "\\(" + STYLE_ITEM + "(?:," + STYLE_ITEM + ")*\\)";
        final String STYLE_GROUP  = "(?:,(" + STYLE_LIST + "|" + STYLE_ITEM + "))";
        final String FRAME_GROUP  = "(?:,(\\d+))";

        Pattern nodePattern = Pattern.compile(
                "aspect_(draw|image)node\\(" +
                        COORD + "," + COORD + "," +     // x, y
                        STRING_GROUP +                  // testo/img
                        STYLE_GROUP + "?" +             // stile opzionale
                        FRAME_GROUP + "?" +             // frame opzionale
                        "\\)"
        );
        Pattern linePattern = Pattern.compile(
                "aspect_line\\(" +
                        COORD + "," + COORD + "," + COORD + "," + COORD +   // x, y, x-offset, y-offset
                        STYLE_GROUP + "?" +                                 // stile opzionale
                        FRAME_GROUP + "?" +                                 // frame opzionale
                        "\\)"
        );
        Pattern arcPattern = Pattern.compile(
                "aspect_arc\\(" +
                        COORD + "," + COORD + "," + COORD + "," + COORD + "," + COORD +     // x, y, x-offset, y-offset, radius
                        STYLE_GROUP + "?" +                                                 // stile opzionale
                        FRAME_GROUP + "?" +                                                 // frame opzionale
                        "\\)"
        );
        Pattern arrowPattern = Pattern.compile(
                "aspect_arrow\\(" +
                        COORD + "," + COORD + "," + COORD + "," + COORD +   // x, y, x-offset, y-offset
                        STYLE_GROUP + "?" +                                 // stile opzionale
                        FRAME_GROUP + "?" +                                 // frame opzionale
                        "\\)"
        );
        Pattern rectanglePattern = Pattern.compile(
                "aspect_rectangle\\(" +
                        COORD + "," + COORD + "," + COORD + "," + COORD +   // x, y, x-offset, y-offset
                        STYLE_GROUP + "?" +                                 // stile opzionale
                        FRAME_GROUP + "?" +                                 // frame opzionale
                        "\\)"
        );
        Pattern trianglePattern = Pattern.compile(
                "aspect_triangle\\(" +
                        COORD + "," + COORD + "," + COORD + "," + COORD + "," + COORD + "," + COORD +   // x, y, z, x-offset, y-offset, z-offest
                        STYLE_GROUP + "?" +                                                             // stile opzionale
                        FRAME_GROUP + "?" +                                                             // frame opzionale
                        "\\)"
        );
        Pattern circlePattern = Pattern.compile(
                "aspect_circle\\(" +
                        COORD + "," + COORD + "," + COORD +   // x, y, radius
                        STYLE_GROUP + "?" +                   // stile opzionale
                        FRAME_GROUP + "?" +                   // frame opzionale
                        "\\)"
        );
        Pattern ellipsePattern = Pattern.compile(
                "aspect_ellipse\\(" +
                        COORD + "," + COORD + "," + COORD +   // x, y, radius
                        STYLE_GROUP + "?" +                   // stile opzionale
                        FRAME_GROUP + "?" +                   // frame opzionale
                        "\\)"
        );

        Matcher matcher;

        if ((matcher = nodePattern.matcher(command)).matches()) {
            if (debug) System.out.println(ANSI_CYAN + "[DEBUG] Match con pattern: nodePattern" + ANSI_RESET);
            return processNode(matcher);
        } else if ((matcher = linePattern.matcher(command)).matches()) {
            if (debug) System.out.println(ANSI_CYAN + "[DEBUG] Match con pattern: linePattern" + ANSI_RESET);
            return processLine(matcher);
        } else if ((matcher = arcPattern.matcher(command)).matches()) {
            if (debug) System.out.println(ANSI_CYAN + "[DEBUG] Match con pattern: arcPattern" + ANSI_RESET);
            return processArc(matcher);
        } else if ((matcher = arrowPattern.matcher(command)).matches()) {
            if (debug) System.out.println(ANSI_CYAN + "[DEBUG] Match con pattern: arrowPattern" + ANSI_RESET);
            return processArrow(matcher);
        } else if ((matcher = rectanglePattern.matcher(command)).matches()) {
            if (debug) System.out.println(ANSI_CYAN + "[DEBUG] Match con pattern: rectanglePattern" + ANSI_RESET);
            return processRectangle(matcher);
        } else if ((matcher = trianglePattern.matcher(command)).matches()) {
            if (debug) System.out.println(ANSI_CYAN + "[DEBUG] Match con pattern: trianglePattern" + ANSI_RESET);
            return processTriangle(matcher);
        } else if ((matcher = circlePattern.matcher(command)).matches()) {
            if (debug) System.out.println(ANSI_CYAN + "[DEBUG] Match con pattern: circlePattern" + ANSI_RESET);
            return processCircle(matcher);
        } else if ((matcher = ellipsePattern.matcher(command)).matches()) {
            if (debug) System.out.println(ANSI_CYAN + "[DEBUG] Match con pattern: ellipsePattern" + ANSI_RESET);
            return processEllipse(matcher);
        }
        if (debug) System.out.println(ANSI_RED + "[DEBUG] Nessun pattern ha fatto match per il comando: " + command + ANSI_RESET);
        return null;
    }

    private static Map<String, Object> processNode(Matcher matcher) {

        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1); // "draw" or "image"

        String x = matcher.group(2);
        String y = matcher.group(3);
        String argument1 = matcher.group(4).replaceAll("\"", ""); // text or image path
        String rawStyle = matcher.group(5);
        String t = matcher.group(6);

        if (debug) {
            System.out.println(ANSI_CYAN + "[DEBUG] Parametri estratti:");
            System.out.println("\t- x: " + x);
            System.out.println("\t- y: " + y);
            System.out.println("\t- argument1: " + argument1);
            System.out.println("\t- rawStyle: " + rawStyle);
            System.out.println("\t- t: " + t + ANSI_RESET);
        }

        String tikzAttribute = null;
        String layer = "0";

        if (rawStyle != null) {
            Map<String, Object> styleResult = resolveStyleAttributesAndFrame(rawStyle, t);
            if (styleResult.containsKey("tikzAttribute")) tikzAttribute = (String) styleResult.get("tikzAttribute");
            if (styleResult.containsKey("layer")) layer = (String) styleResult.get("layer");
            if (styleResult.containsKey("frame")) t = (String) styleResult.get("frame");
        }

        String content = null;
        switch (commandType) {
            case "draw":
                content = argument1; // Testo normale per drawnode
                break;
            case "image":
                String width = null;
                if (tikzAttribute != null) {
                    Matcher widthMatcher = Pattern.compile("(?:(?<=^)|(?<=,))\\s*width\\s*=\\s*(\\d+)\\b").matcher(tikzAttribute);
                    if (widthMatcher.find()) {
                        width = widthMatcher.group(1);
                        tikzAttribute = tikzAttribute.replaceAll("width=" + width + ",?", "");
                        tikzAttribute = tikzAttribute.replaceAll(",{2,}", ",").replaceAll("^,|,$", "");
                    }
                }
                content = (width != null)
                        ? String.format("\\includegraphics[width=%spt]{%s}", width, argument1)
                        : String.format("\\includegraphics{%s}", argument1);
                break;
        }

        String drawCommand = (tikzAttribute != null && !tikzAttribute.isEmpty())
                ? String.format("\\node [%s] at (%s,%s) {%s};", tikzAttribute, x, y, content)
                : String.format("\\node at (%s,%s) {%s};", x, y, content);
        result.put("tikzCommand", drawCommand);
        result.put("layer", layer);
        result.put("frame", t);
        return result;

    }

    private static Map<String, Object> processLine(Matcher matcher) {

        Map<String, Object> result = new HashMap<>();

        String x1 = matcher.group(1);
        String y1 = matcher.group(2);
        String x2 = matcher.group(3);
        String y2 = matcher.group(4);
        String rawStyle = matcher.group(5);
        String t =  matcher.group(6);

        if (debug) {
            System.out.println(ANSI_CYAN + "[DEBUG] Parametri estratti:");
            System.out.println("\t- x1: " + x1);
            System.out.println("\t- y1: " + y1);
            System.out.println("\t- x2: " + x2);
            System.out.println("\t- y2: " + y2);
            System.out.println("\t- rawStyle: " + rawStyle);
            System.out.println("\t- t: " + t + ANSI_RESET);
        }

        String tikzAttribute = null;
        String layer = "0";

        if (rawStyle != null) {
            Map<String, Object> styleResult = resolveStyleAttributesAndFrame(rawStyle, t);
            if (styleResult.containsKey("tikzAttribute")) tikzAttribute = (String) styleResult.get("tikzAttribute");
            if (styleResult.containsKey("layer")) layer = (String) styleResult.get("layer");
            if (styleResult.containsKey("frame")) t = (String) styleResult.get("frame");

            if (styleResult.containsKey("textLabels") || styleResult.containsKey("imageLabels")) {

                String position = String.format("($(%s,%s)!0.5!(%s,%s)$)", x1, y1, x2, y2);
                List<Map<String,Object>> labelsResult = resolveLabels(position, (List<List<String>>) styleResult.get("textLabels"), (List<List<String>>) styleResult.get("imageLabels"), t);
                result.put("labels", labelsResult);
            }
        }

        String drawCommand = (tikzAttribute != null && !tikzAttribute.isEmpty())
                ? String.format("\\draw [%s] (%s,%s) -- (%s,%s);", tikzAttribute, x1, y1, x2, y2)
                : String.format("\\draw (%s,%s) -- (%s,%s);", x1, y1, x2, y2);

        result.put("tikzCommand", drawCommand);
        result.put("layer", layer);
        result.put("frame", t);
        return result;

    }

    private static Map<String, Object> processArc(Matcher matcher) {

        Map<String, Object> result = new HashMap<>();

        String x1 = matcher.group(1);
        String y1 = matcher.group(2);
        String a1 = matcher.group(3);
        String a2 = matcher.group(4);
        String r =  matcher.group(5);
        String rawStyle = matcher.group(6);
        String t = matcher.group(7);

        if (debug) {
            System.out.println(ANSI_CYAN + "[DEBUG] Parametri estratti:");
            System.out.println("\t- x1: " + x1);
            System.out.println("\t- y1: " + y1);
            System.out.println("\t- a1: " + a1);
            System.out.println("\t- a2: " + a2);
            System.out.println("\t- r: " + r);
            System.out.println("\t- rawStyle: " + rawStyle);
            System.out.println("\t- t: " + t + ANSI_RESET);
        }

        String tikzAttribute = null;
        String layer = "0";

        if (rawStyle != null) {
            Map<String, Object> styleResult = resolveStyleAttributesAndFrame(rawStyle, t);
            if (styleResult.containsKey("tikzAttribute")) tikzAttribute = (String) styleResult.get("tikzAttribute");
            if (styleResult.containsKey("layer")) layer = (String) styleResult.get("layer");
            if (styleResult.containsKey("frame")) t = (String) styleResult.get("frame");

            if (styleResult.containsKey("textLabels") || styleResult.containsKey("imageLabels")) {

                String position = String.format("($(%s,%s)+({%s+%s)/2}:%s)$)", x1, y1, a1, a2, r);
                List<Map<String,Object>> labelsResult = resolveLabels(position, (List<List<String>>) styleResult.get("textLabels"), (List<List<String>>) styleResult.get("imageLabels"), t);
                result.put("labels", labelsResult);
            }
        }

        String drawCommand = (tikzAttribute != null && !tikzAttribute.isEmpty())
                ? String.format("\\draw [%s] (%s,%s) ++(%s:%s) arc (%s:%s:%s);", tikzAttribute, x1, y1, a1, r, a1, a2, r)
                : String.format("\\draw (%s,%s) ++(%s:%s) arc (%s:%s:%s);", x1, y1, a1, r, a1, a2, r);

        result.put("tikzCommand", drawCommand);
        result.put("layer", layer);
        result.put("frame", t);
        return result;

    }

    private static Map<String, Object> processArrow(Matcher matcher) {

        Map<String, Object> result = new HashMap<>();
        String x1 = matcher.group(1);
        String y1 = matcher.group(2);
        String x2 = matcher.group(3);
        String y2 = matcher.group(4);
        String rawStyle = matcher.group(5);
        String t = matcher.group(6);

        if (debug) {
            System.out.println(ANSI_CYAN + "[DEBUG] Parametri estratti:");
            System.out.println("\t- x1: " + x1);
            System.out.println("\t- y1: " + y1);
            System.out.println("\t- x2: " + x2);
            System.out.println("\t- y2: " + y2);
            System.out.println("\t- rawStyle: " + rawStyle);
            System.out.println("\t- t: " + t + ANSI_RESET);
        }

        String tikzAttribute = null;
        String layer = "0";

        if (rawStyle != null) {
            Map<String, Object> styleResult = resolveStyleAttributesAndFrame(rawStyle, t);
            if (styleResult.containsKey("tikzAttribute")) tikzAttribute = (String) styleResult.get("tikzAttribute");
            if (styleResult.containsKey("layer")) layer = (String) styleResult.get("layer");
            if (styleResult.containsKey("frame")) t = (String) styleResult.get("frame");

            if (styleResult.containsKey("textLabels") || styleResult.containsKey("imageLabels")) {

                String position = String.format("($(%s,%s)!0.5!(%s,%s)$)", x1, y1, x2, y2);
                List<Map<String,Object>> labelsResult = resolveLabels(position, (List<List<String>>) styleResult.get("textLabels"), (List<List<String>>) styleResult.get("imageLabels"), t);
                result.put("labels", labelsResult);
            }
        }

        String drawCommand = (tikzAttribute != null && !tikzAttribute.isEmpty())
                ? String.format("\\draw [->,%s] (%s,%s) -- (%s,%s);", tikzAttribute, x1, y1, x2, y2)
                : String.format("\\draw [->] (%s,%s) -- (%s,%s);", x1, y1, x2, y2);

        result.put("tikzCommand", drawCommand);
        result.put("layer", layer);
        result.put("frame", t);
        return result;

    }

    private static Map<String, Object> processRectangle(Matcher matcher) {

        Map<String, Object> result = new HashMap<>();

        String x1 = matcher.group(1);
        String y1 = matcher.group(2);
        String x2 = matcher.group(3);
        String y2 = matcher.group(4);
        String rawStyle = matcher.group(5);
        String t = matcher.group(6);

        if (debug) {
            System.out.println(ANSI_CYAN + "[DEBUG] Parametri estratti:");
            System.out.println("\t- x1: " + x1);
            System.out.println("\t- y1: " + y1);
            System.out.println("\t- x2: " + x2);
            System.out.println("\t- y2: " + y2);
            System.out.println("\t- rawStyle: " + rawStyle);
            System.out.println("\t- t: " + t + ANSI_RESET);
        }

        String tikzAttribute = null;
        String layer = "0";

        if (rawStyle != null) {
            Map<String, Object> styleResult = resolveStyleAttributesAndFrame(rawStyle, t);
            if (styleResult.containsKey("tikzAttribute")) tikzAttribute = (String) styleResult.get("tikzAttribute");
            if (styleResult.containsKey("layer")) layer = (String) styleResult.get("layer");
            if (styleResult.containsKey("frame")) t = (String) styleResult.get("frame");

            if (styleResult.containsKey("textLabels") || styleResult.containsKey("imageLabels")) {

                String position = String.format("($(%s,%s)!0.5!(%s,%s)$)", x1, y1, x2, y2);
                List<Map<String,Object>> labelsResult = resolveLabels(position, (List<List<String>>) styleResult.get("textLabels"), (List<List<String>>) styleResult.get("imageLabels"), t);
                result.put("labels", labelsResult);
            }
        }

        String drawCommand = (tikzAttribute != null && !tikzAttribute.isEmpty())
                ? String.format("\\draw [%s] (%s,%s) rectangle (%s,%s);", tikzAttribute, x1, y1, x2, y2)
                : String.format("\\draw (%s,%s) rectangle (%s,%s);", x1, y1, x2, y2);

        result.put("tikzCommand", drawCommand);
        result.put("layer", layer);
        result.put("frame", t);
        return result;

    }

    private static Map<String, Object> processTriangle(Matcher matcher) {

        Map<String, Object> result = new HashMap<>();

        String x1 = matcher.group(1);
        String y1 = matcher.group(2);
        String x2 = matcher.group(3);
        String y2 = matcher.group(4);
        String x3 = matcher.group(5);
        String y3 = matcher.group(6);
        String rawStyle = matcher.group(7);
        String t = matcher.group(8);

        if (debug) {
            System.out.println(ANSI_CYAN + "[DEBUG] Parametri estratti:");
            System.out.println("\t- x1: " + x1);
            System.out.println("\t- y1: " + y1);
            System.out.println("\t- x2: " + x2);
            System.out.println("\t- y2: " + y2);
            System.out.println("\t- x3: " + x3);
            System.out.println("\t- y3: " + y3);
            System.out.println("\t- rawStyle: " + rawStyle);
            System.out.println("\t- t: " + t + ANSI_RESET);
        }

        String tikzAttribute = null;
        String layer = "0";

        if (rawStyle != null) {
            Map<String, Object> styleResult = resolveStyleAttributesAndFrame(rawStyle, t);
            if (styleResult.containsKey("tikzAttribute")) tikzAttribute = (String) styleResult.get("tikzAttribute");
            if (styleResult.containsKey("layer")) layer = (String) styleResult.get("layer");
            if (styleResult.containsKey("frame")) t = (String) styleResult.get("frame");

            if (styleResult.containsKey("textLabels") || styleResult.containsKey("imageLabels")) {

                String position = String.format("($(%s,%s)!2/3!(0,0)+(%s,%s)!2/3!(0,0)+(%s,%s)!2/3!(0,0)$)", x1, y1, x2, y2, x3, y3);
                List<Map<String,Object>> labelsResult = resolveLabels(position, (List<List<String>>) styleResult.get("textLabels"), (List<List<String>>) styleResult.get("imageLabels"), t);
                result.put("labels", labelsResult);
            }
        }

        String drawCommand = (tikzAttribute != null && !tikzAttribute.isEmpty())
                ? String.format("\\draw [%s] (%s,%s) -- (%s,%s) -- (%s,%s) -- cycle;", tikzAttribute, x1, y1, x2, y2, x3, y3)
                : String.format("\\draw (%s,%s) -- (%s,%s) -- (%s,%s) -- cycle;", x1, y1, x2, y2, x3, y3);

        result.put("tikzCommand", drawCommand);
        result.put("layer", layer);
        result.put("frame", t);
        return result;

    }

    private static Map<String, Object> processCircle(Matcher matcher) {

        Map<String, Object> result = new HashMap<>();

        String x1 = matcher.group(1);
        String y1 = matcher.group(2);
        String r = matcher.group(3);
        String rawStyle = matcher.group(4);
        String t = matcher.group(5);

        if (debug) {
            System.out.println(ANSI_CYAN + "[DEBUG] Parametri estratti:");
            System.out.println("\t- x1: " + x1);
            System.out.println("\t- y1: " + y1);
            System.out.println("\t- r: " + r);
            System.out.println("\t- rawStyle: " + rawStyle);
            System.out.println("\t- t: " + t + ANSI_RESET);
        }

        String tikzAttribute = null;
        String layer = "0";

        if (rawStyle != null) {
            Map<String, Object> styleResult = resolveStyleAttributesAndFrame(rawStyle, t);
            if (styleResult.containsKey("tikzAttribute")) tikzAttribute = (String) styleResult.get("tikzAttribute");
            if (styleResult.containsKey("layer")) layer = (String) styleResult.get("layer");
            if (styleResult.containsKey("frame")) t = (String) styleResult.get("frame");

            if (styleResult.containsKey("textLabels") || styleResult.containsKey("imageLabels")) {

                String position = String.format("(%s,%s)", x1, y1);
                List<Map<String,Object>> labelsResult = resolveLabels(position, (List<List<String>>) styleResult.get("textLabels"), (List<List<String>>) styleResult.get("imageLabels"), t);
                result.put("labels", labelsResult);
            }
        }

        String drawCommand = (tikzAttribute != null && !tikzAttribute.isEmpty())
                ? String.format("\\draw [%s] (%s,%s) circle (%s);", tikzAttribute, x1, y1, r)
                : String.format("\\draw (%s,%s) circle (%s)", x1, y1, r);

        result.put("tikzCommand", drawCommand);
        result.put("layer", layer);
        result.put("frame", t);
        return result;

    }

    private static Map<String, Object> processEllipse(Matcher matcher) {

        Map<String, Object> result = new HashMap<>();

        String x1 = matcher.group(1);
        String y1 = matcher.group(2);
        String r1 = matcher.group(3);
        String r2 = matcher.group(4);
        String rawStyle = matcher.group(5);
        String t = matcher.group(6);

        if (debug) {
            System.out.println(ANSI_CYAN + "[DEBUG] Parametri estratti:");
            System.out.println("\t- x1: " + x1);
            System.out.println("\t- y1: " + y1);
            System.out.println("\t- r1: " + r1);
            System.out.println("\t- r2: " + r2);
            System.out.println("\t- rawStyle: " + rawStyle);
            System.out.println("\t- t: " + t + ANSI_RESET);
        }

        String tikzAttribute = null;
        String layer = "0";

        if (rawStyle != null) {
            Map<String, Object> styleResult = resolveStyleAttributesAndFrame(rawStyle, t);
            if (styleResult.containsKey("tikzAttribute")) tikzAttribute = (String) styleResult.get("tikzAttribute");
            if (styleResult.containsKey("layer")) layer = (String) styleResult.get("layer");
            if (styleResult.containsKey("frame")) t = (String) styleResult.get("frame");

            if (styleResult.containsKey("textLabels") || styleResult.containsKey("imageLabels")) {

                String position = String.format("($(%s,%s)!0.5!(%s,%s)$)", x1, y1);
                List<Map<String,Object>> labelsResult = resolveLabels(position, (List<List<String>>) styleResult.get("textLabels"), (List<List<String>>) styleResult.get("imageLabels"), t);
                result.put("labels", labelsResult);
            }
        }

        String drawCommand = (tikzAttribute != null && !tikzAttribute.isEmpty())
                ? String.format("\\draw [%s] (%s,%s) ellipse (%s and %s);", tikzAttribute, x1, y1, r1, r2)
                : String.format("\\draw (%s,%s) ellipse (%s and %s);", x1, y1, r1, r2);

        result.put("tikzCommand", drawCommand);
        result.put("layer", layer);
        result.put("frame", t);
        return result;

    }

    private static Map<String, Object> processCommandGraph(String command) {

        Pattern nodePattern = Pattern.compile("aspect_graph(draw|color)node\\(([^,]+)(?:,(\\w+|\"[^\"]+\"))?(?:,([^,]+))?(?:,(\\d+))?\\)");
        Pattern edgePattern = Pattern.compile("aspect_graph(draw|quote)line\\(([^,]+),([^,]+)(?:,([^,]+))?(?:,(\\d+))?\\)");
        Pattern arrowPattern = Pattern.compile("aspect_graph(draw|quote)arrow\\(([^,]+),([^,]+)(?:,([^,]+))?(?:,(\\d+))?\\)");

        Matcher matcher;

        if ((matcher = nodePattern.matcher(command)).matches()) {
            return processGraphNode(matcher);
        } else if ((matcher = edgePattern.matcher(command)).matches()) {
            return processGraphEdge(matcher);
        } else if ((matcher = arrowPattern.matcher(command)).matches()) {
            return processGraphArrow(matcher);
        }
        return null;
    }

    private static Map<String, Object> processGraphNode(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "color"
        String name = matcher.group(2);
        String arg1 = matcher.group(3);
        String arg2 = matcher.group(4);
        String t = matcher.group(5);
        if (commandType.equals("draw")) {
            t = matcher.group(4);
            if (arg1 != null)
                result.put("tikzCommand",
                        String.format("{%s[minimum size=7mm, %s, draw]},", name, arg1));
            else
                result.put("tikzCommand",
                        String.format("{%s[minimum size=7mm, draw]},", name));
        }
        else if (commandType.equals("color") && arg1 != null){
            arg1 = arg1.replace("\"", "");
            if (arg2 != null)
                result.put("tikzCommand",
                        String.format("{%s[fill=%s, minimum size=7mm, %s, draw]},", name, arg1, arg2));
            else
                result.put("tikzCommand",
                        String.format("{%s[fill=%s, minimum size=7mm, draw]},", name, arg1));
        }
        result.put("frame", t);
        return result;
    }

    private static Map<String, Object> processGraphEdge(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "quote"
        String argA = matcher.group(2);
        String argB = matcher.group(3);
        String text = matcher.group(4);
        String t = matcher.group(5);
        if (commandType.equals("draw")) {
            t = matcher.group(4);
            result.put("tikzCommand",
                    String.format("{%s -- %s},", argA, argB));
        }
        else if (commandType.equals("quote") && text != null){
            text = text.replace("\"", "");
            result.put("tikzCommand",
                    String.format("{%s --[\"%s\"] %s},", argA, text, argB));
        }
        result.put("frame", t);
        return result;
    }

    private static Map<String, Object> processGraphArrow(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "quote"
        String argA = matcher.group(2);
        String argB = matcher.group(3);
        String text = matcher.group(4);
        String t = matcher.group(5);
        if (commandType.equals("draw")) {
            t = matcher.group(4);
            result.put("tikzCommand",
                    String.format("{%s -> %s},", argA, argB));
        }
        else if (commandType.equals("quote") && text != null){
            text = text.replace("\"", "");
            result.put("tikzCommand",
                    String.format("{%s ->[\"%s\"] %s},", argA, text, argB));
        }
        result.put("frame", t);
        return result;
    }

    public static class NumericStringComparator implements Comparator<String> {

        @Override
        public int compare(String str1, String str2) {
            if(str1 == null && str2 == null) return 0;
            if(str1 == null) return -1;
            if(str2 == null) return 1;
            int[] nums1 = extractNumbers(str1);
            int[] nums2 = extractNumbers(str2);
            int result = Integer.compare(nums1[0], nums2[0]);
            if (result == 0) {
                result = Integer.compare(nums1[1], nums2[1]);
            }
            return result;
        }

        private int[] extractNumbers(String str) {
            int[] nums = new int[2];
            str = str.replaceAll("\\s", "");
            if (str.startsWith("<") && str.endsWith(">")) {
                str = str.substring(1, str.length() - 1);
                String[] parts = str.split("-");
                nums[0] = Integer.parseInt(parts[0]);
                nums[1] = (parts.length > 1) ? Integer.parseInt(parts[1]) : nums[0];
            } else {
                nums[0] = Integer.parseInt(str);
                nums[1] = nums[0];
            }
            return nums;
        }
    }

    private static TreeMap<String, List<String>> mergeAdjacentIntervals(TreeMap<String, List<String>> inputTreeMap) {
        TreeMap<String, List<String>> resultTreeMap = new TreeMap<>(new NumericStringComparator());

        for (Map.Entry<String, List<String>> entry1 : inputTreeMap.entrySet()) {

            String key1 = entry1.getKey();
            List<String> values1 = entry1.getValue();

            if (key1 == null) resultTreeMap.put(null, new ArrayList<>(values1));
            else {
                List<String> remaining = new ArrayList<>(values1);

                if (inputTreeMap.higherKey(key1) != null) {

                    String key2 = null;

                    for (Map.Entry<String, List<String>> entry2 : inputTreeMap.tailMap(inputTreeMap.higherKey(key1)).entrySet()) {

                        if (remaining.isEmpty()) break;

                        key2 = entry2.getKey();
                        List<String> values2 = entry2.getValue();
                        List<String> diff = new ArrayList<>(remaining);

                        diff.removeAll(values2);
                        remaining.removeAll(diff);
                        values2.removeAll(remaining);

                        if (!diff.isEmpty()) {
                            int key1Int = Integer.parseInt(key1);
                            int key2Int = Integer.parseInt(key2) - 1;
                            String newKey = (key1Int != key2Int) ? "<" + key1 + "-" + key2Int + ">" : "<" + key2Int + ">";
                            resultTreeMap.put(newKey, diff);
                        }
                    }

                    if (!remaining.isEmpty()) {
                        resultTreeMap.put("<" + key1 + "-" + key2 + ">", remaining);
                    }

                }
                else {

                    if (!values1.isEmpty()) {
                        resultTreeMap.put("<" + key1 + ">", values1);
                    }

                }
            }
        }

        return resultTreeMap;
    }

    // -----------------------------------------------------------------------------------------------
    // Compiling: pdflatex, lualatex
    // -----------------------------------------------------------------------------------------------
    public static void buildLatex(String filename) {

        if (!pdfBuild) return;
        try {
            String ls = System.lineSeparator();
            ProcessBuilder processBuilderPDF = new ProcessBuilder();

            System.out.println(ls + "+++> ASPECT: Preparing files for building...");
            Thread.sleep(graph ? 5000 : 2000);

            if (graph) {
                processBuilderPDF.command("lualatex", "-halt-on-error --shell-escape", filename);
            } else {
                processBuilderPDF.command("pdflatex", "-halt-on-error --shell-escape", filename);
            }

            Process pdf = processBuilderPDF.start();
            String pdfname = filename.substring(0, filename.lastIndexOf('.')) + ".pdf";
            System.out.println("+++> ASPECT: Building " + pdfname);

            // Inizio estensione buildLatex
            BufferedReader errread = new BufferedReader(new InputStreamReader(pdf.getInputStream()));
            List<String> output = new ArrayList<>();
            String line;

            while ((line = errread.readLine()) != null) {
                output.add(line);
            }
            // Fine estensione buildLatex
            pdf.waitFor();

            if (pdf.exitValue() != 0) { // Modifica al blocco di errore (Il bufferedReader ora è fuori)
                System.err.println("***> ASPECT ERROR: PDF build failed!");
                for (String l : output) System.err.println(l); // stampa tutto l'output
                System.exit(1);
            }
            System.out.println("+++> ASPECT: File created " + pdfname + ls);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static List<Map<String,Object>> resolveLabels(String pos, List<List<String>> textLabels, List<List<String>> imageLabels, String frame) {

        List<Map<String,Object>> labelCommands = new ArrayList<>();

        // Genero un comando Stringa per ogni label

        if (textLabels != null) {
            for (List<String> label : textLabels) {
                String text = label.get(0).replace("\"", "");
                String rawStyle = label.get(1);

                String tikzAttribute = null;
                String layer = "0";

                if (rawStyle != null) {
                    ArrayList<String> styles = parseStyleParameter(rawStyle); // Pulisco i valori da "" e ()
                    Map<String, Object> styleResult = extractTikzAttributes(styles);

                    if (styleResult.containsKey("tikzAttribute")) tikzAttribute = (String) styleResult.get("tikzAttribute");
                    if (styleResult.containsKey("layer")) layer = (String) styleResult.get("layer");
                }

                String drawCommand = (tikzAttribute != null && !tikzAttribute.isEmpty())
                        ? String.format("\\node [%s] at %s {%s};", tikzAttribute, pos, text)
                        : String.format("\\node at %s {%s};", pos, text);

                Map<String,Object> result =  new HashMap<>();
                result.put("tikzCommand", drawCommand);
                result.put("layer", layer);
                result.put("frame", frame);

                labelCommands.add(result);

            }
        }

        if (imageLabels != null) {
            for (List<String> label : imageLabels) {
                String url = label.get(0);
                String rawStyle = label.get(1);

                String tikzAttribute = null;
                String layer = "0";

                if (rawStyle != null) {
                    ArrayList<String> styles = parseStyleParameter(rawStyle); // Pulisco i valori da "" e ()
                    Map<String, Object> styleResult = extractTikzAttributes(styles);

                    if (styleResult.containsKey("tikzAttribute")) tikzAttribute = (String) styleResult.get("tikzAttribute");
                    if (styleResult.containsKey("layer")) layer = (String) styleResult.get("layer");
                }

                String width = null;
                if (tikzAttribute != null) {
                    Matcher widthMatcher = Pattern.compile("(?:(?<=^)|(?<=,))\\s*width\\s*=\\s*(\\d+)\\b").matcher(tikzAttribute);
                    if (widthMatcher.find()) {
                        width = widthMatcher.group(1);
                        tikzAttribute = tikzAttribute.replaceAll("width=" + width + ",?", "");
                        tikzAttribute = tikzAttribute.replaceAll(",{2,}", ",").replaceAll("^,|,$", "");
                    }
                }
                String content = (width != null)
                        ? String.format("\\includegraphics[width=%spt]{%s}", width, url)
                        : String.format("\\includegraphics{%s}", url);

                String drawCommand = (tikzAttribute != null && !tikzAttribute.isEmpty())
                        ? String.format("\\node [%s] at %s {%s};", tikzAttribute, pos, content)
                        : String.format("\\node at %s {%s};", pos, content);

                Map<String,Object> result =  new HashMap<>();
                result.put("tikzCommand", drawCommand);
                result.put("layer", layer);
                result.put("frame", frame);

                labelCommands.add(result);
            }
        }

        return labelCommands;
    }
    private static Map<String, Object> resolveStyleAttributesAndFrame(String rawStyle, String t) {
        Map<String, Object> result = new HashMap<>();
        ArrayList<String> styles = parseStyleParameter(rawStyle); // Pulisco i valori da "" e ()
        Map<String, Object> styleMap = extractTikzAttributes(styles);

        if (!styleMap.isEmpty()) {
            result.put("tikzAttribute", styleMap.get("tikzAttribute"));
            result.put("layer", styleMap.get("layer"));
            result.put("textLabels", styleMap.get("textLabels"));
            result.put("imageLabels", styleMap.get("imageLabels"));
        } else if (t == null) {
            if (styles.size() == 1 && isInteger(styles.get(0))) {
                result.put("frame", styles.get(0));
            }
        }

        if (debug) {
            System.out.println(ANSI_CYAN + "[DEBUG] Attributi stile risolti: ");
            System.out.println("\t- attributes=" + styleMap.get("tikzAttribute"));
            System.out.println("\t- layer=" + styleMap.get("layer"));
            System.out.println("\t- textLabels=" + styleMap.get("textLabels"));
            System.out.println("\t- imageLabels=" + styleMap.get("imageLabels"));
            System.out.println("\t- frame=" + t + ANSI_RESET);
        }

        return result;
    }
    private static Map<String, Object> extractTikzAttributes(ArrayList<String> styles) {
        Map<String, Object> result = new HashMap<>();
        StringJoiner joinAttr = new StringJoiner(",");
        List<List<String>> foundTextLabels = new ArrayList<>();
        List<List<String>> foundImageLabels = new ArrayList<>();
        boolean gotLayer = false;

        for (String style : styles) {
            Map<String, String> attributes = styleMap.get(style);
            if (attributes != null) {
                for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                    String key = attribute.getKey();
                    String value = attribute.getValue().replace("\\\\", "\\");
                    if (value.isEmpty()) joinAttr.add(key);
                    else joinAttr.add(key + "=" + value);
                }
            }
            if (!gotLayer) { // Un solo layer per comando
                String layer = layerMap.get(style);
                if (layer != null) {
                    result.put("layer", layer);
                    gotLayer = true;
                }
            }
            List<List<String>> textLabels = labelMap.get(style);
            if (textLabels != null) {
                for (List<String> label : textLabels) {
                    foundTextLabels.add(label);
                }
            }
            List<List<String>> imageLabels = imgLabelMap.get(style);
            if (imageLabels != null) {
                for (List<String> label : imageLabels) {
                    foundImageLabels.add(label);
                }
            }
        }
        String tikzAttribute = joinAttr.toString();
        if (!tikzAttribute.isEmpty()) result.put("tikzAttribute", tikzAttribute);
        if (!foundTextLabels.isEmpty()) result.put("textLabels", foundTextLabels);
        if (!foundImageLabels.isEmpty()) result.put("imageLabels", foundImageLabels);

        return result; // Restituisce una mappa popolata o empty, mai null.
    }
    private static ArrayList<String> parseStyleParameter(String raw) {
        ArrayList<String> result = new ArrayList<>();
        raw = raw.replace("\"", "");
        if (raw.startsWith("(") && raw.endsWith(")")) {
            // Caso Lista di stili tra virgolette o meno
            String inside = raw.substring(1, raw.length() - 1);
            for (String s : splitParameters(inside)) {
                result.add(s);
            }
        } else {
            result.add(raw);
        }
        return result;
    }
    private static ArrayList<String> splitParameters(String parameters) {
        StringBuilder sb = new StringBuilder();//costruzione di un oggetto di tipo StringBuilder per manipolare la stringa contenente i parametri
        int parenthesesCount = 0;//contatore delle parentesi
        boolean inQuotes = false;//booleano per sapere se mi trovo o meno dentro le doppie virgolette
        char delimiter = ',';//sono le virgole a separare i parametri
        ArrayList<String> paramList = new ArrayList<>();

        for (char ch : parameters.toCharArray()) {//toCharArray converte stringa in array di char
            // Ho riscritto gli if per avere una versione più compatta e chiara
            if (ch == '(' && !inQuotes) parenthesesCount++;
            else if (ch == ')' && !inQuotes) parenthesesCount--;
            else if (ch == '"') inQuotes = !inQuotes;

            if (ch == delimiter && parenthesesCount == 0 && !inQuotes) {
                paramList.add(sb.toString());//aggiungo il parametro all'ArrayList
                sb.setLength(0); //resetto lo StringBuilder
            } else {
                sb.append(ch);//aggiungo il carattere allo StringBuilder
            }

        }

        paramList.add(sb.toString());//l'ultimo sb non è seguito da una virgola

        if(paramList.size() == 1 && paramList.get(0).trim().isEmpty()) System.err.println("Error: no parameters found!");

        return paramList;
    }

    private static int findMatchingParen(String s, int openIdx) {
        int depth = 0;
        boolean inSingle = false, inDouble = false;
        for (int i = openIdx; i < s.length(); i++) {
            char c = s.charAt(i);

            // gestisci escape in stringhe
            if ((inSingle || inDouble) && c == '\\') {
                i++; // salta il prossimo carattere
                continue;
            }
            if (!inDouble && c == '\'' ) inSingle = !inSingle;
            else if (!inSingle && c == '"' ) inDouble = !inDouble;

            if (inSingle || inDouble) continue;

            if (c == '(') depth++;
            else if (c == ')') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1; // non trovata
    }

    public void run() {
        try {

            String ls = System.lineSeparator();
            System.out.println("+++> ASPECT: Reading from stdin..." + ls);

            StringBuilder before = new StringBuilder();
            StringBuilder after = new StringBuilder();
            String line;

            if (free) {

                BufferedReader bereader = new BufferedReader(new FileReader(beforeFilename));

                while ((line = bereader.readLine()) != null) {
                    before.append(line);
                    before.append(ls);
                }

                BufferedReader afreader = new BufferedReader(new FileReader(afterFilename));

                while ((line = afreader.readLine()) != null) {
                    after.append(line);
                    after.append(ls);
                }

            }

            Scanner scanner = new Scanner(input); //input è un oggetto di tipo InputStream
            boolean isValidInput = false;

            while (scanner.hasNextLine()) {
                String inputAtomsString = scanner.nextLine();//nextLine legge una riga alla volta

                if (verbose) System.out.println(ANSI_GREEN + "[ASPECT][INPUT] Riga letta: " + inputAtomsString + ANSI_RESET);
                if (verbose) System.out.println(inputAtomsString);

                TreeMap<String, List<String>> frameCommandMap = new TreeMap<>(new NumericStringComparator());

                Pattern pattern = Pattern.compile("(dl|aspect_style|aspect_label|aspect_image_label|aspect_\\w+)\\(");
                Matcher matcher = pattern.matcher(inputAtomsString);//creazione dell'oggetto matcher per cercare in inputAtomsString il pattern specificato

                ArrayList<String> matches = new ArrayList<>();      //creazione ArrayList per contenere corrispondenze trovate di aspect_draw*

                while (matcher.find()) {
                    String commandName = matcher.group(1);
                    int openIdx = matcher.end() - 1; // posizione della '(' trovata
                    int closeIdx = findMatchingParen(inputAtomsString, openIdx);
                    if (closeIdx == -1) {
                        System.err.println("Error: parentesi non bilanciate per comando: " + commandName);
                        continue;
                    }

                    String parameters = inputAtomsString.substring(openIdx + 1, closeIdx);
                    ArrayList<String> paramList = splitParameters(parameters); // vedi nuova versione sotto
                    String[] parArray = paramList.toArray(new String[0]);

                    // Switch case per popolare le mappe prima di gestire i comandi
                    switch (commandName) {
                        case "dl":
                            if (parArray.length == 2) {
                                String name = parArray[0].replace("\"", "");
                                String value = parArray[1].replace("\"", "");
                                dlMap.put(name, value);

                                if (debug) System.out.println(ANSI_CYAN + "[DEBUG] dlMap aggiornato: " + name + " = " + value + ANSI_RESET);

                            } else {
                                if (parArray.length > 2) System.err.println("Error: command dl has too many parameters!");
                                    else System.err.println("Error: command dl has too few parameters!");
                            }
                            break;
                        case "aspect_style":
                            if (parArray.length == 3) {
                                String name = parArray[0].replace("\"", "");
                                String attribute = parArray[1].replace("\"","");
                                String value = parArray[2].replace("\"", "");

                                // recupera la mappa interna oppure creane una nuova
                                Map<String, String> attributes = styleMap.getOrDefault(name, new HashMap<>());
                                attributes.put(attribute, value); // Così facendo non posso avere due volte lo stesso attributo
                                styleMap.put(name, attributes); // aggiorna lo stile

                                if (debug) System.out.println(ANSI_CYAN + "[DEBUG] styleMap aggiornato: " + name + " → " + attribute + "=" + value + ANSI_RESET);

                            } else {
                                if (parArray.length > 3) System.err.println("Error: command aspect_style has too many parameters!");
                                else System.err.println("Error: command aspect_style has too few parameters!");
                            }
                            break;
                        case "aspect_layer":
                            if (parArray.length == 2) {
                                String name = parArray[0].replace("\"", "");
                                String value = parArray[1].replace("\"", "");
                                layerMap.put(name, value);

                                if (debug) System.out.println(ANSI_CYAN + "[DEBUG] layerMap aggiornato: " + name + " → " + value + ANSI_RESET);

                            } else {
                                if (parArray.length > 2) System.err.println("Error: command aspect_layer has too many parameters!");
                                else System.err.println("Error: command aspect_layer has too few parameters!");
                            }
                            break;
                        case "aspect_label":
                            if (parArray.length == 3) {
                                String name = parArray[0].replace("\"", "");
                                String text = parArray[1];
                                String styles = parArray[2];
                                List<String> values = Arrays.asList(text, styles);

                                labelMap.computeIfAbsent(name, k -> new ArrayList<>()).add(values);

                                if (debug) System.out.println(ANSI_CYAN + "[DEBUG] labelMap aggiornato: " + name + " → " + text + " , " + styles + ANSI_RESET);
                            } else {
                                if (parArray.length > 3) System.err.println("Error: command aspect_label has too many parameters!");
                                else System.err.println("Error: command aspect_label has too few parameters!");
                            }
                            break;
                        case "aspect_image_label":
                            if (parArray.length == 3) {
                                String name = parArray[0].replace("\"", "");
                                String url = parArray[1];
                                String styles = parArray[2];
                                List<String> values = Arrays.asList(url, styles);

                                imgLabelMap.computeIfAbsent(name, k -> new ArrayList<>()).add(values);

                                if (debug) System.out.println(ANSI_CYAN + "[DEBUG] imgLabelMap aggiornato: " + name + " → " + url + " , " + styles + ANSI_RESET);
                            } else {
                                if (parArray.length > 3) System.err.println("Error: command aspect_image_label has too many parameters!");
                                else System.err.println("Error: command aspect_image_label has too few parameters!");
                            }
                            break;
                        default:
                            if (commandName.startsWith("aspect_")) {
                                String match = commandName + "(" + parameters + ")";

                                matches.add(match); //la aggiungo all'ArrayList contenente gli atomi aspect_* della riga
                            }
                    }
                }

                // Converto i parametri che hanno una corrispondenza in dlMap
                for (ListIterator<String> it = matches.listIterator(); it.hasNext(); ) {
                    boolean changed = false;
                    String match = it.next();

                    Matcher m2 = pattern.matcher(match);
                    if (m2.find()) {
                        String commandName = m2.group(1);
                        int openIdx = m2.end() - 1;
                        int closeIdx = findMatchingParen(match, openIdx);
                        if (closeIdx == -1) continue;

                        String parameters = match.substring(openIdx + 1, closeIdx);

                        ArrayList<String> paramList = splitParameters(parameters);
                        String[] parArray = paramList.toArray(new String[0]);

                        for (int i = 0; i < parArray.length; i++) {
                            if (!isInteger(parArray[i]) && dlMap.containsKey(parArray[i])) {
                                parArray[i] = dlMap.get(parArray[i]);
                                changed = true;
                            }
                        }

                        if (changed) {
                            StringBuilder newMatch = new StringBuilder(commandName).append("(");
                            for (int i = 0; i < parArray.length; i++) {
                                if (i > 0) newMatch.append(",");
                                newMatch.append(parArray[i]);
                            }
                            newMatch.append(")");
                            it.set(newMatch.toString());

                            if (debug) {
                                System.out.println(ANSI_CYAN + "[DEBUG] Sostituzione dlMap: ");
                                System.out.println("\tCommand: " + match + " --> " + newMatch + ANSI_RESET);
                            }
                        }
                    }
                }

                if (matches.isEmpty()) continue; //se l'ArrayList è vuoto, torno al while
                isValidInput = true;
                String[] inputAtomsArray = matches.toArray(new String[0]); //conversione dell'ArrayList in un array di stringhe
                // order is useful in graph mode so when I print multiple
                // solutions of the same problem the layout is the same
                Arrays.sort(inputAtomsArray);

                boolean graphCommandIsPresent = false;
                boolean standardCommandIsPresent = false;
                pattern = Pattern.compile("aspect_(graphdrawnode|graphcolornode|" +
                        "graphdrawline|graphquoteline|" +
                        "graphdrawarrow|graphquotearrow)\\(.*\\)"); //pattern di graph command
                for (String s : inputAtomsArray) {//s è il valore della cella (dell'array) che si sta considerando (e non l'indice)
                    matcher = pattern.matcher(s);
                    if (matcher.matches()) graphCommandIsPresent = true;
                    else standardCommandIsPresent = true; //se il comando nella stringa non è grafico, allora è standard
                }

                if (graphCommandIsPresent && standardCommandIsPresent) {//gestione caso in cui sono presenti sia comandi grafici che comandi standard
                    System.err.println("***> ASPECT ERROR: Incompatible Command Modes ");
                    System.err.println("     You are attempting to use standard mode  " +
                            "     commands and graph mode commands simultaneously," +
                            "     which is not supported. Please choose either" +
                            "     standard mode or graph mode for your current operation.");
                }

                graph = graphCommandIsPresent;

                // -----------------------------------------------------------------------------------------------
                // Graph mode - Input
                // -----------------------------------------------------------------------------------------------
                if (graph) {
                    ArrayList<String> graphEdges = new ArrayList<>();
                    for (String atom : inputAtomsArray) {
                        if (atom.contains("node")) {
                            Map<String, Object> result = processCommandGraph(atom);
                            if (result != null) {
                                String tikzCommand = (String) result.get("tikzCommand");
                                String frame = (String) result.get("frame");
                                frameCommandMap.computeIfAbsent(frame, k -> new LinkedList<>()).add(tikzCommand);
                            }
                            else {
                                System.err.println("***> ASPECT ERROR: command not found: " + atom + ls);
                                System.err.println("     Remember that numerical values (coordinates) " + ls +
                                        "     must be within the range 000-999 as required " + ls +
                                        "     also by the TikZ language" + ls);
                            }
                        } else if (atom.contains("line") || atom.contains("arrow")) {
                            graphEdges.add(atom);
                        }
                    }
                    for (String atom : graphEdges) {
                        Map<String, Object> result = processCommandGraph(atom);
                        if (result != null) {
                            String tikzCommand = (String) result.get("tikzCommand");
                            String frame = (String) result.get("frame");
                            frameCommandMap.computeIfAbsent(frame, k -> new LinkedList<>()).add(tikzCommand);
                        }
                        else {
                            System.err.println("***> ASPECT ERROR: command not found: " + atom + ls);
                            System.err.println("     Remember that numerical values (coordinates) " + ls +
                                    "     must be within the range 000-999 as required " + ls +
                                    "     also by the TikZ language" + ls);
                        }
                    }
                }
                // -----------------------------------------------------------------------------------------------
                // Standard mode - Input
                // -----------------------------------------------------------------------------------------------
                else {
                    TreeMap<String, TreeMap<String, List<String>>> auxMap = new TreeMap<>(new NumericStringComparator());

                    for (String atom : inputAtomsArray) {

                        Map<String, Object> result = processCommand(atom);

                        if (result != null) {
                            String tikzCommand = (String) result.get("tikzCommand");
                            String frame = (String) result.get("frame");
                            String layer = (String) result.get("layer");

                            if (debug) System.out.println(ANSI_CYAN + "[DEBUG] Comando processato: " + tikzCommand + ", frame=" + frame + ", layer=" + layer + ANSI_RESET);

                            //dato un frame di auxMap, accedo al layer e aggiungo il comando alla lista corrispondente (se non esiste la creo)
                            auxMap
                                    .computeIfAbsent(frame, k -> new TreeMap<>(new NumericStringComparator()))
                                    .computeIfAbsent(layer, k -> new LinkedList<>())
                                    .add(tikzCommand);

                            // Gestisco eventuali label generate
                            List<Map<String,Object>> labels = (List<Map<String, Object>>) result.get("labels");
                            if (labels != null) {
                                for (Map<String,Object> label : labels) {
                                    if (debug) System.out.println(ANSI_CYAN + "[DEBUG] Label processato: " + label.get("tikzCommand") + ", frame=" + label.get("frame") + ", layer=" + label.get("layer") + ANSI_RESET);
                                    String labelTikzCommand = (String) label.get("tikzCommand");
                                    String labelFrame = (String) label.get("frame");
                                    String labelLayer = (String) label.get("layer");

                                    auxMap
                                            .computeIfAbsent(labelFrame, k -> new TreeMap<>(new NumericStringComparator()))
                                            .computeIfAbsent(labelLayer, k -> new LinkedList<>())
                                            .add(labelTikzCommand);
                                }
                            }

                        } else {
                            System.err.println("***> ASPECT ERROR: command not found: " + atom + ls);
                            System.err.println("     Remember that numerical values (coordinates) " + ls +
                                    "     must be within the range 000-999 as required " + ls +
                                    "     also by the TikZ language" + ls);
                        }
                    }

                    //ora i comandi nella lista sono già raggruppati per layer
                    for(Map.Entry<String, TreeMap<String, List<String>>> entry : auxMap.entrySet()) {
                        String frame = entry.getKey();
                        TreeMap<String, List<String>> innerMap = entry.getValue(); //albero annidato (layer, lista)

                        for(Map.Entry<String, List<String>> innerEntry : innerMap.entrySet()) {
                            //String layer = innerEntry.getKey();
                            List<String> innerList = innerEntry.getValue();

                            for (String tikzCommand : innerList){
                                frameCommandMap.computeIfAbsent(frame, k -> new LinkedList<>()).add(tikzCommand);
                            }
                        }
                    }

                } //fine caso comando standard

                boolean useOverlay = frameCommandMap.size() > 1;

                // -----------------------------------------------------------------------------------------------
                // Single File w/ Beamer Overlays
                // -----------------------------------------------------------------------------------------------
                if (merge && useOverlay && !graph) {
                    int fileId = files.isEmpty() ? 0 : files.lastKey();
                    fileId +=1 ;
                    String filename = name + "_" + fileId + ".tex";

                    if (debug) System.out.println(ANSI_CYAN + "[DEBUG] Scrittura file: " + filename + ANSI_RESET);

                    File file = new File(filename);
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

                    if (free) {
                        out.println(before + ls);
                        out.println("\\begin{tikzpicture}");
                    }
                    else {
                        out.println("\\begin{frame}" + ls
                                + "\\begin{center}" + ls
                                + "\\resizebox{" + resizeFactor + "\\textwidth}{!}{" + ls
                                + "\\begin{tikzpicture}");
                    }

                    TreeMap<String, List<String>> frameCommandMapOverlaysInterval = mergeAdjacentIntervals(frameCommandMap);

                    for (Map.Entry<String, List<String>> commandOverlaysInterval : frameCommandMapOverlaysInterval.entrySet()) {
                        List<String> tikzCommandsList = commandOverlaysInterval.getValue();
                        /*tikzCommandsList.sort(Comparator.comparing((String s) -> s.contains("node"))
                                .thenComparing((String s) -> s.contains("color"))
                                .thenComparing(Comparator.naturalOrder()));*/
                        if (commandOverlaysInterval.getKey() != null) {
                            for (String tikzCommand : tikzCommandsList)
                                out.println("  \\only" + commandOverlaysInterval.getKey() + "{ " + tikzCommand + " }");
                        }
                        else {
                            for (String tikzCommand : tikzCommandsList)
                                out.println("  " + tikzCommand);
                        }
                    }

                    if (free) {
                        out.println("\\end{tikzpicture}" + ls);
                        out.println(after);
                    }
                    else {
                        out.println("\\end{tikzpicture}" + ls
                                + "}" + ls
                                + "\\end{center}" + ls
                                + "\\end{frame}");
                    }

                    out.flush();
                    out.close();

                    files.put(fileId, 1);
                    System.out.println("+++> ASPECT: File created " + filename);

                }
                // -----------------------------------------------------------------------------------------------
                // Multiple Files
                // -----------------------------------------------------------------------------------------------
                else {
                    List<String> tikzCommandsForNullFrame = frameCommandMap.get(null);
                    int fileId = files.isEmpty() ? 0 : files.lastKey();
                    fileId +=1 ;
                    int frameNumber = 1;
                    for (Map.Entry<String, List<String>> entry : frameCommandMap.entrySet()) {
                        // if there is no null frame only skip it otherwise create a single file
                        if (entry.getKey() == null && !(frameCommandMap.size() == 1)) {
                            continue;
                        }
                        String filename = name + "_" + fileId + (!(frameCommandMap.size() == 1) ? "_" + frameNumber + ".tex" : ".tex");
                        // String filename = name + "_" + fileId + "_" + frameNumber + ".tex";
                        File file = new File(filename);
                        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

                        // -----------------------------------------------------------------------------------------------
                        // Tikzpicture
                        // -----------------------------------------------------------------------------------------------

                        if (free) {
                            out.println(before + ls);
                            out.println("\\begin{tikzpicture}");
                        }
                        else if (merge) {
                            out.println("\\begin{frame}" + ls
                                    + "\\begin{center}" + ls
                                    + "\\resizebox{" + resizeFactor + "\\textwidth}{!}{" + ls
                                    + "\\begin{tikzpicture}");
                        }
                        else {
                            out.println("\\documentclass[tikz, dvipsnames]{standalone}" + ls
                                    + "\\usepackage{xcolor}");
                            if (graph) {
                                out.println("\\usetikzlibrary{graphs, quotes, graphdrawing}" + ls
                                        + "\\usegdlibrary{force}" + ls);
                            } else {
                                out.println("\\usetikzlibrary{calc}");
                            }
                            out.println("\\begin{document}" + ls
                                    + "\\begin{tikzpicture}");
                        }
                        if (graph) {
                            out.println("\\graph[spring layout, node distance=2cm and 2cm]{" + ls);
                        }

                        List<String> tikzCommandsList = entry.getValue();

                        if (tikzCommandsForNullFrame != null) {
                            /*if (!graph) tikzCommandsForNullFrame.sort(Comparator.comparing((String s) -> s.contains("node"))
                                    .thenComparing((String s) -> s.contains("color"))
                                    .thenComparing(Comparator.naturalOrder()));*/
                            for (String tikzCommand : tikzCommandsForNullFrame) {
                                out.println("  " + tikzCommand);
                            }
                        }
                        if (!(frameCommandMap.size() == 1)) {
                            /*if (!graph) tikzCommandsList.sort(Comparator.comparing((String s) -> s.contains("node"))
                                    .thenComparing((String s) -> s.contains("color"))
                                    .thenComparing(Comparator.naturalOrder()));*/
                            for (String tikzCommand : tikzCommandsList) {
                                out.println("  " + tikzCommand);
                            }
                        }

                        if (graph)
                            out.println("};");
                        if (free) {
                            out.println("\\end{tikzpicture}" + ls);
                            out.println(after);
                        }
                        else if (merge) {
                            out.println("\\end{tikzpicture}" + ls
                                    + "}" + ls
                                    + "\\end{center}" + ls
                                    + "\\end{frame}");
                        }
                        else {
                            out.println("\\end{tikzpicture}" + ls
                                    + "\\end{document}");
                        }

                        out.flush();
                        out.close();

                        files.put(fileId, frameNumber);
                        System.out.println("+++> ASPECT: File created " + filename);

                        if (!merge) buildLatex(filename);

                        frameNumber += 1;
                    }
                }

                // Resetto le HashMap per la nextLine
                dlMap.clear();
                styleMap.clear();
                layerMap.clear();
                labelMap.clear();

            }

            if (!isValidInput){
                System.err.println("***> ASPECT ERROR: atoms not found. Nothing to do !");
                System.err.println("     Please check the output of the ASP solver." + ls);
            }
            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
