import org.apache.commons.lang3.ObjectUtils;

import javax.lang.model.type.NullType;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

public class TexPdfThNew implements Runnable {
    InputStream input;
    boolean merge;
    boolean free;
    boolean graph;
    public static int fn = 1;
    String name;

    //definizione dell'input, argomenti in ingresso
    public TexPdfThNew(InputStream is, String filename, Boolean merge, Boolean free, Boolean graph) {
        this.input = is;
        this.name = filename;
        this.merge = merge;
        this.free = free;
        this.graph = graph;
    }

    private static Map<String, Object> processCommand(String command) {

        Pattern nodePattern = Pattern.compile("aspect_(draw|color|image)node\\(([^,]+),([^,]+),(\"[^\"]+\"|[a-zA-Z]+|[^,]+)(?:,([^,]+)(?:,(\\d+))?\\))?");
        Pattern linePattern = Pattern.compile("aspect_(draw|color)line\\(([^,]+),([^,]+),([^,]+),([^,]+)(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");
        Pattern arcPattern = Pattern.compile("aspect_(draw|color)arc\\(([^,]+),([^,]+),([^,]+),([^,]+),([^,]+)(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");
        Pattern arrowPattern = Pattern.compile("aspect_(draw|color)arrow\\(([^,]+),([^,]+),([^,]+),([^,]+)(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");
        Pattern rectanglePattern = Pattern.compile("aspect_(draw|color|fill)rectangle\\(([^,]+),([^,]+),([^,]+),([^,]+)(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");
        Pattern trianglePattern = Pattern.compile("aspect_(draw|color|fill)triangle\\(([^,]+),([^,]+),([^,]+),([^,]+),([^,]+),([^,]+)(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");
        Pattern circlePattern = Pattern.compile("aspect_(draw|color|fill)circle\\(([^,]+),([^,]+),([^,]+)(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");
        Pattern ellipsePattern = Pattern.compile("aspect_(draw|color|fill)ellipse\\(([^,]+),([^,]+),([^,]+)(?:,([a-zA-Z]+))?(?:,(\\d+))?\\)");

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
        String commandType = matcher.group(1);  // "draw" or "color"
        String x = matcher.group(2);
        String y = matcher.group(3);
        String argument1 = matcher.group(4);  // text or image path
        String argument2 = matcher.group(5);  // color or width
        String t = matcher.group(6);
        result.put("frame", t);
        if (commandType.equals("draw"))
            result.put("tikzCommand",
                    String.format("\\draw (%s,%s) node {\\LARGE %s};", x, y, argument1));
        else if(commandType.equals("color") && argument2 != null)
            result.put("tikzCommand",
                    String.format("\\draw [color=%s] (%s,%s) node {\\LARGE %s};", argument2, x, y, argument1));
        else if(commandType.equals("image") && argument2 != null)
            result.put("tikzCommand",
                    String.format("\\node [inner sep=0pt] (img) at (%s,%s) {\\includegraphics[width=%s px]{%s}};", x, y, argument2, argument1));
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
        if (commandType.equals("draw"))
            result.put("tikzCommand",
                    String.format("\\draw (%s,%s) -- (%s,%s);", x1, y1, x2, y2));
        else if(commandType.equals("color") && color != null)
            result.put("tikzCommand",
                    String.format("\\draw [color=%s] (%s,%s) -- (%s,%s);", color, x1, y1, x2, y2));
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
        if (commandType.equals("draw"))
            result.put("tikzCommand",
                    String.format("\\draw (%s,%s) arc (%s:%s:%s);", x1, y1, a1, a2, r1));
        else if(commandType.equals("color") && color != null)
            result.put("tikzCommand",
                    String.format("\\draw [color=%s] (%s,%s) arc (%s:%s:%s);", color, x1, y1, a1, a2, r1));
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
        if (commandType.equals("draw"))
            result.put("tikzCommand",
                    String.format("\\draw (%s,%s) -> (%s,%s);", x1, y1, x2, y2));
        else if(commandType.equals("color") && color != null)
            result.put("tikzCommand",
                    String.format("\\draw [color=%s] (%s,%s) -> (%s,%s);", color, x1, y1, x2, y2));
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
        if (commandType.equals("draw"))
            result.put("tikzCommand",
                    String.format("\\draw (%s,%s) rectangle (%s,%s);", x1, y1, x2, y2));
        else if (commandType.equals("color") && color != null)
            result.put("tikzCommand",
                    String.format("\\draw [color=%s] (%s,%s) rectangle (%s,%s);", color, x1, y1, x2, y2));
        else if (commandType.equals("fill") && color != null)
            result.put("tikzCommand",
                    String.format("\\draw [fill=%s] (%s,%s) rectangle (%s,%s);", color, x1, y1, x2, y2));
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
        if (commandType.equals("draw"))
            result.put("tikzCommand",
                    String.format("\\draw (%s,%s) -- (%s,%s) -- (%s,%s) cycle;", x1, y1, x2, y2, x3, y3));
        else if (commandType.equals("color") && color != null)
            result.put("tikzCommand",
                    String.format("\\draw [color=%s] (%s,%s) -- (%s,%s) -- (%s,%s) cycle;", color, x1, y1, x2, y2, x3, y3));
        else if (commandType.equals("fill") && color != null)
            result.put("tikzCommand",
                    String.format("\\draw [fill=%s] (%s,%s) -- (%s,%s) -- (%s,%s) cycle;", color, x1, y1, x2, y2, x3, y3));
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
        if (commandType.equals("draw"))
            result.put("tikzCommand",
                    String.format("\\draw (%s,%s) circle (%s);", x1, y1, r));
        else if (commandType.equals("color") && color != null)
            result.put("tikzCommand",
                    String.format("\\draw [color=%s] (%s,%s) circle (%s);", color, x1, y1, r));
        else if (commandType.equals("fill") && color != null)
            result.put("tikzCommand",
                    String.format("\\draw [fill=%s] (%s,%s) circle (%s);", color, x1, y1, r));
        return result;
    }

