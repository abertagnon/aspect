import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Iterator;


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
            ArrayList<String> tikz_commands = new ArrayList<String>();

            // faccio partire clingo, ridireziono l'output in input al mio programma

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("sh", "-c", "clingo " + arguments);
            processBuilder.redirectOutput(Redirect.PIPE);
            processBuilder.redirectError(Redirect.PIPE);
            Process clingo = processBuilder.start();
            System.out.print("clingo is running...\r\n");
            InputStream in = clingo.getInputStream();

            InputStream err = clingo.getErrorStream();
            if (err.available() > 0) {
                BufferedReader errread = new BufferedReader(new InputStreamReader(err));
                String error = errread.readLine();
                do {
                    System.err.println(error);
                    error = errread.readLine();
                } while (error != null);
            }
            // lettura linee di output con Buffered Reader,
            // le salvo in una lista di stringhe se contengono answer o aspect

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            BufferedWriter threadOut = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
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
	
		

		    	 
