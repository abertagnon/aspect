import java.io.*;
import java.nio.file.Paths;

public class ASPect {
    public static String file_out_prefix = "aspect_out_";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ASPect filename(s) [more than a file can be used]");
            System.exit(1);
        }
        // stringa contenente il nome dell'ultimo file passato senza l'estensione .asp
        int l_arg = args.length - 1;
        String name0 = args[l_arg];
        String name_with_extension = Paths.get(name0).getFileName().toString();
        String name = name_with_extension.substring(0, name_with_extension.lastIndexOf("."));
        boolean merge = false;
        boolean free = false;
        boolean graph = false;
        String resfactor = "5";
        StringBuilder argsBuilder = new StringBuilder();
        // check per merge
        switch (args[0]) {
            case "merge":
                merge = true;

                if (args[1].contains("resize")) {
                    resfactor = args[1].replaceAll("[^0-9]+", " ").trim();
                    for (int i = 2; i < args.length; i++) {
                        argsBuilder.append(" ").append(args[i]);

                    }
                } else {
                    for (int i = 1; i < args.length; i++) {
                        argsBuilder.append(" ").append(args[i]);
                    }
                }
                break;
            case "free":
                free = true;
                for (int i = 1; i < args.length; i++) {
                    argsBuilder.append(" ").append(args[i]);
                }
                break;
            case "graph":
                graph = true;
                for (int i = 1; i < args.length; i++) {
                    argsBuilder.append(" ").append(args[i]);
                }
                break;
            default:
                for (String arg : args) {
                    argsBuilder.append(" ").append(arg);
                }
                break;
        }

        // creazione stringa per passare argomenti spaziati a processbuilder (tornata utile con problema passaggio args risolto)

        String arguments = argsBuilder.toString();
        // creazione pipe per i due thread, chiamate ai thread
        try {
            PipedOutputStream os = new PipedOutputStream();
            PipedInputStream is = new PipedInputStream();
            os.connect(is);
            ClingoTh cl = new ClingoTh(os, arguments);
            TexPdfTh tp = new TexPdfTh(is, name, merge, free, graph);

            Thread clingo = new Thread(cl);
            Thread texpdf = new Thread(tp);

            clingo.start();
            texpdf.start();
            clingo.join();
            texpdf.join();



            if (merge || free) {
                int filenumber;
                filenumber = TexPdfTh.fn; // associare numero di file da thread texpdf
                String mergedname = file_out_prefix + name + "_final.tex";
                File merged = new File(mergedname);
                FileWriter fw = new FileWriter(merged);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw);
                out.println("\\documentclass{beamer}" + System.lineSeparator()
                            + "\\usepackage{tikz}" + System.lineSeparator()
                            + "\\usepackage{graphicx}" + System.lineSeparator()
                            + System.lineSeparator()
                            + "\\begin{document}" + System.lineSeparator()
                            );
                for (int j = 1; j < filenumber; j++) {
                    if (merge) {
                        out.println("\\begin{frame}" + System.lineSeparator()
                                    + "\\resizebox{" + resfactor + "em}{" + resfactor + "em}{" + System.lineSeparator()
                                    + "\\input{" + file_out_prefix + name + j + "}" + System.lineSeparator()
                                    + "}" + System.lineSeparator()
                                    + "\\end{frame}" + System.lineSeparator()
                                    );
                    }
                    else if (free) {
                        out.println("\\input{" + file_out_prefix + name + j + "}" + System.lineSeparator());
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
                processBuilderPDF.command("pdflatex", "-shell-escape", "-halt-on-error", mergedname);
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
