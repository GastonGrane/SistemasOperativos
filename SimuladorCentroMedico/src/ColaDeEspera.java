import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class ColaDeEspera {
    private final Queue<Paciente> cola = new LinkedList<>();
    private final int capacidadMaxima;
    private final String nombre;

    public ColaDeEspera(String nombre, int capacidad) {
        this.nombre = nombre;
        this.capacidadMaxima = capacidad;
    }

    public boolean agregarPaciente(Paciente p) {
        if (cola.size() < capacidadMaxima) {
            cola.add(p);
            return true;
        }
        return false;
    }

    public Paciente obtenerPaciente() {
        return cola.poll();
    }

    public Queue<Paciente> getPacientes() {
        return cola;
    }
}