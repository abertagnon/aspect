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
    boolean verbose;
    boolean merge;
    boolean free;
    boolean graph;
    public static int fn = 0;
    String name;

    //definizione dell'input, argomenti in ingresso
    public TexPdfThNew(InputStream is, String filename, boolean verbose, boolean merge, boolean free, boolean graph) {
        this.input = is;
        this.verbose = verbose;
        this.name = filename;
        this.merge = merge;
        this.free = free;
        this.graph = graph;
    }

    private static Map<String, Object> processCommand(String command) {

        Pattern nodePattern =
                Pattern.compile("aspect_(draw|image|color)node\\(([0-9]{0,3}),([0-9]{0,3}),(\\w+|\"[^\"]+\")?(?:,(\\w+))?(?:,(\\d+))?\\)");
        Pattern linePattern =
                Pattern.compile("aspect_(draw|color)line\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");
        Pattern arcPattern =
                Pattern.compile("aspect_(draw|color)arc\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");
        Pattern arrowPattern =
                Pattern.compile("aspect_(draw|color)arrow\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");
        Pattern rectanglePattern =
                Pattern.compile("aspect_(draw|color|fill)rectangle\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");
        Pattern trianglePattern =
                Pattern.compile("aspect_(draw|color|fill)triangle\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");
        Pattern circlePattern =
                Pattern.compile("aspect_(draw|color|fill)circle\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");
        Pattern ellipsePattern =
                Pattern.compile("aspect_(draw|color|fill)ellipse\\(([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3}),([0-9]{0,3})(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");

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
        String argument2 = matcher.group(5);  // color or width (color/image) or frame (draw)
        String t = matcher.group(6);
        switch (commandType) {
            case "draw":
                if (argument2 == null || argument2.matches("\\d+")) {
                    t = matcher.group(5);
                    result.put("tikzCommand",
                            String.format("\\draw  (%s,%s) node[text centered, anchor=base, text height=72pt, font=\\fontsize{72}{10}\\selectfont] {%s};", x, y, argument1));
                } else return null;
                break;
            case "color":
                if (argument2 != null && !argument2.matches("\\d+")) {
                    result.put("tikzCommand",
                            String.format("\\draw [text=%s] (%s,%s) node[text centered, anchor=base, text height=72pt, font=\\fontsize{72}{10}\\selectfont] {%s};", argument2, x, y, argument1));
                } else return null;
                break;
            case "image":
                if (argument2 != null && argument2.matches("\\d+")) {
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
                    result.put("tikzCommand",
                            String.format("\\draw [color=%s] (%s,%s) rectangle (%s,%s);", color, x1, y1, x2, y2));
                } else return null;
                break;
            case "fill":
                if (color != null && !color.matches("\\d+")) {
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
                    result.put("tikzCommand",
                            String.format("\\draw [color=%s] (%s,%s) -- (%s,%s) -- (%s,%s) -- cycle;", color, x1, y1, x2, y2, x3, y3));
                } else return null;
                break;
            case "fill":
                if (color != null && !color.matches("\\d+")) {
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
                    result.put("tikzCommand",
                            String.format("\\draw [color=%s] (%s,%s) circle (%s);", color, x1, y1, r));
                } else return null;
                break;
            case "fill":
                if (color != null && !color.matches("\\d+")) {
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
                    result.put("tikzCommand",
                            String.format("\\draw [color=%s] (%s,%s) ellipse (%s and %s);", color, x1, y1, r1, r2));
                } else return null;
                break;
            case "fill":
                if (color != null && !color.matches("\\d+")) {
                    result.put("tikzCommand",
                            String.format("\\fill [%s] (%s,%s) ellipse (%s and %s);", color, x1, y1, r1, r2));
                } else return null;
                break;
        }
        return result;
    }

    private static Map<String, Object> processCommandGraph(String command) {

        Pattern nodePattern = Pattern.compile("aspect_(draw|color)node\\(([^,]+)(?:,([^,]+))?(?:,([^,]+))?(?:,(\\d+))?\\)");
        Pattern edgePattern = Pattern.compile("aspect_(draw|quote)line\\(([^,]+),([^,]+)(?:,([^,]+))?(?:,(\\d+))?\\)");
        Pattern arrowPattern = Pattern.compile("aspect_(draw|quote)arrow\\(([^,]+),([^,]+)(?:,([^,]+))?(?:,(\\d+))?\\)");

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
        result.put("frame", t);
        if (commandType.equals("draw")) {
            if (arg1 != null)
                result.put("tikzCommand",
                        String.format("{%s[minimum size=7mm, %s, draw]},", name, arg1));
            else
                result.put("tikzCommand",
                        String.format("{%s[minimum size=7mm, circle, draw]},", name));
        }
        else if (commandType.equals("color") && arg1 != null){
            if (arg2 != null)
                result.put("tikzCommand",
                        String.format("{%s[fill=%s, minimum size=7mm, %s, draw]},", name, arg1, arg2));
            else
                result.put("tikzCommand",
                        String.format("{%s[fill=%s, minimum size=7mm, circle, draw]},", name, arg1));
        }
        return result;
    }

    private static Map<String, Object> processGraphEdge(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "quote"
        String argA = matcher.group(2);
        String argB = matcher.group(3);
        String text = matcher.group(4);
        String t = matcher.group(5);
        result.put("frame", t);
        if (commandType.equals("draw")) {
            result.put("tikzCommand",
                    String.format("{%s -- %s},", argA, argB));
        }
        else if (commandType.equals("quote") && text != null){
            text = text.replace("\"", "");
            result.put("tikzCommand",
                    String.format("{%s --[\"%s\"] %s},", argA, text, argB));
        }
        return result;
    }

    private static Map<String, Object> processGraphArrow(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "quote"
        String argA = matcher.group(2);
        String argB = matcher.group(3);
        String text = matcher.group(4);
        String t = matcher.group(5);
        result.put("frame", t);
        if (commandType.equals("draw")) {
            result.put("tikzCommand",
                    String.format("{%s -> %s},", argA, argB));
        }
        else if (commandType.equals("quote") && text != null){
            text = text.replace("\"", "");
            result.put("tikzCommand",
                    String.format("{%s ->[\"%s\"] %s},", argA, text, argB));
        }
        return result;
    }

    public class NumericStringComparator implements Comparator<String> {
        @Override
        public int compare(String str1, String str2) {
            if (str1 == null && str2 == null) return 0;
            else if (str1 == null) return -1;
            else if (str2 == null) return 1;

            try {
                int num1 = Integer.parseInt(str1);
                int num2 = Integer.parseInt(str2);
                return Integer.compare(num1, num2);
            } catch (NumberFormatException e) {
                return str1.compareTo(str2);
            }
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

                BufferedReader bereader = new BufferedReader(new FileReader("before.tex"));

                while ((line = bereader.readLine()) != null) {
                    before.append(line);
                    before.append(ls);
                }

                BufferedReader afreader = new BufferedReader(new FileReader("after.tex"));

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
                Map<String, List<String>> frameCommandMap = new TreeMap<>(new NumericStringComparator());
                Pattern pattern = Pattern.compile("aspect_\\w+\\((?:\"[^\"]*\"|[^)\"]*)*\\)");
                Matcher matcher = pattern.matcher(inputAtomsString);
                ArrayList<String> matches = new ArrayList<>();
                while (matcher.find()) {
                    String match = matcher.group();
                    matches.add(match);
                }
                if(matches.isEmpty()) continue;
                isValidInput = true;
                String[] inputAtomsArray = matches.toArray(new String[0]);
                // order is useful in graph mode so when I print multiple
                // solutions of the same problem the layout is the same
                Arrays.sort(inputAtomsArray);

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
                    }
                    frameCommandMap.computeIfAbsent(null, k -> new LinkedList<>()).add("};" + ls);
                }
                else {
                    for (String atom : inputAtomsArray) {
                        Map<String, Object> result = processCommand(atom);
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

                List<String> tikzCommandsForNullFrame = frameCommandMap.get(null);

                for (Map.Entry<String, List<String>> entry : frameCommandMap.entrySet()) {
                    if (entry.getKey() == null && !(frameCommandMap.size() == 1)) {
                        continue;
                    }
                    fn += 1;
                    String filename = name + "_" + fn + ".tex";
                    File file = new File(filename);
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

                    // -----------------------------------------------------------------------------------------------
                    // Latex Header
                    // -----------------------------------------------------------------------------------------------

                    if (merge) {
                        out.println("\\begin{tikzpicture}" + ls);
                    } else if (free) {
                        out.println(before + ls);
                        out.println("\\begin{tikzpicture}" + ls);
                    } else {
                        out.println("\\documentclass[tikz, border=2pt]{standalone}" + ls);
                        if (graph) {
                            out.println("\\usetikzlibrary{graphs, quotes, graphdrawing}" + ls
                                    + "\\usegdlibrary{force}" + ls);
                        }
                        out.println("\\begin{document}" + ls
                                + "\\begin{tikzpicture}" + ls);
                    }
                    if (graph) {
                        out.println("\\graph[spring layout, node distance=2cm and 2cm]{" + ls);
                    }

                    // -----------------------------------------------------------------------------------------------
                    // Tikzpicture
                    // -----------------------------------------------------------------------------------------------
                    List<String> tikzCommandsList = entry.getValue();

                    if (tikzCommandsForNullFrame != null) {
                        for (String tikzCommand : tikzCommandsForNullFrame) {
                            out.println("  " + tikzCommand);
                        }
                    }
                    if (!(frameCommandMap.size() == 1)) {
                        for (String tikzCommand : tikzCommandsList) {
                            out.println("  " + tikzCommand);
                        }
                    }

                    // -----------------------------------------------------------------------------------------------
                    // Latex Footer
                    // -----------------------------------------------------------------------------------------------

                    if (merge) {
                        out.println("\\end{tikzpicture}");

                    } else if (free) {
                        out.println("\\end{tikzpicture}" + ls);
                        out.println(after);
                    } else {
                        out.println("\\end{tikzpicture}" + ls
                                + "\\end{document}");
                    }

                    // flush e chiusura buffer output
                    out.flush();
                    out.close();

                    System.out.println("+++> ASPECT: File created " + filename + ls);

                    // -----------------------------------------------------------------------------------------------
                    // Compiling: pdflatex, lualatex
                    // -----------------------------------------------------------------------------------------------

                    if (!merge && !free) {
                        ProcessBuilder processBuilderPDF = new ProcessBuilder();
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
                    }
                }
            }
            if (!isValidInput){
                System.err.println("***> ASPECT ERROR: atoms not found. Nothing to do !");
                System.err.println("     Please check the output of the ASP solver." + ls);
                // System.exit(1);
            }
            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
