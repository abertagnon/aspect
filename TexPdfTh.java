import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TexPdfTh implements Runnable {
    InputStream input;
    boolean mergeb;
    boolean freeb;
    
    boolean graphb;
    public static int fn = 1;
    String name;

    //definizione dell'input, argomenti in ingresso
    public TexPdfTh(InputStream is, String filename, Boolean merge, Boolean free, Boolean graph) {
        this.input = is;
        name = filename;
        mergeb = merge;
        freeb = free;
        graphb = graph;
    }

    public void run() {
        try {
            // apertura buffer in ingresso
            BufferedReader threadIn = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            // creazione nomefile e file

            String texname = ASPect.file_out_prefix + name + fn + ".tex";
            StringBuilder before = new StringBuilder();
            StringBuilder after = new StringBuilder();
            String line = null;
            String ls = System.getProperty("line.separator");
            if (freeb) {

                BufferedReader bereader = new BufferedReader(new FileReader("before.tex"));

                while ((line = bereader.readLine()) != null) {
                    before.append(line);
                    before.append(ls);
                }

//                String beline = bereader.readLine();
//                int bei = 0;
//                while (beline != null) {
//                    before[bei] = beline;
//                    bei = bei + 1;
//                    beline = bereader.readLine();
//                }

                BufferedReader afreader = new BufferedReader(new FileReader("after.tex"));

                while ((line = afreader.readLine()) != null) {
                    after.append(line);
                    after.append(ls);
                }
//                String afline = afreader.readLine();
//                int afi = 0;
//                while (afline != null) {
//                    after[afi] = afline;
//                    afi = afi + 1;
//                    afline = afreader.readLine();
//                }
            }

            // stringhe e array necessari alla conversione
            String tikz_coord = null;
            String[] coords = null;
            String tikz_final = null;
            String[] tikz_commands = null;
            String tikz_commandline = null;
            ArrayList<String> graph_links = new ArrayList<>();

            // lettura input
            tikz_commandline = threadIn.readLine();
            if (tikz_commandline == null){
                System.err.println("ASPECT atoms not found. Nothing to do !");
                System.err.println("Please check the output of the ASP solver.");
                System.exit(1);
            }

            while (tikz_commandline != null) {
                // creo file tex e buffer per scriverci
                texname = ASPect.file_out_prefix + name + fn + ".tex";
                File tex = new File(texname);
                FileWriter fw = new FileWriter(tex);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw);


                // divido gli atomi
                tikz_commands = tikz_commandline.split(" ");
                // order is useful in graph mode so when I print multiple
                // solutions of the same problem the layout is the same
                Arrays.sort(tikz_commands);

                // stampa header
                if (mergeb) {
                    out.println("\\begin{tikzpicture}"
                            + "\r\n");
                } else if (freeb) {
//                    assert before != null;
//                    for (String s : before) {
//                        out.println(s + "\r\n");
//                    }
                    out.println(before + ls);

                } else if (graphb) {
                    out.println("\\documentclass{article}\r\n"
                            + "\\usepackage{tikz}\r\n"
                            + "\\usepackage{graphicx}\r\n"
                            + "\\usetikzlibrary{graphs,quotes,graphdrawing}\r\n"
                            + "\\usegdlibrary{force}\r\n"                         
                            + "\r\n"
                            + "\\begin{document}\r\n"
                            + "\\section*{Answer:" + fn + "}\r\n"
                            + "\\begin{figure}[!h]\r\n"
                            + "\\centering\r\n"
                            + "\\resizebox{\\textwidth}{!}{%\r\n"
                            + "\\begin{tikzpicture}\r\n"
                            + "\\graph[spring layout, node distance=2cm and 2cm]{\r\n"
                            + "\r\n");
                } else {
                    out.println("\\documentclass{article}\r\n"
                            + "\\usepackage{tikz}\r\n"
                            + "\\usepackage{graphicx}\r\n"
                            + "\r\n"
                            + "\\begin{document}\r\n"
                            + "\\section*{Answer:" + fn + "}\r\n"
                            + "\\begin{figure}[!h]\r\n"
                            + "\\centering\r\n"
                            + "\\resizebox{\\textwidth}{!}{%\r\n"
                            + "\\begin{tikzpicture}"
                            + "\r\n");
                }

                // ciclo sugli atomi
                for (String tikz_command : tikz_commands) {
                    // stringbuilder per stampare le stringhe
                    StringBuilder tikz_tmp = new StringBuilder();

                
                    if (tikz_command.contains("aspect")) {
                        
                        // atomi in modalità graph
                        if (graphb) {

                            // traduzione nodi, scrittura comando tikz
                            if (tikz_command.contains("node")) {

                            String node_name = null;

                            if (tikz_command.contains("draw")) {

                                Pattern pattern = Pattern.compile("\\((.*?)\\)");
                                Matcher matcher = pattern.matcher(tikz_command);
                                if (matcher.find()) {
                                    node_name = matcher.group(1);
                                }

                                tikz_tmp.append("{");
                                tikz_tmp.append(node_name);
                                tikz_tmp.append("[minimum size=7mm, circle, draw]},");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                                // nodo colorato
                            } else if (tikz_command.contains("color")) {

                                String color = null;

                                Pattern pattern1 = Pattern.compile("\\((.*?),");
                                Matcher matcher1 = pattern1.matcher(tikz_command);

                                Pattern pattern2 = Pattern.compile(",(\\w+)\\)");
                                Matcher matcher2 = pattern2.matcher(tikz_command);
                                if (matcher1.find()) {
                                    node_name = matcher1.group(1);
                                }
                                if (matcher2.find()) {
                                    color = matcher2.group(1);
                                }

                                tikz_tmp.append("{");
                                tikz_tmp.append(node_name);
                                tikz_tmp.append("[fill=");
                                tikz_tmp.append(color);
                                tikz_tmp.append(", minimum size=7mm, circle, draw]");
                                tikz_tmp.append("}, ");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                                
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
                        // traduzione rettangoli e ottenimento coordinate, scrittura comando tikz
                        if (tikz_command.contains("rectangle")) {

                            tikz_coord = tikz_command.replaceAll("[^0-9]+", " ");
                            coords = tikz_coord.trim().split(" ");

                            if (tikz_command.contains("draw")) {
                                tikz_tmp.append("\\draw (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") rectangle (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);

                    // rettangolo colorato
                            } else if (tikz_command.contains("color")) {

                                String color = null;

                                Pattern pattern = Pattern.compile(",(\\w+)\\)");
                                Matcher matcher = pattern.matcher(tikz_command);
                                if (matcher.find()) {
                                    color = matcher.group(1);
                                }

                                tikz_tmp.append("\\draw [color=");
                                tikz_tmp.append(color);
                                tikz_tmp.append("] (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") rectangle (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);

                    // rettangolo con riempimento
                            } else if (tikz_command.contains("fill")) {

                                String color = null;
                                Pattern pattern = Pattern.compile(",(\\w+)\\)");
                                Matcher matcher = pattern.matcher(tikz_command);
                                if (matcher.find()) {
                                    color = matcher.group(1);
                                }


                                tikz_tmp.append("\\draw [fill=");
                                tikz_tmp.append(color);
                                tikz_tmp.append("] (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") rectangle (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                            }

                // traduzione cerchio e ottenimento coordinate, scrittura comando tikz
                        } else if (tikz_command.contains("circle")) {
                            tikz_coord = tikz_command.replaceAll("[^0-9]+", " ");
                            coords = tikz_coord.trim().split(" ");

                            if (tikz_command.contains("draw")) {
                                tikz_tmp.append("\\draw (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") circle (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);

                    // cerchio colorato
                            } else if (tikz_command.contains("color")) {

                                String color = null;

                                Pattern pattern2 = Pattern.compile(",(\\w+)\\)");
                                Matcher matcher2 = pattern2.matcher(tikz_command);
                                if (matcher2.find()) {
                                    color = matcher2.group(1);
                                }

                                tikz_tmp.append("\\draw [color=");
                                tikz_tmp.append(color);
                                tikz_tmp.append("] (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") circle (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);

                    // cerchio con riempimento
                            } else if (tikz_command.contains("fill")) {

                                String color = null;

                                Pattern pattern1 = Pattern.compile(",(\\w+)\\)");
                                Matcher matcher1 = pattern1.matcher(tikz_command);
                                if (matcher1.find()) {
                                    color = matcher1.group(1);
                                }

                                tikz_tmp.append("\\draw [fill=");
                                tikz_tmp.append(color);
                                tikz_tmp.append("] (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") circle (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                            }

                // traduzione ellisse e ottenimento coordinate, scrittura comando tikz
                        } else if (tikz_command.contains("ellipse")) {
                            tikz_coord = tikz_command.replaceAll("[^0-9]+", " ");
                            coords = tikz_coord.trim().split(" ");


                            if (tikz_command.contains("draw")) {
                                tikz_tmp.append("\\draw (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") ellipse (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(" and ");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                    // ellisse colorata
                            } else if (tikz_command.contains("color")) {

                                String color = null;

                                Pattern pattern3 = Pattern.compile(",(\\w+)\\)");
                                Matcher matcher3 = pattern3.matcher(tikz_command);
                                if (matcher3.find()) {
                                    color = matcher3.group(1);
                                }

                                tikz_tmp.append("\\draw [color=");
                                tikz_tmp.append(color);
                                tikz_tmp.append("] (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") ellipse (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(" and ");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);

                    // ellisse con riempimento
                            } else if (tikz_command.contains("fill")) {

                                String color = null;

                                Pattern pattern1 = Pattern.compile(",(\\w+)\\)");
                                Matcher matcher1 = pattern1.matcher(tikz_command);
                                if (matcher1.find()) {
                                    color = matcher1.group(1);
                                }

                                tikz_tmp.append("\\draw [fill=");
                                tikz_tmp.append(color);
                                tikz_tmp.append("] (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") ellipse (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(" and ");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                            }

                // traduzione triangoli	e ottenimento coordinate, scrittura comando tikz
                        } else if (tikz_command.contains("triangle")) {

                            tikz_coord = tikz_command.replaceAll("[^0-9]+", " ");
                            coords = tikz_coord.trim().split(" ");

                            if (tikz_command.contains("draw")) {

                                tikz_tmp.append("\\draw (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") -- (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(") -- (");
                                tikz_tmp.append(coords[4]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[5]);
                                tikz_tmp.append(") cycle;");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                    // triangolo colorato
                            } else if (tikz_command.contains("color")) {

                                String color = null;
                                Pattern pattern1 = Pattern.compile(",(\\w+)\\)");
                                Matcher matcher1 = pattern1.matcher(tikz_command);
                                if (matcher1.find()) {
                                    color = matcher1.group(1);
                                }

                                tikz_tmp.append("\\draw [color=");
                                tikz_tmp.append(color);
                                tikz_tmp.append("] (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") -- (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(") -- (");
                                tikz_tmp.append(coords[4]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[5]);
                                tikz_tmp.append(") cycle;");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);

                    // triangolo con riempimento
                            } else if (tikz_command.contains("fill")) {

                                String color = null;
                                Pattern pattern = Pattern.compile(",(\\w+)\\)");
                                Matcher matcher = pattern.matcher(tikz_command);
                                if (matcher.find()) {
                                    color = matcher.group(1);
                                }


                                tikz_tmp.append("\\draw [fill=");
                                tikz_tmp.append(color);
                                tikz_tmp.append("] (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") -- (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(") -- (");
                                tikz_tmp.append(coords[4]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[5]);
                                tikz_tmp.append(") cycle;");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                            }

                // traduzione nodi e ottenimento coordinate, scrittura comando tikz
                        } else if (tikz_command.contains("node")) {
                            tikz_coord = tikz_command.replaceAll("[^0-9]+", " ");
                            coords = tikz_coord.trim().split(" ");
                            String node_type = null;
                            Pattern pattern = Pattern.compile("\"(.*?)\"");
                            Matcher matcher = pattern.matcher(tikz_command);
                            if (matcher.find()) {
                                node_type = matcher.group(1);
                            }
                            // TODO: maybe rename this, I'm using coords and node_type for the text printed on the node
                            if (node_type == null){
                                node_type = coords[2];
                            }
                            if (tikz_command.contains("draw")) {
                                tikz_tmp.append("\\draw (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") node {\\LARGE");
                                tikz_tmp.append(node_type);
                                tikz_tmp.append("};");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                    // nodo colorato
                            } else if (tikz_command.contains("color")) {

                                String color = null;

                                Pattern pattern1 = Pattern.compile(",(\\w+)\\)");
                                Matcher matcher1 = pattern1.matcher(tikz_command);
                                if (matcher1.find()) {
                                    color = matcher1.group(1);
                                }

                                tikz_tmp.append("\\draw [color=");
                                tikz_tmp.append(color);
                                tikz_tmp.append("] (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") node {\\LARGE");
                                tikz_tmp.append(node_type);
                                tikz_tmp.append("};");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                            // TODO: maybe rename this, I'm using coords[2] for width
                            } else if (tikz_command.contains("image")) {
                                tikz_tmp.append("\\node [inner sep=0pt] (img) at (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") {\\includegraphics[width=");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append("px]{");
                                tikz_tmp.append(node_type);
                                tikz_tmp.append("}};");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                            }

                // traduzione linee e ottenimento coordinate, scrittura comando tikz
                        } else if (tikz_command.contains("line")) {
                            tikz_coord = tikz_command.replaceAll("[^0-9]+", " ");
                            coords = tikz_coord.trim().split(" ");
                            if (tikz_command.contains("draw")) {

                                tikz_tmp.append("\\draw (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") -- (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);

                    // linee colorate
                            } else if (tikz_command.contains("color")) {

                                String color = null;
                                Pattern pattern = Pattern.compile(",(\\w+)\\)");
                                Matcher matcher = pattern.matcher(tikz_command);
                                if (matcher.find()) {
                                    color = matcher.group(1);
                                }


                                tikz_tmp.append("\\draw [color=");
                                tikz_tmp.append(color);
                                tikz_tmp.append("] (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") -- (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                            }

                // traduzione archi e ottenimento coordinate, scrittura comando tikz
                        } else if (tikz_command.contains("arc")) {
                            tikz_coord = tikz_command.replaceAll("[^0-9]+", " ");
                            coords = tikz_coord.trim().split(" ");

                            if (tikz_command.contains("draw")) {
                                tikz_tmp.append("\\draw (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") arc (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(":");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(":");
                                tikz_tmp.append(coords[4]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                    // arco colorato
                            } else if (tikz_command.contains("color")) {

                                String color = null;

                                Pattern pattern1 = Pattern.compile(",(\\w+)\\)");
                                Matcher matcher1 = pattern1.matcher(tikz_command);
                                if (matcher1.find()) {
                                    color = matcher1.group(1);
                                }

                                tikz_tmp.append("\\draw [color=");
                                tikz_tmp.append(color);
                                tikz_tmp.append("] (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") arc (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(":");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(":");
                                tikz_tmp.append(coords[4]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);
                            }

                // traduzione frecce e ottenimento coordinate, scrittura comando tikz
                        } else if (tikz_command.contains("arrow")) {
                            tikz_coord = tikz_command.replaceAll("[^0-9]+", " ");
                            coords = tikz_coord.trim().split(" ");
                            if (tikz_command.contains("draw")) {

                                tikz_tmp.append("\\draw [->] (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") -- (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);

                    // frecce colorate
                            } else if (tikz_command.contains("color")) {

                                String color = null;
                                Pattern pattern = Pattern.compile(",(\\w+)\\)");
                                Matcher matcher = pattern.matcher(tikz_command);
                                if (matcher.find()) {
                                    color = matcher.group(1);
                                }


                                tikz_tmp.append("\\draw [->] [color=");
                                tikz_tmp.append(color);
                                tikz_tmp.append("] (");
                                tikz_tmp.append(coords[0]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[1]);
                                tikz_tmp.append(") -- (");
                                tikz_tmp.append(coords[2]);
                                tikz_tmp.append(",");
                                tikz_tmp.append(coords[3]);
                                tikz_tmp.append(");");
                                tikz_final = tikz_tmp.toString();
                                out.println(tikz_final);

                                }
                            }
                        }
                    }
                }

                // stampa footer documento
                if (mergeb) {
                    out.println("\r\n"
                            + "\\end{tikzpicture}\r\n");

                } else if (freeb) {
//                    assert after != null;
//                    for (String s : after) {
//                        out.println(s + "\r\n");
//                    }
                    out.println(after + ls);
                } else if (graphb) {

                    for (String tikz_command : graph_links) {
                        // stringbuilder per stampare le stringhe
                        StringBuilder tikz_tmp = new StringBuilder();                                                          
    
                        // traduzione connessioni, scrittura comando tikz
                            if (tikz_command.contains("line")) {
                                
                                    String node1 = null;
                                    String node2 = null;

                                if (tikz_command.contains("draw")) {
    
                                    Pattern pattern1 = Pattern.compile("\\((.*?),");
                                    Matcher matcher1 = pattern1.matcher(tikz_command);
    
                                    Pattern pattern2 = Pattern.compile(",(.*?)\\)");
                                    Matcher matcher2 = pattern2.matcher(tikz_command);
    
                                    if (matcher1.find()) {
                                        node1 = matcher1.group(1);
                                    }
                                    if (matcher2.find()) {
                                        node2 = matcher2.group(1);
                                    }
    
                                    tikz_tmp.append("{");
                                    tikz_tmp.append(node1);
                                    tikz_tmp.append("--");
                                    tikz_tmp.append(node2);
                                    tikz_tmp.append("}, ");
                                    tikz_final = tikz_tmp.toString();
                                    out.println(tikz_final);
    
                                    // linee etichettate
                                } else if (tikz_command.contains("quote")) {
                                    String quote = null;
    
                                    Pattern pattern1 = Pattern.compile("\\((.*?),");
                                    Matcher matcher1 = pattern1.matcher(tikz_command);
    
                                    Pattern pattern2 = Pattern.compile(",(.*?),");
                                    Matcher matcher2 = pattern2.matcher(tikz_command);
    
                                    Pattern pattern = Pattern.compile("\"(.*?)\"");
                                    Matcher matcher = pattern.matcher(tikz_command);
    
    
                                    if (matcher1.find()) {
                                        node1 = matcher1.group(1);
                                    }
                                    if (matcher2.find()) {
                                        node2 = matcher2.group(1);
                                    }
                                    if (matcher.find()) {
                                        quote = matcher.group(1);
                                    }
    
                                    tikz_tmp.append("{");
                                    tikz_tmp.append(node1);
                                    tikz_tmp.append("--[\"");
                                    tikz_tmp.append(quote);
                                    tikz_tmp.append("\"]");
                                    tikz_tmp.append(node2);
                                    tikz_tmp.append("}, ");
                                    tikz_final = tikz_tmp.toString();
                                    out.println(tikz_final);
                                    }
                                // traduzione frecce, scrittura comando tikz
                                } else if (tikz_command.contains("arrow")) {
    
                                    String node1 = null;
                                    String node2 = null;
    
                                    if (tikz_command.contains("draw")) {
    
                                        Pattern pattern1 = Pattern.compile("\\((.*?),");
                                        Matcher matcher1 = pattern1.matcher(tikz_command);
    
                                        Pattern pattern2 = Pattern.compile(",(.*?)\\)");
                                        Matcher matcher2 = pattern2.matcher(tikz_command);
    
                                        if (matcher1.find()) {
                                            node1 = matcher1.group(1);
                                        }
                                        if (matcher2.find()) {
                                            node2 = matcher2.group(1);
                                        }
    
                                        tikz_tmp.append("{");
                                        tikz_tmp.append(node1);
                                        tikz_tmp.append("->");
                                        tikz_tmp.append(node2);
                                        tikz_tmp.append("}, ");
                                        tikz_final = tikz_tmp.toString();
                                        out.println(tikz_final);
    
                                        // linee etichettate
                                    } else if (tikz_command.contains("quote")) {
                                        String quote = null;
    
                                        Pattern pattern1 = Pattern.compile("\\((.*?),");
                                        Matcher matcher1 = pattern1.matcher(tikz_command);
    
                                        Pattern pattern2 = Pattern.compile(",(.*?),");
                                        Matcher matcher2 = pattern2.matcher(tikz_command);
    
                                        Pattern pattern = Pattern.compile("\"(.*?)\"");
                                        Matcher matcher = pattern.matcher(tikz_command);
    
    
                                        if (matcher1.find()) {
                                            node1 = matcher1.group(1);
                                        }
                                        if (matcher2.find()) {
                                            node2 = matcher2.group(1);
                                        }
                                        if (matcher.find()) {
                                            quote = matcher.group(1);
                                        }
    
                                        tikz_tmp.append("{");
                                        tikz_tmp.append(node1);
                                        tikz_tmp.append("->[\"");
                                        tikz_tmp.append(quote);
                                        tikz_tmp.append("\"] ");
                                        tikz_tmp.append(node2);
                                        tikz_tmp.append("}, ");
                                        tikz_final = tikz_tmp.toString();
                                        out.println(tikz_final);
                                    }
                            }
                    }
                                
                            





                    out.println("\r\n"
                            + "};\r\n"
                            + "\\end{tikzpicture}\r\n"
                            + "}\r\n"
                            + "\\end{figure}\r\n"
                            + "\\end{document}\r\n");
                } else {
                    out.println("\r\n"
                            + "\\end{tikzpicture}\r\n"
                            + "}\r\n"
                            + "\\end{figure}\r\n"
                            + "\\end{document}\r\n");
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
                if (!mergeb && !freeb) {
                    ProcessBuilder processBuilderPDF = new ProcessBuilder();
                    if (graphb) {
                        processBuilderPDF.command("lualatex", "-halt-on-error", texname);
                    }
                    else {
                        processBuilderPDF.command("pdflatex", "-halt-on-error", texname);
                    }
                    processBuilderPDF.redirectOutput(ProcessBuilder.Redirect.PIPE);
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
        }
    }
}
