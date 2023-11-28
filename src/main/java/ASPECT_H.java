import java.io.*;
import org.apache.commons.cli.*;

public class ASPECT_H {

    public static void main(String[] args) {
        String ls = System.getProperty("line.separator");
        System.out.println("\n###############################################################");
        String asciiTitle =
                "                  _   ___ ___ ___ ___ _____ " + ls +
                "                 /_\\ / __| _ \\ __/ __|_   _|" + ls +
                "                / _ \\\\__ \\  _/ _| (__  | |  " + ls +
                "               /_/ \\_\\___/_| |___\\___| |_|  " + ls;
        System.out.println(asciiTitle);
        System.out.println(" ASPECT: Answer Set rePresentation as vEctor graphiCs in laTex ");
        System.out.println("        https://github.com/abertagnon/aspect -- v0.1.3a        ");
        String yellowColor = "\u001B[33m";
        String resetColor = "\u001B[0m";
        String debugText = " ************** TEST ONLY -- NOT FOR PRODUCTION ************** ";
        System.out.println(yellowColor + debugText + resetColor);
        System.out.println("###############################################################" + ls);
        Options options = new Options();

        options.addOption(Option.builder("o").longOpt("output")
                .desc("set name for output file")
                .hasArg(true)
                .required(false)
                .build());
        options.addOption(Option.builder("v").longOpt("verbose")
                .desc("enable verbose mode (shows on the screen all ASPECT atomic formulas received as input)")
                .hasArg(false)
                .required(false)
                .build());

        // merge mode
        options.addOption(Option.builder("m").longOpt("merge")
                .desc("enable merge mode")
                .hasArg(false)
                .required(false)
                .build());
        // free mode
        options.addOption(Option.builder("f").longOpt("free")
                .desc("enable free mode")
                .hasArg(false)
                .required(false)
                .build());
        // graph mode
        options.addOption(Option.builder("g").longOpt("graph")
                .desc("enable graph mode")
                .hasArg(false)
                .required(false)
                .build());
        // resize option
        options.addOption(Option.builder("r").longOpt("resize")
                .desc("set resize parameter for merge mode (default 5)")
                .hasArg(true)
                .required(false)
                .build());
        // animate mode
        options.addOption(Option.builder("a").longOpt("animate")
                .desc("enable animate mode")
                .hasArg(false)
                .required(false)
                .build());
        // speed option
        options.addOption(Option.builder("s").longOpt("speed")
                .desc("set speed parameter for animate mode (default 5)")
                .hasArg(true)
                .required(false)
                .build());

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        boolean merge = false;
        boolean free = false;
        boolean graph = false;
        boolean animate = false;
        boolean verbose = false;
        String resizeFactor = "5";
        String speedFactor = "5";
        String outputFileName = "aspect_output";

        try {
            CommandLine cmd = parser.parse(options, args);

            if(cmd.hasOption("o")) {
                outputFileName = cmd.getOptionValue("o");
            }
            if(cmd.hasOption("v")) {
                verbose = true;
            }

            if (cmd.hasOption("m")) {
                merge = true;
                if (cmd.hasOption("r")) {
                    resizeFactor = cmd.getOptionValue("r");
                }
            }
            if (cmd.hasOption("f")) {
                free = true;
            }
            if (cmd.hasOption("g")) {
                graph = true;
            }
            if (cmd.hasOption("a")) {
                animate = true;
                if (cmd.hasOption("s")) {
                    speedFactor = cmd.getOptionValue("s");
                }
            }
            
            // Special conditions on options
            if(merge && free){
                throw new ParseException("*** ASPECT ERROR: Merge mode and Free mode cannot be used together.");
            }
            if(cmd.hasOption("r") && !merge){
                System.err.println("*** ASPECT WARNING: Resize option only used in merge mode. I'll ignore it.");
            }
            if(cmd.hasOption("s") && !animate){
                System.err.println("*** ASPECT WARNING: Speed option only used in animate mode. I'll ignore it.");
            }

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("ASPECT [options]", options);
            System.exit(1);
        }

        try {
            TexPdfThNew tp = new TexPdfThNew(System.in, outputFileName, verbose, merge, free, graph);
            Thread texpdf = new Thread(tp);

            texpdf.start();
            texpdf.join();

            if (merge || free) {
                int filenumber;
                filenumber = TexPdfThNew.fn; // associare numero di file da thread texpdf
                if (filenumber != 0) {
                    String mergedname = outputFileName + "_merged.tex";
                    File merged = new File(mergedname);
                    FileWriter fw = new FileWriter(merged);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw);
                    out.println("\\documentclass{beamer}" + ls
                            + "\\usepackage{tikz}" + ls
                            + "\\usepackage{graphicx}" + ls);
                    if (graph) {
                        out.println("\\usetikzlibrary{graphs,quotes,graphdrawing}" + ls
                                + "\\usegdlibrary{force}" + ls);
                    }
                    out.println("\\begin{document}" + ls);
                    for (int j = 1; j <= filenumber; j++) {
                        if (merge) {
                            out.println("\\begin{frame}" + ls
                                    + "\\resizebox{" + resizeFactor + "em}{" + resizeFactor + "em}{" + ls
                                    + "\\input{" + outputFileName + "_" + j + "}" + ls
                                    + "}" + ls
                                    + "\\end{frame}" + ls
                            );
                        } else {
                            out.println("\\input{" + outputFileName + "_" + j + "}" + ls);
                        }
                    }
                    out.println("\\end{document}");
                    out.flush();
                    bw.flush();
                    fw.flush();

                    out.close();
                    bw.close();
                    fw.close();

                    System.out.println("+++> ASPECT (MERGE|FREE): File created " + mergedname);

                    ProcessBuilder processBuilderPDF = new ProcessBuilder();
                    if (graph) {
                        processBuilderPDF.command("lualatex", "-halt-on-error", mergedname);
                    } else {
                        processBuilderPDF.command("pdflatex", "-halt-on-error", mergedname);
                    }
                    processBuilderPDF.redirectOutput(ProcessBuilder.Redirect.PIPE);
                    Process mpdf = processBuilderPDF.start();

                    String mpdfname = mergedname.substring(0, mergedname.lastIndexOf(".")) + ".pdf";
                    System.out.println("+++> ASPECT (MERGE|FREE): Building " + mpdfname);
                    mpdf.waitFor();

                    if (mpdf.exitValue() != 0) {
                        InputStream in = mpdf.getInputStream();
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
                    System.out.println("+++> ASPECT (MERGE|FREE): File created " + mpdfname + ls);
                }
            }

            if (animate) {
                int filenumber = TexPdfThNew.fn;
                if (filenumber != 0) {
                    String strfn = String.valueOf(filenumber);
                    String animatedname = outputFileName + "_animated.tex";
                    File animated = new File(animatedname);
                    FileWriter fw = new FileWriter(animated);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw);
                    out.println("\\documentclass{beamer}" + ls
                            + "\\usepackage{animate}" + ls
                            + "\\begin{document}" + ls
                            + "\\begin{frame}" + ls
                            + "\\frametitle{Frame title}" + ls
                            + "\\framesubtitle{Frame subtitle}" + ls
                            + "\\begin{center}" + ls
                            + "\\animategraphics[autoplay, loop, width=\\linewidth]{" + speedFactor + "}{" + outputFileName + "_}{1}{" + strfn + "}" + ls
                            + "\\end{center}" + ls
                            + "\\end{frame}" + ls
                            + "\\end{document}");
                    out.flush();
                    bw.flush();
                    fw.flush();

                    out.close();
                    bw.close();
                    fw.close();

                    System.out.println("+++> ASPECT (ANIMATE): File created " + animatedname);
                    ProcessBuilder processBuilderPDF = new ProcessBuilder();
                    processBuilderPDF.command("pdflatex", "-halt-on-error", animatedname);
                    processBuilderPDF.redirectOutput(ProcessBuilder.Redirect.PIPE);
                    Process apdf = processBuilderPDF.start();

                    String apdfname = animatedname.substring(0, animatedname.lastIndexOf(".")) + ".pdf";
                    System.out.println("+++> ASPECT (ANIMATE): Building " + apdfname);
                    apdf.waitFor();

                    if (apdf.exitValue() != 0) {
                        InputStream in = apdf.getInputStream();
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
                    System.out.println("+++> ASPECT (ANIMATE): File created " + apdfname + ls);
                }
            }

            

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("###############################################################");
        System.out.println("                   ASPECT END  - Goodbye!                      ");
        System.out.println("###############################################################");
    }
}
