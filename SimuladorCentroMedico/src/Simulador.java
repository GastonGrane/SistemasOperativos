import java.util.concurrent.Semaphore;

public class Simulador {
    public static void main(String[] args) {
        Logger.iniciarLog();
        Semaphore accesoZonaCriticaColas = new Semaphore(1);
        Semaphore turnoDeRecepcionista = new Semaphore(1);


        Logger.log("=== INICIO DE LA SIMULACION ===");

        Reloj reloj = new Reloj();
        ColaDeEspera emergencia = new ColaDeEspera("Emergencia", 3);
        ColaDeEspera urgencia = new ColaDeEspera("Urgencia", 3);
        ColaDeEspera general = new ColaDeEspera("General", 5);

        Coordinador coordinador = new Coordinador();

        Recepcionista recepcionista = new Recepcionista("SimuladorCentroMedico\\pacientes.txt", emergencia, urgencia, general, reloj, turnoDeRecepcionista);
        Enfermero enfermero1 = new Enfermero("Enfermero A", emergencia, urgencia, general, reloj, accesoZonaCriticaColas, turnoDeRecepcionista);
        Enfermero enfermero2 = new Enfermero("Enfermero B", emergencia, urgencia, general, reloj, accesoZonaCriticaColas, turnoDeRecepcionista);

        coordinador.registrarEnfermeros(enfermero1, enfermero2);

        Medico medico1 = new Medico("Médico A", coordinador, reloj);
        Medico medico2 = new Medico("Médico B", coordinador, reloj);

        recepcionista.start();
        enfermero1.start();
        enfermero2.start();
        medico1.start();
        medico2.start();

        Administrador admin = new Administrador(emergencia, urgencia, general, reloj);
        admin.start();

        while (!reloj.esFinDelDia()) {
            Logger.log("Hora: " + reloj.getHoraActual());
            reloj.avanzarMinuto();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Logger.log("=== FIN DE LA SIMULACION ===");
    }
}