import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Recepcionista extends Thread {
    private final String archivoPacientes;
    private final ColaDeEspera emergencia, urgencia, general;
    private final Reloj reloj;
    private final List<Paciente> pacientesPendientes = new LinkedList<>();
    private final Semaphore turnoDeRecepcionista;

    public Recepcionista(String archivoPacientes, ColaDeEspera emergencia, ColaDeEspera urgencia, ColaDeEspera general, Reloj reloj, Semaphore turnoDeRecepcionista) {
        this.archivoPacientes = archivoPacientes;
        this.emergencia = emergencia;
        this.urgencia = urgencia;
        this.general = general;
        this.reloj = reloj;
        cargarPacientes();
        this.turnoDeRecepcionista = turnoDeRecepcionista;
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
            try {
                turnoDeRecepcionista.acquire();  //Bloquea a los enfermeros

                List<Paciente> paraInsertar = new ArrayList<>();
                for (Paciente p : pacientesPendientes) {
                    if (p.horaLlegada == reloj.getHora() && p.minutoLlegada == reloj.getMinuto()) {
                        paraInsertar.add(p);
                    }
                }

                for (Paciente p : paraInsertar) {
                    boolean aceptado = false;
                    switch (p.tipo) {
                        case EMERGENCIA -> aceptado = emergencia.agregarPaciente(p);
                        case URGENCIA -> aceptado = urgencia.agregarPaciente(p);
                        case GENERAL, CARNE -> {
                            if (p.tipo == TipoConsulta.CARNE && !p.tieneOdontologo) {
                                Logger.log(p.getTiempoLlegadaStr() + " - " + p.nombre + " rechazado (sin odontólogo)");
                                continue;
                            }
                            aceptado = general.agregarPaciente(p);
                        }
                    }
                    String estado = aceptado ? "aceptado" : "rechazado (cola llena)";
                    Logger.log(p.getTiempoLlegadaStr() + " - " + p.nombre + " " + estado + " en cola " + p.tipo);
                    if (aceptado) p.listoParaAtencion.release();
                }

                pacientesPendientes.removeAll(paraInsertar);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                turnoDeRecepcionista.release();  //Ahora los enfermeros pueden buscar
            }
        }
    }

}