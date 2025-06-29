public class Reloj {
    public int minutosDesde(int h, int m) {
        return (hora - h) * 60 + (minuto - m);
    }
    private int hora = 8;
    private int minuto = 0;

    public synchronized void avanzarMinuto() {
        minuto++;
        if (minuto >= 60) {
            minuto = 0;
            hora++;
        }
    }

    public synchronized String getHoraActual() {
        return String.format("%02d:%02d", hora, minuto);
    }

    public synchronized int getHora() {
        return hora;
    }

    public synchronized int getMinuto() {
        return minuto;
    }

    public synchronized boolean esFinDelDia() {
        return hora >= 20;
    }
}