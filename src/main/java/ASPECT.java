import java.io.*;
import org.apache.commons.cli.*;

public class ASPECT {

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
        System.out.println("        https://github.com/abertagnon/aspect -- v0.1.3         ");
        // String yellowColor = "\u001B[33m";
        // String resetColor = "\u001B[0m";
        // String debugText = " ************** TEST ONLY -- NOT FOR PRODUCTION ************** ";
        // System.out.println(yellowColor + debugText + resetColor);
        System.out.println("###############################################################" + ls);
        Options options = new Options();

        options.addOption(Option.builder("o").longOpt("output")
                .desc("set name for output file")
                .hasArg(true)
                .required(false)
                .build());
        options.addOption(Option.builder("v").longOpt("verbose")
                .desc("verbose output (shows on the screen all ASPECT atomic formulas received as input)")
                .hasArg(false)
                .required(false)
                .build());
        options.addOption(Option.builder("nobuild")
                .desc("disables automatic building (via pdflatex or lualatex) of the files created")
                .hasArg(false)
                .required(false)
                .build());
        options.addOption(Option.builder("h").longOpt("help")
                .desc("shows ASPECT command line options")
                .hasArg(false)
                .required(false)
                .build());

        // merge mode
        options.addOption(Option.builder("b").longOpt("beamer")
                .desc("enable beamer mode")
                .hasArg(false)
                .required(false)
                .build());
        options.addOption(Option.builder("r").longOpt("resize")
                .desc("set resize parameter for beamer mode, % of \\textwidth, from 0 to 1 (default 1)")
                .hasArg(true)
                .required(false)
                .build());

        // free mode
        options.addOption(Option.builder("f").longOpt("free")
                .desc("enable free mode")
                .hasArg(false)
                .required(false)
                .build());
        options.addOption(Option.builder("before")
                .desc("set before file used in free mode (default before.tex)")
                .hasArg(true)
                .required(false)
                .build());
        options.addOption(Option.builder("after")
                .desc("set after file used in free mode (default after.tex)")
                .hasArg(true)
                .required(false)
                .build());

        // animate mode
        options.addOption(Option.builder("a").longOpt("animate")
                .desc("enable animate mode")
                .hasArg(false)
                .required(false)
                .build());
        options.addOption(Option.builder("s").longOpt("speed")
                .desc("set speed parameter for animate mode (default 1)")
                .hasArg(true)
                .required(false)
                .build());

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        boolean merge = false;
        boolean free = false;
        boolean animate = false;
        boolean verbose = false;

        String resizeFactor = "1";
        String speedFactor = "1";
        String outputFileName = "aspect_output";

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                formatter.printHelp("ASPECT [options]", options);
                System.exit(1);
            }

            if(cmd.hasOption("o")) {
                outputFileName = cmd.getOptionValue("o");
            }
            if(cmd.hasOption("v")) {
                verbose = true;
            }
            if(cmd.hasOption("nobuild")) {
                TexPdfTh.pdfBuild = false;
            }

            if (cmd.hasOption("b")) {
                merge = true;
                if (cmd.hasOption("r")) {
                    resizeFactor = cmd.getOptionValue("r");
                }
            }

            if (cmd.hasOption("f")) {
                free = true;
                if (cmd.hasOption("before")) {
                    TexPdfTh.beforeFilename = cmd.getOptionValue("before");
                }
                if (cmd.hasOption("after")) {
                    TexPdfTh.afterFilename = cmd.getOptionValue("after");
                }
            }

            if (cmd.hasOption("a")) {
                animate = true;
                if (cmd.hasOption("s")) {
                    speedFactor = cmd.getOptionValue("s");
                }
            }

            if (merge && animate) {
                throw new ParseException("***> ASPECT ERROR: animate and beamer mode cannot be used together.");
            }

            if(cmd.hasOption("r") && !merge){
                System.err.println("***> ASPECT WARNING: resize option only used in beamer mode. I'll ignore it.");
            }
            if(cmd.hasOption("s") && !animate){
                System.err.println("***> ASPECT WARNING: speed option only used in animate mode. I'll ignore it.");
            }
            if(cmd.hasOption("before") && !free){
                System.err.println("***> ASPECT WARNING: before option only used in free mode. I'll ignore it.");
            }
            if(cmd.hasOption("after") && !free){
                System.err.println("***> ASPECT WARNING: after option only used in free mode. I'll ignore it.");
            }

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("ASPECT [options]", options);
            System.exit(1);
        }

        try {
            TexPdfTh tp = new TexPdfTh(System.in, outputFileName, verbose, merge, free, resizeFactor);
            Thread texpdf = new Thread(tp);

            texpdf.start();
            texpdf.join();

            if (merge) {
                int fileNumber = TexPdfTh.files.isEmpty() ? 0 : TexPdfTh.files.lastKey();
                if (fileNumber != 0) {
                    int frameNumber = TexPdfTh.files.get(fileNumber);
                    String mergedFilename = outputFileName + "_merged.tex";
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(mergedFilename)));

                    out.println("\\documentclass{beamer}" + ls
                            + "\\usepackage{tikz}" + ls
                            + "\\usepackage{graphicx}" + ls
                            + "\\usepackage{xcolor}");

                    if (TexPdfTh.graph) {
                        out.println("\\usetikzlibrary{graphs,quotes,graphdrawing}" + ls
                                + "\\usegdlibrary{force}" + ls);
                    }

                    out.println("\\begin{document}" + ls);

                    for (int j = 1; j <= fileNumber; j++)
                        if (frameNumber == 1) out.println("\\input{" + outputFileName + "_" + j + "}" + ls);
                        else for (int i = 1; i <= frameNumber; i++)
                            out.println("\\input{" + outputFileName + "_" + j + "_" + i + "}" + ls);

                    out.println("\\end{document}");

                    out.flush();
                    out.close();

                    System.out.println("+++> ASPECT: File created " + mergedFilename);
                    TexPdfTh.buildLatex(mergedFilename);
                }
            }

            if (animate) {
                int fileNumber = TexPdfTh.files.isEmpty() ? 0 : TexPdfTh.files.lastKey();
                if (fileNumber != 0) {
                    for (int j = 1; j <= fileNumber; j++) {
                        int frameNumber = TexPdfTh.files.get(j);
                        String animatedFilename = outputFileName + "_" + fileNumber + "_animated.tex";
                        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(animatedFilename)));

                        out.println("\\documentclass{standalone}" + ls
                                + "\\usepackage{animate}" + ls
                                + "\\usepackage{graphicx}" + ls
                                + "\\begin{document}" + ls
                                + "\\animategraphics[autoplay, loop]{" + speedFactor + "}{" + outputFileName + "_" + fileNumber + "_}{1}{" + frameNumber + "}" + ls
                                + "\\end{document}");

                        out.flush();
                        out.close();

                        System.out.println("+++> ASPECT: File created " + animatedFilename);
                        TexPdfTh.buildLatex(animatedFilename);
                    }
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
