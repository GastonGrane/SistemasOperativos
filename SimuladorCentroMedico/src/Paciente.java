import java.util.concurrent.Semaphore;

public class Paciente {
    public String nombre;
    public TipoConsulta tipo;
    public int horaLlegada;
    public int minutoLlegada;
    public int duracion;
    public boolean tieneOdontologo;
    public int tiempoEspera = 0;
    public final Semaphore listoParaAtencion = new Semaphore(0);

    public Paciente(String nombre, TipoConsulta tipo, int hora, int minuto, int duracion, boolean tieneOdontologo) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.horaLlegada = hora;
        this.minutoLlegada = minuto;
        this.duracion = duracion;
        this.tieneOdontologo = tieneOdontologo;
    }

    public String getTiempoLlegadaStr() {
        return String.format("%02d:%02d", horaLlegada, minutoLlegada);
    }

    @Override
    public String toString() {
        return nombre + " - " + tipo + " - " + getTiempoLlegadaStr() + " - " + duracion + " min - Odontólogo: " + tieneOdontologo;
    }
}