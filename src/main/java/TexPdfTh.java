import java.io.*;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Scanner;

public class TexPdfThNew implements Runnable {
    InputStream input;
    String name;
    boolean verbose;
    boolean merge;
    boolean free;
    String resizeFactor;
    static boolean pdfBuild = true;
    static boolean graph;
    // <fileId, frameNumber>
    static TreeMap<Integer, Integer> files = new TreeMap<>();
    static String beforeFilename = "before.tex";
    static String afterFilename = "after.tex";

    //definizione dell'input, argomenti in ingresso
    public TexPdfThNew(InputStream is, String filename, boolean verbose, boolean merge, boolean free, String resizeFactor) {
        this.input = is;
        this.name = filename;
        this.verbose = verbose;
        this.merge = merge;
        this.free = free;
        this.resizeFactor = resizeFactor;
    }

    private static Map<String, Object> processCommand(String command) {

        Pattern nodePattern =
                Pattern.compile("aspect_(draw|image|color)node\\(([0-9]{0,3}),([0-9]{0,3}),(\\w+|\"[^\"]+\")?(?:,(\\w+|\"[^\"]+\"))?(?:,(\\w+))?(?:,(\\d+))?\\)");
        Pattern linePattern =
                Pattern.compile("aspect_(draw|color)line\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,(\\w+|\"[^\"]+\"))?(?:,(\\d+))?\\)");
        Pattern arcPattern =
                Pattern.compile("aspect_(draw|color)arc\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,(\\w+|\"[^\"]+\"))?(?:,(\\d+))?\\)");
        Pattern arrowPattern =
                Pattern.compile("aspect_(draw|color)arrow\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,(\\w+|\"[^\"]+\"))?(?:,(\\d+))?\\)");
        Pattern rectanglePattern =
                Pattern.compile("aspect_(draw|color|fill)rectangle\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,(\\w+|\"[^\"]+\"))?(?:,(\\d+))?\\)");
        Pattern trianglePattern =
                Pattern.compile("aspect_(draw|color|fill)triangle\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,(\\w+|\"[^\"]+\"))?(?:,(\\d+))?\\)");
        Pattern circlePattern =
                Pattern.compile("aspect_(draw|color|fill)circle\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,(\\w+|\"[^\"]+\"))?(?:,(\\d+))?\\)");
        Pattern ellipsePattern =
                Pattern.compile("aspect_(draw|color|fill)ellipse\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,(\\w+|\"[^\"]+\"))?(?:,(\\d+))?\\)");

        Matcher matcher;

        if ((matcher = nodePattern.matcher(command)).matches()) {
            return processNode(matcher);
        } else if ((matcher = linePattern.matcher(command)).matches()) {
            return processLine(matcher);
        } else if ((matcher = arcPattern.matcher(command)).matches()) {
            return processArc(matcher);
        } else if ((matcher = arrowPattern.matcher(command)).matches()) {
            return processArrow(matcher);
        } else if ((matcher = rectanglePattern.matcher(command)).matches()) {
            return processRectangle(matcher);
        } else if ((matcher = trianglePattern.matcher(command)).matches()) {
            return processTriangle(matcher);
        } else if ((matcher = circlePattern.matcher(command)).matches()) {
            return processCircle(matcher);
        } else if ((matcher = ellipsePattern.matcher(command)).matches()) {
            return processEllipse(matcher);
        }
        return null;
    }

    private static Map<String, Object> processNode(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "color" or "image"
        String x = matcher.group(2);
        String y = matcher.group(3);
        String argument1 = matcher.group(4).replace("\"", "");   // text or image path
        String argument2 = matcher.group(5);  // text size or color (color) width (image) or text size or frame (draw)
        String argument3 = matcher.group(6);
        String t = matcher.group(7);

        switch (commandType) {
            case "draw":
                if (argument2 == null || argument2.matches("\\d+")) {
                    t = matcher.group(5);
                    result.put("tikzCommand",
                            String.format("\\draw (%s,%s) node[text centered] {%s};", x, y, argument1));

                } else if (argument2.matches("\"?(tiny|scriptsize|footnotesize|small|normalsize|large|Large|LARGE|huge|Huge)\"?") && (argument3 == null || argument3.matches("\\d+"))) {
                    argument2 = argument2.replace("\"", "");
                    t = matcher.group(6);
                    result.put("tikzCommand",
                            String.format("\\draw (%s,%s) node[text centered, font=\\%s] {%s};", x, y, argument2, argument1));
                } else return null;
                break;
            case "color":
                if (argument2 == null) return null;
                if (argument2.matches("\"?(tiny|scriptsize|footnotesize|small|normalsize|large|Large|LARGE|huge|Huge)\"?") && argument3.matches("\\w+")) {
                    argument2 = argument2.replace("\"", "");
                    argument3 = argument3.replace("\"", "");
                    result.put("tikzCommand",
                            String.format("\\draw [text=%s] (%s,%s) node[text centered, font=\\%s] {%s};", argument3, x, y, argument2, argument1));
                } else if (argument2.matches("\\w+") && (argument3 == null || argument3.matches("\\d+"))){
                    t = matcher.group(6);
                    argument2 = argument2.replace("\"", "");
                    result.put("tikzCommand",
                            String.format("\\draw [text=%s] (%s,%s) node[text centered] {%s};", argument2, x, y, argument1));
                } else return null;
                break;
            case "image":
                if (argument2 != null && argument2.matches("\\d+") && (argument3 == null || argument3.matches("\\d+"))) {
                    t = matcher.group(6);
                    result.put("tikzCommand",
                            String.format("\\node [inner sep=0pt] (img) at (%s,%s) {\\includegraphics[width=%s px]{%s}};", x, y, argument2, argument1));
                } else return null;
                break;
        }
        result.put("frame", t);
        return result;
    }

