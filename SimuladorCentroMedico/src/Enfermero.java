import java.util.concurrent.Semaphore;

public class Enfermero extends Thread {
    public final String nombre;
    private final ColaDeEspera emergencia, urgencia, general;
    private final Reloj reloj;
    private Paciente pacienteActual = null;
    private boolean enAsistencia = false;
    public final Semaphore inicioDeAtencion = new Semaphore(0);
    private final Semaphore accesoZonaCriticaColas;
    private final Semaphore turnoDeRecepcionista;



    public Enfermero(String nombre, ColaDeEspera emergencia, ColaDeEspera urgencia, ColaDeEspera general, Reloj reloj, Semaphore accesoZonaCriticaColas, Semaphore turnoDeRecepcionista) {
        this.setName(nombre); this.nombre = nombre;
        this.emergencia = emergencia;
        this.urgencia = urgencia;
        this.general = general;
        this.reloj = reloj;
        this.accesoZonaCriticaColas = accesoZonaCriticaColas;
        this.turnoDeRecepcionista = turnoDeRecepcionista;
        this.setName(nombre);
    }

    public synchronized boolean estaAtendiendo() {
        return pacienteActual != null;
    }

    public synchronized Paciente getPacienteActual() {
        return pacienteActual;
    }

    public synchronized boolean tieneAsistencia() {
        return enAsistencia;
    }

    public synchronized void marcarAsistencia() {
        this.enAsistencia = true;
    }

    @Override
    public void run() {
        while (!reloj.esFinDelDia()) {
            Paciente paciente = null;

            try {
                turnoDeRecepcionista.acquire();   // espera que la recepcionista termine
                turnoDeRecepcionista.release();   // libera inmediatamente
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                // ZONA CRÍTICA: solo un enfermero accede a las colas por vez
                accesoZonaCriticaColas.acquire();

                paciente = emergencia.obtenerPaciente();
                if (paciente == null) paciente = urgencia.obtenerPaciente();
                if (paciente == null) paciente = general.obtenerPaciente();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                accesoZonaCriticaColas.release();  // Liberar el acceso a las colas
            }

            if (paciente != null) {
                try {
                    paciente.listoParaAtencion.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (this) {
                    pacienteActual = paciente;
                    enAsistencia = false;
                }
                Logger.log(reloj.getHoraActual() + " - " + nombre + " empieza a atender a " + paciente.nombre + " (" + paciente.tipo + ")");
                inicioDeAtencion.release();  // Habilita a los médicos
                try {
                    Thread.sleep(paciente.duracion * 10L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Logger.log(reloj.getHoraActual() + " - " + nombre + " terminó de atender a " + paciente.nombre);
                inicioDeAtencion.drainPermits(); // Limpia el semáforo para el siguiente
                synchronized (this) {
                    pacienteActual = null;
                    enAsistencia = false;
                }
            } else {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}