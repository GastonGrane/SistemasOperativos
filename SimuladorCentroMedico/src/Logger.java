import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    private static final String ARCHIVO_LOG = "log_simulacion.txt";

    public static void iniciarLog() {
        try (PrintWriter out = new PrintWriter(new FileWriter(ARCHIVO_LOG))) {
            out.print(""); // Limpia el contenido
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void log(String mensaje) {
        try (PrintWriter out = new PrintWriter(new FileWriter(ARCHIVO_LOG, true))) {
            out.println(mensaje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}