    private static Map<String, Object> processLine(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "color"
        String x1 = matcher.group(2);
        String y1 = matcher.group(3);
        String x2 = matcher.group(4);
        String y2 = matcher.group(5);
        String color = matcher.group(6);
        String t = matcher.group(7);
        result.put("frame", t);
        if (commandType.equals("draw")) {
            if (color == null) {
                result.put("tikzCommand",
                        String.format("\\draw (%s,%s) -- (%s,%s);", x1, y1, x2, y2));
            } else return null;
        }
        else if(commandType.equals("color")) {
            if (color != null) {
                color = color.replace("\"", "");
                result.put("tikzCommand",
                        String.format("\\draw [color=%s] (%s,%s) -- (%s,%s);", color, x1, y1, x2, y2));
            } else return null;
        }
        return result;
    }

    private static Map<String, Object> processArc(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "color"
        String x1 = matcher.group(2);
        String y1 = matcher.group(3);
        String a1 = matcher.group(4);
        String a2 = matcher.group(5);
        String r1 = matcher.group(6);
        String color = matcher.group(7);
        String t = matcher.group(8);
        result.put("frame", t);
        if (commandType.equals("draw")) {
            if (color == null) {
                result.put("tikzCommand",
                        String.format("\\draw (%s,%s) arc (%s:%s:%s);", x1, y1, a1, a2, r1));
            } else return null;
        }
        else if(commandType.equals("color")) {
            if (color != null) {
                color = color.replace("\"", "");
                result.put("tikzCommand",
                        String.format("\\draw [color=%s] (%s,%s) arc (%s:%s:%s);", color, x1, y1, a1, a2, r1));
            } else return null;
        }
        return result;
    }

    private static Map<String, Object> processArrow(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "color"
        String x1 = matcher.group(2);
        String y1 = matcher.group(3);
        String x2 = matcher.group(4);
        String y2 = matcher.group(5);
        String color = matcher.group(6);
        String t = matcher.group(7);
        result.put("frame", t);
        if (commandType.equals("draw")) {
            if (color == null) {
                result.put("tikzCommand",
                        String.format("\\draw [->] (%s,%s) -- (%s,%s);", x1, y1, x2, y2));
            } else return null;
        }
        else if(commandType.equals("color")) {
            if (color != null) {
                color = color.replace("\"", "");
                result.put("tikzCommand",
                        String.format("\\draw [->, color=%s] (%s,%s) -- (%s,%s);", color, x1, y1, x2, y2));
            } else return null;
        }
        return result;
    }

    private static Map<String, Object> processRectangle(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "color" or "fill"
        String x1 = matcher.group(2);
        String y1 = matcher.group(3);
        String x2 = matcher.group(4);
        String y2 = matcher.group(5);
        String color = matcher.group(6);
        String t = matcher.group(7);
        result.put("frame", t);
        switch (commandType) {
            case "draw":
                if (color == null) {
                    result.put("tikzCommand",
                            String.format("\\draw (%s,%s) rectangle (%s,%s);", x1, y1, x2, y2));
                } else return null;
                break;
            case "color":
                if (color != null && !color.matches("\\d+")) {
                    color = color.replace("\"", "");
                    result.put("tikzCommand",
                            String.format("\\draw [color=%s] (%s,%s) rectangle (%s,%s);", color, x1, y1, x2, y2));
                } else return null;
                break;
            case "fill":
                if (color != null && !color.matches("\\d+")) {
                    color = color.replace("\"", "");
                    result.put("tikzCommand",
                            String.format("\\fill [%s] (%s,%s) rectangle (%s,%s);", color, x1, y1, x2, y2));
                } else return null;
                break;
        }
        return result;
    }

