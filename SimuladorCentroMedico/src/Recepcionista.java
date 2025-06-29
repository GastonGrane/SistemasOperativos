import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Recepcionista extends Thread {
    private final String archivoPacientes;
    private final ColaDeEspera emergencia, urgencia, general;
    private final Reloj reloj;
    private final List<Paciente> pacientesPendientes = new ArrayList<>();

    public Recepcionista(String archivoPacientes, ColaDeEspera emergencia, ColaDeEspera urgencia, ColaDeEspera general, Reloj reloj) {
        this.archivoPacientes = archivoPacientes;
        this.emergencia = emergencia;
        this.urgencia = urgencia;
        this.general = general;
        this.reloj = reloj;
        cargarPacientes();
    }

    private void cargarPacientes() {
        try (BufferedReader br = new BufferedReader(new FileReader(archivoPacientes))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("-");
                if (partes.length != 5) continue;
                String nombre = partes[0].trim();
                TipoConsulta tipo = TipoConsulta.valueOf(partes[1].trim().toUpperCase());
                String[] horaMin = partes[2].trim().split(":");
                int hora = Integer.parseInt(horaMin[0]);
                int minuto = Integer.parseInt(horaMin[1]);
                int duracion = Integer.parseInt(partes[3].trim());
                boolean tieneOdontologo = partes[4].trim().equalsIgnoreCase("si");

                Paciente p = new Paciente(nombre, tipo, hora, minuto, duracion, tieneOdontologo);
                pacientesPendientes.add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!reloj.esFinDelDia()) {
            List<Paciente> ingresadosEsteMinuto = new ArrayList<>();
            for (Paciente p : pacientesPendientes) {
                if (p.horaLlegada == reloj.getHora() && p.minutoLlegada == reloj.getMinuto()) {
                    boolean aceptado = false;
                    switch (p.tipo) {
                        case EMERGENCIA -> aceptado = emergencia.agregarPaciente(p);
                        case URGENCIA -> aceptado = urgencia.agregarPaciente(p);
                        case GENERAL, CARNE -> {
                            if (p.tipo == TipoConsulta.CARNE && !p.tieneOdontologo) {
                                Logger.log(p.getTiempoLlegadaStr() + " - " + p.nombre + " rechazado (sin odontólogo)");
                                ingresadosEsteMinuto.add(p);
                                continue;
                            }
                            aceptado = general.agregarPaciente(p);
                        }
                    }
                    String estado = aceptado ? "aceptado" : "rechazado (cola llena)";
                    Logger.log(p.getTiempoLlegadaStr() + " - " + p.nombre + " " + estado + " en cola " + p.tipo);
                    if (aceptado) p.listoParaAtencion.release();
                    ingresadosEsteMinuto.add(p);
                }
            }
            pacientesPendientes.removeAll(ingresadosEsteMinuto);
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}