import java.util.LinkedList;
import java.util.Queue;

public class ColaDeEspera {
    private final Queue<Paciente> cola = new LinkedList<>();
    private final int capacidadMaxima;
    private final String nombre;

    public ColaDeEspera(String nombre, int capacidad) {
        this.nombre = nombre;
        this.capacidadMaxima = capacidad;
    }

    public synchronized boolean agregarPaciente(Paciente p) {
        if (cola.size() < capacidadMaxima) {
            cola.add(p);
            return true;
        }
        return false;
    }

    public synchronized Paciente obtenerPaciente() {
        return cola.poll();
    }

    public synchronized int size() {
        return cola.size();
    }

    public synchronized boolean estaVacia() {
        return cola.isEmpty();
    }

    public synchronized Queue<Paciente> getPacientes() {
        return cola;
    }

    public String getNombre() {
        return nombre;
    }
}