    private static Map<String, Object> processTriangle(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "color" or "fill"
        String x1 = matcher.group(2);
        String y1 = matcher.group(3);
        String x2 = matcher.group(4);
        String y2 = matcher.group(5);
        String x3 = matcher.group(6);
        String y3 = matcher.group(7);
        String color = matcher.group(8);
        String t = matcher.group(9);
        result.put("frame", t);
        switch (commandType) {
            case "draw":
                if (color == null) {
                    result.put("tikzCommand",
                            String.format("\\draw (%s,%s) -- (%s,%s) -- (%s,%s) -- cycle;", x1, y1, x2, y2, x3, y3));
                } else return null;
                break;
            case "color":
                if (color != null && !color.matches("\\d+")) {
                    color = color.replace("\"", "");
                    result.put("tikzCommand",
                            String.format("\\draw [color=%s] (%s,%s) -- (%s,%s) -- (%s,%s) -- cycle;", color, x1, y1, x2, y2, x3, y3));
                } else return null;
                break;
            case "fill":
                if (color != null && !color.matches("\\d+")) {
                    color = color.replace("\"", "");
                    result.put("tikzCommand",
                            String.format("\\fill [%s] (%s,%s) -- (%s,%s) -- (%s,%s) -- cycle;", color, x1, y1, x2, y2, x3, y3));
                } else return null;
                break;
        }
        return result;
    }

    private static Map<String, Object> processCircle(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "color" or "fill"
        String x1 = matcher.group(2);
        String y1 = matcher.group(3);
        String r = matcher.group(4);
        String color = matcher.group(5);
        String t = matcher.group(6);
        result.put("frame", t);
        switch (commandType) {
            case "draw":
                if (color == null) {
                    result.put("tikzCommand",
                            String.format("\\draw (%s,%s) circle (%s);", x1, y1, r));
                } else return null;
                break;
            case "color":
                if (color != null && !color.matches("\\d+")) {
                    color = color.replace("\"", "");
                    result.put("tikzCommand",
                            String.format("\\draw [color=%s] (%s,%s) circle (%s);", color, x1, y1, r));
                } else return null;
                break;
            case "fill":
                if (color != null && !color.matches("\\d+")) {
                    color = color.replace("\"", "");
                    result.put("tikzCommand",
                            String.format("\\fill [%s] (%s,%s) circle (%s);", color, x1, y1, r));
                } else return null;
                break;
        }
        return result;
    }

    private static Map<String, Object> processEllipse(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "color" or "fill"
        String x1 = matcher.group(2);
        String y1 = matcher.group(3);
        String r1 = matcher.group(4);
        String r2 = matcher.group(5);
        String color = matcher.group(6);
        String t = matcher.group(7);
        result.put("frame", t);
        switch (commandType) {
            case "draw":
                if (color == null) {
                    result.put("tikzCommand",
                            String.format("\\draw (%s,%s) ellipse (%s and %s);", x1, y1, r1, r2));
                } else return null;
                break;
            case "color":
                if (color != null && !color.matches("\\d+")) {
                    color = color.replace("\"", "");
                    result.put("tikzCommand",
                            String.format("\\draw [color=%s] (%s,%s) ellipse (%s and %s);", color, x1, y1, r1, r2));
                } else return null;
                break;
            case "fill":
                if (color != null && !color.matches("\\d+")) {
                    color = color.replace("\"", "");
                    result.put("tikzCommand",
                            String.format("\\fill [%s] (%s,%s) ellipse (%s and %s);", color, x1, y1, r1, r2));
                } else return null;
                break;
        }
        return result;
    }

