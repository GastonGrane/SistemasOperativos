import java.util.concurrent.Semaphore;

public class Coordinador {
    private final Semaphore mutex = new Semaphore(1);
    private Enfermero enfermeroA;
    private Enfermero enfermeroB;

    public void registrarEnfermeros(Enfermero a, Enfermero b) {
        this.enfermeroA = a;
        this.enfermeroB = b;
    }

    public Enfermero buscarEnfermeroParaAsistir() {
        try {
            mutex.acquire();
            if (enfermeroA.estaAtendiendo() && enfermeroA.getPacienteActual().tipo != TipoConsulta.CARNE && !enfermeroA.tieneAsistencia()) {
                enfermeroA.marcarAsistencia();
                return enfermeroA;
            } else if (enfermeroB.estaAtendiendo() && enfermeroB.getPacienteActual().tipo != TipoConsulta.CARNE && !enfermeroB.tieneAsistencia()) {
                enfermeroB.marcarAsistencia();
                return enfermeroB;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.release();
        }
        return null;
    }
}