    private static Map<String, Object> processEllipse(Matcher matcher) {
        Map<String, Object> result = new HashMap<>();
        String commandType = matcher.group(1);  // "draw" or "color" or "fill"
        String x1 = matcher.group(1);
        String y1 = matcher.group(2);
        String r1 = matcher.group(3);
        String r2 = matcher.group(4);
        String color = matcher.group(5);
        String t = matcher.group(6);
        result.put("frame", t);
        if (commandType.equals("draw"))
            result.put("tikzCommand",
                    String.format("\\draw (%s,%s) ellipse (%s and %s);", x1, y1, r1, r2));
        else if (commandType.equals("color") && color != null)
            result.put("tikzCommand",
                    String.format("\\draw [color=%s] (%s,%s) ellipse (%s and %s);", color, x1, y1, r1, r2));
        else if (commandType.equals("fill") && color != null)
            result.put("tikzCommand",
                    String.format("\\draw [fill=%s] (%s,%s) ellipse (%s and %s);", color, x1, y1, r1, r2));
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
                        String.format("{%s[minimum size=7mm, draw]},", name));
            else
                result.put("tikzCommand",
                        String.format("{%s[minimum size=7mm, arg1, draw]},", name));
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
            result.put("tikzCommand",
                    String.format("{%s ->[\"%s\"] %s},", argA, text, argB));
        }
        return result;
    }

    public void run() {
        try {
            // apertura buffer in ingresso
            BufferedReader threadIn = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

            StringBuilder before = new StringBuilder();
            StringBuilder after = new StringBuilder();
            String line;
            String ls = System.getProperty("line.separator");

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

            // lettura input
            String tikz_commandline = threadIn.readLine();
            if (tikz_commandline == null){
                System.err.println("ASPECT atoms not found. Nothing to do !");
                System.err.println("Please check the output of the ASP solver.");
                System.exit(1);
            }

            while (tikz_commandline != null) {
                // creo file tex e buffer per scriverci
                String texname = ASPECT.file_out_prefix + name + fn + ".tex";
                File tex = new File(texname);
                FileWriter fw = new FileWriter(tex);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw);

                // stringhe e array necessari alla conversione
                String[] tikz_commands;
                ArrayList<String> graph_links = new ArrayList<>();

                // divido gli atomi
                tikz_commands = tikz_commandline.split(" ");
                // order is useful in graph mode so when I print multiple
                // solutions of the same problem the layout is the same
                Arrays.sort(tikz_commands);

                // stampa header
                if (merge) {
                    out.println("\\begin{tikzpicture}" + ls);
                } else if (free) {
                    out.println(before + ls);
                    out.println("\\begin{tikzpicture}" + ls);
                } else {
                    //out.println("\\documentclass{article}" + ls
                    out.println("\\documentclass[tikz, border=2pt]{standalone}\n" + ls);
                            //+ "\\usepackage{tikz}" + ls
                            //+ "\\usepackage{graphicx}" + ls);
                    if(graph) {
                        out.println("\\usetikzlibrary{graphs,quotes,graphdrawing}" + ls
                                + "\\usegdlibrary{force}" + ls);
                    }
                    out.println("\\begin{document}" + ls
                            //+ "\\section*{Answer:" + fn + "}" + ls
                            //+ "\\begin{figure}[!h]" + ls
                            //+ "\\centering" + ls
                            //+ "\\resizebox{\\textwidth}{!}{%" + ls
                            + "\\begin{tikzpicture}" + ls);
                }
                if (graph) {
                    out.println("\\graph[spring layout, node distance=2cm and 2cm]{" + ls);
                }

                // ciclo sugli atomi
                for (String tikz_command : tikz_commands) {

                    if (tikz_command.contains("aspect")) {

                        // atomi in modalità graph
                        if (graph) {

                            // traduzione nodi, scrittura comando tikz
                            if (tikz_command.contains("node")) {

                                Map<String, Object> result = processCommandGraph(tikz_command);
                                if (result != null) {
                                    String tikzCommand = (String) result.get("tikzCommand");
                                    int frame = (int) result.get("frame");
                                    out.println(tikzCommand);
                                }

                                // traduzione connessioni, scrittura comando tikz
                            } else if (tikz_command.contains("line")) {

                                graph_links.add(tikz_command);

                            }
                            // traduzione frecce, scrittura comando tikz
                            else if (tikz_command.contains("arrow")) {

                                graph_links.add(tikz_command);

                            }

                        } else {
                            Map<String, Object> result = processCommand(tikz_command);
                            if (result != null) {
                                String tikzCommand = (String) result.get("tikzCommand");
                                String frame = (String) result.get("frame");
                                if (frame == null)
                                    System.err.println("nullo");
                                else {
                                    System.err.println(frame);
                                }
                                out.println(tikzCommand);
                            }
                        }
                    }
                }

                if (graph) {

                    for (String tikz_command : graph_links) {
                        Map<String, Object> result = processCommandGraph(tikz_command);
                        if (result != null) {
                            String tikzCommand = (String) result.get("tikzCommand");
                            int frame = (int) result.get("frame");
                            out.println(tikzCommand);
                        }
                    }
                    out.println("};" + ls);
                }

                // stampa footer documento
                if (merge) {
                    out.println("\\end{tikzpicture}");

                } else if (free) {
                    out.println("\\end{tikzpicture}" + ls);
                    out.println(after);
                }
                else {
                    out.println("\\end{tikzpicture}" + ls
                            //+ "}" + ls
                            //+ "\\end{figure}" + ls
                            + "\\end{document}");
                }
                // flush e chiusura buffer output
                out.flush();
                bw.flush();
                fw.flush();

                out.close();
                bw.close();
                fw.close();
                fn = fn + 1;
                System.out.println("File created: " + texname);

                // avvio pdflatex e passaggio del file creato
                if (!merge && !free) {
                    ProcessBuilder processBuilderPDF = new ProcessBuilder();
                    if (graph) {
                        processBuilderPDF.command("lualatex", "-halt-on-error", texname);
                    }
                    else {
                        processBuilderPDF.command("pdflatex", "-halt-on-error", texname);
                    }
                    Process pdf = processBuilderPDF.start();


                    // ottenimento nome pdf e conferma creazione
                    String pdfname = texname.substring(0, texname.lastIndexOf(".")) + ".pdf";
                    System.out.println("Building... " + pdfname + "\r");
                    // attendo fine esecuzione pdf
                    pdf.waitFor();

                    if(pdf.exitValue() != 0){
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

                    System.out.println("File created: " + pdfname + "\r");
                    // passo a nuova linea

                }
                tikz_commandline = threadIn.readLine();

            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