    private static Map<String, Object> processCommandGraph(String command) {

        Pattern nodePattern = Pattern.compile("aspect_graph(draw|color)node\\(([^,]+)(?:,([^,]+))?(?:,([^,]+))?(?:,(\\d+))?\\)");
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
                        String.format("{%s[minimum size=7mm, circle, draw]},", name));
        }
        else if (commandType.equals("color") && arg1 != null){
            arg1 = arg1.replace("\"", "");
            if (arg2 != null)
                result.put("tikzCommand",
                        String.format("{%s[fill=%s, minimum size=7mm, %s, draw]},", name, arg1, arg2));
            else
                result.put("tikzCommand",
                        String.format("{%s[fill=%s, minimum size=7mm, circle, draw]},", name, arg1));
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
            String ls = System.getProperty("line.separator");
            ProcessBuilder processBuilderPDF = new ProcessBuilder();

            System.out.println(ls + "+++> ASPECT: Preparing files for building...");
            Thread.sleep(graph ? 5000 : 2000);

            if (graph) {
                processBuilderPDF.command("lualatex", "-halt-on-error", filename);
            } else {
                processBuilderPDF.command("pdflatex", "-halt-on-error", filename);
            }
            Process pdf = processBuilderPDF.start();
            String pdfname = filename.substring(0, filename.lastIndexOf(".")) + ".pdf";
            System.out.println("+++> ASPECT: Building " + pdfname);
            pdf.waitFor();

            if (pdf.exitValue() != 0) {
                InputStream in = pdf.getInputStream();
                if (in.available() > 0) {
                    BufferedReader errread = new BufferedReader(new InputStreamReader(in));
                    String error = errread.readLine();
                    do {
                        System.err.println(error);
                        error = errread.readLine();
                    } while (error != null);
                    System.exit(1);
                }
            }
            System.out.println("+++> ASPECT: File created " + pdfname + ls);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void run() {
        try {
            String ls = System.getProperty("line.separator");
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

            Scanner scanner = new Scanner(input);
            boolean isValidInput = false;
            while (scanner.hasNextLine()) {
                String inputAtomsString = scanner.nextLine();
                if (verbose) System.out.println(inputAtomsString);
                TreeMap<String, List<String>> frameCommandMap = new TreeMap<>(new NumericStringComparator());
                Pattern pattern = Pattern.compile("aspect_\\w+\\((?:\"[^\"]*\"|[^)\"]*)*\\)");
                Matcher matcher = pattern.matcher(inputAtomsString);
                ArrayList<String> matches = new ArrayList<>();
                while (matcher.find()) {
                    String match = matcher.group();
                    matches.add(match);
                }
                if (matches.isEmpty()) continue;
                isValidInput = true;
                String[] inputAtomsArray = matches.toArray(new String[0]);
                // order is useful in graph mode so when I print multiple
                // solutions of the same problem the layout is the same
                Arrays.sort(inputAtomsArray);

                boolean graphCommandIsPresent = false;
                boolean standardCommandIsPresent = false;
                pattern = Pattern.compile("aspect_(graphdrawnode|graphcolornode|" +
                        "graphdrawline|graphquoteline|" +
                        "graphdrawarrow|graphquotearrow)\\(.*\\)");
                for (String s : inputAtomsArray) {
                    matcher = pattern.matcher(s);
                    if (matcher.matches()) graphCommandIsPresent = true;
                    else standardCommandIsPresent = true;
                }

                if (graphCommandIsPresent && standardCommandIsPresent) {
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
                    for (String atom : inputAtomsArray) {
                        Map<String, Object> result = processCommand(atom);
                        if (result != null) {
                            String tikzCommand = (String) result.get("tikzCommand");
                            String frame = (String) result.get("frame");
                            frameCommandMap.computeIfAbsent(frame, k -> new LinkedList<>()).add(tikzCommand);
                        } else {
                            System.err.println("***> ASPECT ERROR: command not found: " + atom + ls);
                            System.err.println("     Remember that numerical values (coordinates) " + ls +
                                    "     must be within the range 000-999 as required " + ls +
                                    "     also by the TikZ language" + ls);
                        }
                    }
                }

                boolean useOverlay = frameCommandMap.size() > 1;

                // -----------------------------------------------------------------------------------------------
                // Single File w/ Beamer Overlays
                // -----------------------------------------------------------------------------------------------
                if (merge && useOverlay && !graph) {
                    int fileId = files.isEmpty() ? 0 : files.lastKey();
                    fileId +=1 ;
                    String filename = name + "_" + fileId + ".tex";
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
                        tikzCommandsList.sort(Comparator.comparing((String s) -> s.contains("node"))
                                .thenComparing((String s) -> s.contains("color"))
                                .thenComparing(Comparator.naturalOrder()));
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
                            out.println("\\documentclass[tikz]{standalone}" + ls);
                            if (graph) {
                                out.println("\\usetikzlibrary{graphs, quotes, graphdrawing}" + ls
                                        + "\\usegdlibrary{force}" + ls);
                            }
                            out.println("\\begin{document}" + ls
                                    + "\\begin{tikzpicture}");
                        }
                        if (graph) {
                            out.println("\\graph[spring layout, node distance=2cm and 2cm]{" + ls);
                        }

                        List<String> tikzCommandsList = entry.getValue();

                        if (tikzCommandsForNullFrame != null) {
                            if (!graph) tikzCommandsForNullFrame.sort(Comparator.comparing((String s) -> s.contains("node"))
                                    .thenComparing((String s) -> s.contains("color"))
                                    .thenComparing(Comparator.naturalOrder()));
                            for (String tikzCommand : tikzCommandsForNullFrame) {
                                out.println("  " + tikzCommand);
                            }
                        }
                        if (!(frameCommandMap.size() == 1)) {
                            if (!graph) tikzCommandsList.sort(Comparator.comparing((String s) -> s.contains("node"))
                                    .thenComparing((String s) -> s.contains("color"))
                                    .thenComparing(Comparator.naturalOrder()));
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
