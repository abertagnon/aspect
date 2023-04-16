
import java.io.*;

public class ASPect {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ASPect filename(s) [more than a file can be used]");
            System.exit(1);
        }
        // stringa contenente il nome dell'ultimo file passato senza l'estensione .asp
        int l_arg = args.length - 1;
        String name0 = args[l_arg];
        String name = name0.substring(0, name0.lastIndexOf("."));
        boolean merge = false;
        boolean free = false;
        String resfactor = "5";
        StringBuilder argsBuilder = new StringBuilder();
        // check per merge
        if (args[0].equals("merge")) {
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
        } else if (args[0].equals("free")) {
            free = true;
            for (int i = 1; i < args.length; i++) {
                argsBuilder.append(" ").append(args[i]);
            }
        } else {
            for (String arg : args) {
                argsBuilder.append(" ").append(arg);
            }
        }

        // creazione stringa per passare argomenti spaziati a processbuilder (tornata utile con problema passaggio args risolto)

        String arguments = argsBuilder.toString();
        // creazione pipe per i due thread, chiamate ai thread
        try {
            PipedOutputStream os = new PipedOutputStream();
            PipedInputStream is = new PipedInputStream();
            os.connect(is);
            ClingoTh cl = new ClingoTh(os, arguments);
            TexPdfTh tp = new TexPdfTh(is, name, merge, free);

            Thread clingo = new Thread(cl);
            Thread texpdf = new Thread(tp);

            clingo.start();
            texpdf.start();
            clingo.join();
            texpdf.join();

            if (merge) {
                int filenumber;
                filenumber = TexPdfTh.fn; // associare numero di file da thread texpdf
                String mergedname = name + "_merged.tex";
                File merged = new File(mergedname);
                FileWriter fw = new FileWriter(merged);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw);
                out.println("\\documentclass{beamer}\r\n"
                        + "\\usepackage{tikz}\r\n"
                        + "\\usepackage{graphicx}\r\n"
                        + "\r\n"
                        + "\\begin{document}\r\n");
                for (int j = 1; j < filenumber; j++) {
                    out.println("\\begin{frame}\r\n"
                            + "\\resizebox{" + resfactor + "em}{" + resfactor + "em}{\r\n"
                            + "\\input{" + name + j + "}\r\n"
                            + "}\r\n"
                            + "\\end{frame}\r\n");
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
                processBuilderPDF.command("sh", "-c", "pdflatex " + mergedname);
                Process mpdf = processBuilderPDF.start();

                String mpdfname = mergedname.substring(0, mergedname.lastIndexOf(".")) + ".pdf";
                System.out.println("File created: " + mpdfname + "\r");
                mpdf.waitFor();

            } else if (free) {
                int filenumber;
                filenumber = TexPdfTh.fn; // associare numero di file da thread texpdf
                String mergedname = name + "_final.tex";
                File merged = new File(mergedname);
                FileWriter fw = new FileWriter(merged);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw);
                out.println("\\documentclass{beamer}\r\n"
                        + "\\usepackage{tikz}\r\n"
                        + "\\usepackage{graphicx}\r\n"
                        + "\r\n"
                        + "\\begin{document}\r\n");
                for (int j = 1; j < filenumber; j++) {
                    out.println("\\input{" + name + j + "}\r\n");
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
                processBuilderPDF.command("sh", "-c", "pdflatex " + mergedname);
                Process mpdf = processBuilderPDF.start();

                String mpdfname = mergedname.substring(0, mergedname.lastIndexOf(".")) + ".pdf";
                System.out.println("File created: " + mpdfname + "\r");
                mpdf.waitFor();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
