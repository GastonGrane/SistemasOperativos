import java.util.Iterator;
import java.util.LinkedList;

public class Administrador extends Thread {
    private final ColaDeEspera emergencia, urgencia, general;
    private final Reloj reloj;
    private final LinkedList<Paciente> muertos = new LinkedList<>();
    private final LinkedList<Paciente> abandonos = new LinkedList<>();
    private final LinkedList<Paciente> promovidos = new LinkedList<>();

    public Administrador(ColaDeEspera emergencia, ColaDeEspera urgencia, ColaDeEspera general, Reloj reloj) {
        this.emergencia = emergencia;
        this.urgencia = urgencia;
        this.general = general;
        this.reloj = reloj;
    }

    @Override
    public void run() {
        while (!reloj.esFinDelDia()) {
            revisarCola(urgencia);
            revisarCola(emergencia);
            revisarCola(general);
            try {
                Thread.sleep(10); // revisar por minuto simulado
            } catch (InterruptedException e) {
                break;
            }
        }

        Logger.log("=== ESTADISTICAS FINALES ===");
        Logger.log("Muertos: " + muertos.size());
        Logger.log("Abandonaron: " + abandonos.size());
        Logger.log("Urgencias promovidas: " + promovidos.size());
    }

    private void revisarCola(ColaDeEspera cola) {
        synchronized (cola) {
            Iterator<Paciente> it = cola.getPacientes().iterator();
            while (it.hasNext()) {
                Paciente p = it.next();
                int espera = reloj.minutosDesde(p.horaLlegada, p.minutoLlegada);
                switch (p.tipo) {
                    case URGENCIA -> {
                        if (espera > 110) {
                            it.remove();
                            p.tipo = TipoConsulta.EMERGENCIA;
                            emergencia.agregarPaciente(p);
                            Logger.log(reloj.getHoraActual() + " - " + p.nombre + " promovido de URGENCIA a EMERGENCIA");
                            promovidos.add(p);
                        }
                    }
                    case EMERGENCIA -> {
                        if (espera > 10) {
                            it.remove();
                            Logger.log(reloj.getHoraActual() + " - " + p.nombre + " murió esperando en EMERGENCIA");
                            muertos.add(p);
                        }
                    }
                    case GENERAL, CARNE -> {
                        if (espera > 180) {
                            it.remove();
                            Logger.log(reloj.getHoraActual() + " - " + p.nombre + " abandonó la cola GENERAL");
                            abandonos.add(p);
                        }
                    }
                }
            }
        }
    }
}