public class Enfermero extends Thread {
    public final String nombre;
    private final ColaDeEspera emergencia, urgencia, general;
    private final Reloj reloj;
    private Paciente pacienteActual = null;
    private boolean enAsistencia = false;

    public Enfermero(String nombre, ColaDeEspera emergencia, ColaDeEspera urgencia, ColaDeEspera general, Reloj reloj) {
        this.setName(nombre); this.nombre = nombre;
        this.emergencia = emergencia;
        this.urgencia = urgencia;
        this.general = general;
        this.reloj = reloj;
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
            synchronized (emergencia) {
                paciente = emergencia.obtenerPaciente();
            }
            if (paciente == null) {
                synchronized (urgencia) {
                    paciente = urgencia.obtenerPaciente();
                }
            }
            if (paciente == null) {
                synchronized (general) {
                    paciente = general.obtenerPaciente();
                }
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
                try {
                    Thread.sleep(paciente.duracion * 10L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Logger.log(reloj.getHoraActual() + " - " + nombre + " terminó de atender a " + paciente.nombre);
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