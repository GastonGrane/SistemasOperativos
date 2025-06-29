public class Medico extends Thread {
    private final String nombre;
    private final Coordinador coordinador;
    private final Reloj reloj;

    public Medico(String nombre, Coordinador coordinador, Reloj reloj) {
        this.nombre = nombre;
        this.coordinador = coordinador;
        this.reloj = reloj;
    }

    @Override
    public void run() {
        while (!reloj.esFinDelDia()) {
            Enfermero enfermero = coordinador.buscarEnfermeroParaAsistir();
            if (enfermero != null) {
                Paciente paciente = enfermero.getPacienteActual();
                Logger.log(reloj.getHoraActual() + " - " + nombre + " asiste a " + enfermero.getName() + " con paciente " + paciente.nombre);
            }
            try {
                Thread.sleep(5); // espera antes de revisar de nuevo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}