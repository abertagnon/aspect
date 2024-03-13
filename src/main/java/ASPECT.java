import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.List;

import org.apache.commons.cli.*;
import javax.imageio.ImageIO;

public class ASPECT {

    public static void main(String[] args) {
        String ls = System.lineSeparator();
        System.out.println("\n###############################################################");
        String asciiTitle =
                "                  _   ___ ___ ___ ___ _____ " + ls +
                "                 /_\\ / __| _ \\ __/ __|_   _|" + ls +
                "                / _ \\\\__ \\  _/ _| (__  | |  " + ls +
                "               /_/ \\_\\___/_| |___\\___| |_|  " + ls;
        System.out.println(asciiTitle);
        System.out.println(" ASPECT: Answer Set rePresentation as vEctor graphiCs in laTex ");
        System.out.println("        https://github.com/abertagnon/aspect -- v0.1.4         ");
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
        options.addOption(Option.builder("k").longOpt("keep")
                .desc("maintains logs and auxiliary files produced by pdflatex or lualatex")
                .hasArg(false)
                .required(false)
                .build());
        options.addOption(Option.builder("c").longOpt("convert")
                .desc("converts PDF to PNG or JPEG file")
                .hasArg(true)
                .required(false)
                .build());
        options.addOption(Option.builder("cc").longOpt("copyclipboard")
                .desc("converts PDF to image and copies it to the clipboard (default PNG)")
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
        boolean keepTemporaryFiles = false;

        /* PDF to Image conversion */
        boolean generateOutputImage = false;
        int outputImageDPI = 1200;
        String outputImageFormat = "png";
        boolean copyToClipboard = false;
        /* ----------------------- */

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
            if(cmd.hasOption("k")) {
                keepTemporaryFiles = true;
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

            if (cmd.hasOption("c") || cmd.hasOption("cc")) {
                if (merge || animate) {
                    System.err.println("***> ASPECT WARNING: image option cannot be used together with animate and/or beamer. I'll ignore it.");
                }
                generateOutputImage = true;
                if (cmd.hasOption("cc")) {
                    copyToClipboard = true;
                }
                if (cmd.hasOption("c")) {
                    String format = cmd.getOptionValue("c");
                    if (Objects.equals(format, "PNG") || Objects.equals(format, "JPEG")) {
                        outputImageFormat = format.toLowerCase();
                    }
                    else {
                        throw new ParseException("***> ASPECT ERROR: allowed image formats are only PNG or JPEG.");
                    }
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

            TexPdfTh.buildDirectory = outputFileName + "_aux";

            File directory = new File(TexPdfTh.buildDirectory);
            if (!directory.exists()) {
                boolean success = directory.mkdir();
                if (!success) {
                    System.err.println("***> ASPECT ERROR: Can't create directory for logs and auxiliary files");
                    System.exit(1);
                }
            }
            else {
                deleteDirectory(Paths.get(TexPdfTh.buildDirectory));
            }

            texpdf.start();
            texpdf.join();

            if (merge) {
                int fileNumber = TexPdfTh.files.isEmpty() ? 0 : TexPdfTh.files.lastKey();
                if (fileNumber != 0) {
                    int frameNumber = TexPdfTh.files.get(fileNumber);
                    String mergedFilename = outputFileName + "_merged.tex";
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(mergedFilename)));

                    out.println("\\documentclass[dvipsnames]{beamer}" + ls
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

            Path directorySource = Paths.get(TexPdfTh.buildDirectory);
            Path currentDirectory = Paths.get("").toAbsolutePath();

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directorySource, "*.pdf")) {
                Path lastFileCopied = null;

                List<Path> files = new ArrayList<>();
                for (Path file : stream) {
                    files.add(file);
                }

                files.sort(Comparator.comparing(Path::getFileName));

                for (Path file : files) {
                    Path destinationFile = currentDirectory.resolve(file.getFileName());
                    Files.copy(file, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                    lastFileCopied = file;

                    if (generateOutputImage) {
                        ProcessBuilder processBuilderImage = new ProcessBuilder();
                        String filename = file.getFileName().toString();
                        String imagename = filename.substring(0, filename.lastIndexOf(".")) + "." + outputImageFormat;
                        processBuilderImage.command("convert", "-density", Integer.toString(outputImageDPI), "-background", "white", "-flatten", filename, imagename);

                        Process imageProcess = processBuilderImage.start();
                        System.out.println("+++> ASPECT: Converting image " + imagename + " (this may take some time, please wait)");
                        imageProcess.waitFor();

                        if (imageProcess.exitValue() != 0) {
                            InputStream in = imageProcess.getInputStream();
                            if (in.available() > 0) {
                                BufferedReader errread = new BufferedReader(new InputStreamReader(in));
                                String error = errread.readLine();
                                do {
                                    System.err.println(error);
                                    error = errread.readLine();
                                } while (error != null);
                                System.exit(1);
                            }
                            in.close();
                        }
                        System.out.println("+++> ASPECT: File created " + imagename + ls);
                    }

                }

                if (lastFileCopied != null && copyToClipboard) {
                    String filename = lastFileCopied.getFileName().toString();
                    String imagename = filename.substring(0, filename.lastIndexOf(".")) + "." + outputImageFormat;
                    copyImageToClipboard(imagename);
                }

            } catch (IOException e) {
                System.err.println("***> ASPECT ERROR: Can't find (or access) the output PDF files");
                keepTemporaryFiles = true;
            }

            if (!keepTemporaryFiles) {
                deleteDirectory(directorySource);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("###############################################################");
        System.out.println("                   ASPECT END  - Goodbye!                      ");
        System.out.println("###############################################################");
    }

    private static void copyImageToClipboard(String imagename) {
        File imageFile = new File(imagename);
        Image image = null;
        try {
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        TransferableImage transferableImage = new TransferableImage(image);
        clipboard.setContents(transferableImage, null);

        System.out.println("+++> ASPECT: File " + imagename + " copied to clipboard successfully");
    }

    static void deleteDirectory(Path directory) {
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            System.err.println("***> ASPECT ERROR: Can't delete temporary directory (" + TexPdfTh.buildDirectory + ")");
            System.exit(1);
        }
    }

    static class TransferableImage implements Transferable {
        private Image image;

        public TransferableImage(Image image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.imageFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }
}
