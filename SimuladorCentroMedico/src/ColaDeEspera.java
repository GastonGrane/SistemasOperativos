import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class ColaDeEspera {
    private final Queue<Paciente> cola = new LinkedList<>();
    private final int capacidadMaxima;
    private final String nombre;
    private final Semaphore mutex = new Semaphore(1);


    public ColaDeEspera(String nombre, int capacidad) {
        this.nombre = nombre;
        this.capacidadMaxima = capacidad;
    }

    public boolean agregarPaciente(Paciente p) {
        try {
            mutex.acquire();
            if (cola.size() < capacidadMaxima) {
                cola.add(p);
                return true;
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            mutex.release();
        }
    }


    public Paciente obtenerPaciente() {
        return cola.poll();
    }

    public Queue<Paciente> getPacientes() {
        return cola;
    }
}