import java.io.*;
import org.apache.commons.lang3.SystemUtils;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
public class ClingoTh implements Runnable {
    OutputStream output;
    String arguments;

    //output a pipe, argomenti in ingresso
    public ClingoTh(OutputStream os, String argz) {
        this.output = os;
        arguments = argz;
    }

    public void run() {

        try {
            String line;

            // faccio partire clingo, ridireziono l'output in input al mio programma

            ProcessBuilder processBuilder = new ProcessBuilder();
            if (SystemUtils.IS_OS_MAC_OSX) System.setProperty( "jdk.lang.Process.launchMechanism", "fork" );
            processBuilder.command("sh", "-c", "clingo " + arguments);
            processBuilder.redirectOutput(Redirect.PIPE);
            processBuilder.redirectError(Redirect.PIPE);

            System.out.println("Starting clingo...");
            Process clingo = processBuilder.start();

            InputStream in = clingo.getInputStream();
            InputStream err = clingo.getErrorStream();
            if (err.available() > 0) {
                BufferedReader errread = new BufferedReader(new InputStreamReader(err));
                String error = errread.readLine();
                do {
                    System.err.println(error);
                    error = errread.readLine();
                } while (error != null);

                System.err.println("clingo encountered an error.");
                System.err.println("Nothing else to do.");
                System.exit(1);
            }
            // lettura linee di output con Buffered Reader,
            // le salvo in una lista di stringhe se contengono answer o aspect

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            BufferedWriter threadOut = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
            PrintWriter print = new PrintWriter(threadOut);
            line = br.readLine();

            while (line != null) {
                if (line.contains("aspect")) {
                    print.println(line);
                }
                line = br.readLine();
            }

            threadOut.flush();
            threadOut.close();


        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}




