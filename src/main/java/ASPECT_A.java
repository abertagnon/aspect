import java.io.*;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.*;
public class ASPECT_A {
    public static String file_out_prefix = "aspect_out_";

    public static void main(String[] args) {
        // System.out.println("***** ASPECT START *****");
        System.out.println("\n###############################################################");
        String asciiTitle =
                "                  _   ___ ___ ___ ___ _____ \n" +
                "                 /_\\ / __| _ \\ __/ __|_   _|\n" +
                "                / _ \\\\__ \\  _/ _| (__  | |  \n" +
                "               /_/ \\_\\___/_| |___\\___| |_|  \n";
        System.out.println(asciiTitle);
        System.out.println(" ASPECT: Answer Set rePresentation as vEctor graphiCs in laTex ");
        System.out.println("        https://github.com/abertagnon/aspect -- v0.1.3a        ");
        String yellowColor = "\u001B[33m";
        String resetColor = "\u001B[0m";
        String debugText = " ************** TEST ONLY -- NOT FOR PRODUCTION ************** ";
        System.out.println(yellowColor + debugText + resetColor);
        System.out.println("###############################################################\n");
        Options options = new Options();

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
        String resizeFactor = "5";
        String speedFactor = "5";

        List<String> argList = null;
        String name = null;

        try {
            CommandLine cmd = parser.parse(options, args);
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
            // all other arguments
            argList = cmd.getArgList();
            if (argList.size() < 1) {
                throw new ParseException("*** ASPECT ERROR: At least one input file is required.");
            }
            String name0 = argList.get(argList.size() - 1);
            String name_with_extension = Paths.get(name0).getFileName().toString();
            name = name_with_extension.substring(0, name_with_extension.lastIndexOf("."));

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("ASPECT [options] [(opt.) clingo arguments] [input1 [input2 [input3] ...]]", options);
            System.exit(1);
        }

        String arguments = String.join(" ", argList);
        // creazione pipe per i due thread, chiamate ai thread
        try {
            PipedOutputStream os = new PipedOutputStream();
            PipedInputStream is = new PipedInputStream();
            os.connect(is);
            ClingoTh cl = new ClingoTh(os, arguments);
            TexPdfThNew tp = new TexPdfThNew(is, name, false, merge, free, graph);

            Thread clingo = new Thread(cl);
            Thread texpdf = new Thread(tp);

            clingo.start();
            texpdf.start();
            clingo.join();
            texpdf.join();

            if (merge || free) {
                int filenumber;
                filenumber = TexPdfThNew.fn; // associare numero di file da thread texpdf
                String mergedname = file_out_prefix + name + "_final.tex";
                File merged = new File(mergedname);
                FileWriter fw = new FileWriter(merged);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw);
                String ls = System.getProperty("line.separator");
                out.println("\\documentclass{beamer}" + ls
                        + "\\usepackage{tikz}" + ls
                        + "\\usepackage{graphicx}" + ls);
                if(graph) {
                    out.println("\\usetikzlibrary{graphs,quotes,graphdrawing}" + ls
                            + "\\usegdlibrary{force}" + ls);
                }
                out.println("\\begin{document}" + ls);
                for (int j = 1; j < filenumber; j++) {
                    if (merge) {
                        out.println("\\begin{frame}" + ls
                                + "\\resizebox{" + resizeFactor + "em}{" + resizeFactor + "em}{" + ls
                                + "\\input{" + file_out_prefix + name + "_" + j + "}" + ls
                                + "}" + ls
                                + "\\end{frame}" + ls
                        );
                    } else {
                        out.println("\\input{" + file_out_prefix + name + "_" + j + "}" + ls);
                    }
                }
                out.println("\\end{document}");
                out.flush();
                bw.flush();
                fw.flush();

                out.close();
                bw.close();
                fw.close();

                System.out.println("File created: " + mergedname);

                ProcessBuilder processBuilderPDF = new ProcessBuilder();
                if(graph) {
                    processBuilderPDF.command("lualatex", "-halt-on-error", mergedname);
                }
                else {
                    processBuilderPDF.command("pdflatex", "-halt-on-error", mergedname);
                }
                processBuilderPDF.redirectOutput(ProcessBuilder.Redirect.PIPE);
                Process mpdf = processBuilderPDF.start();

                String mpdfname = mergedname.substring(0, mergedname.lastIndexOf(".")) + ".pdf";
                System.out.println("MERGE|FREE: Building... " + mpdfname + "\r");
                mpdf.waitFor();

                if(mpdf.exitValue() != 0){
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

                System.out.println("MERGE|FREE: File created: " + mpdfname + "\r");
            }

            if (animate) {
                int filenumber = TexPdfThNew.fn - 1;
                if (filenumber == 0) filenumber = 1;
                String strfn = String.valueOf(filenumber);
                String animatedname = file_out_prefix + name + "_animation.tex";
                File animated = new File(animatedname);
                FileWriter fw = new FileWriter(animated);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw);
                String ls = System.getProperty("line.separator");
                out.println("\\documentclass{beamer}" + ls
                        + "\\usepackage{animate}" + ls
                        + "\\begin{document}" + ls
                        + "\\begin{frame}" + ls
                        + "\\frametitle{Frame title}" + ls
                        + "\\framesubtitle{Frame subtitle}" + ls
                        + "\\begin{center}" + ls
                        + "\\animategraphics[autoplay, loop, width=\\linewidth]{" + speedFactor + "}{" + file_out_prefix + name + "_}{1}{" + strfn +"}" + ls
                        + "\\end{center}" + ls
                        + "\\end{frame}" + ls
                        + "\\end{document}");
                out.flush();
                bw.flush();
                fw.flush();

                out.close();
                bw.close();
                fw.close();

                System.out.println("File created: " + animatedname);
                ProcessBuilder processBuilderPDF = new ProcessBuilder();
                processBuilderPDF.command("pdflatex", "-halt-on-error", animatedname);
                processBuilderPDF.redirectOutput(ProcessBuilder.Redirect.PIPE);
                Process apdf = processBuilderPDF.start();

                String apdfname = animatedname.substring(0, animatedname.lastIndexOf(".")) + ".pdf";
                System.out.println("ANIMATE: Building... " + apdfname + "\r");
                apdf.waitFor();

                if(apdf.exitValue() != 0){
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

                System.out.println("ANIMATE: File created: " + apdfname + "\r");
            }

            

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("***** ASPECT END *****");
    }
}
