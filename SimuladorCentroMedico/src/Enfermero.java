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

            // Esperar a que la recepcionista termine de insertar
            try {
                turnoDeRecepcionista.acquire();
                turnoDeRecepcionista.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // ZONA CRÍTICA: acceso único a todas las colas
            try {
                accesoZonaCriticaColas.acquire();

                paciente = emergencia.obtenerPaciente();
                if (paciente == null) paciente = urgencia.obtenerPaciente();
                if (paciente == null) paciente = general.obtenerPaciente();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                accesoZonaCriticaColas.release();
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
                inicioDeAtencion.drainPermits();  // Resetear para el próximo

                synchronized (this) {
                    pacienteActual = null;
                    enAsistencia = false;
                }
            } else {
                try {
                    Thread.sleep(5);  // espera breve si no hay paciente
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}