package org.example.modelo;

public class Repartidor implements Runnable {
    private int id;
    private final String nombre;
    private final Pedido pedido;

    /** Constructor usado por RepartidorDAO al leer desde MySQL. */
    public Repartidor(int id, String nombre) {
        this(id, nombre, null);
    }

    /** Constructor usado antes de insertar un repartidor nuevo. */
    public Repartidor(String nombre) {
        this(0, nombre, null);
    }

    /** Constructor conservado para la simulación de una entrega en un hilo. */
    public Repartidor(String nombre, Pedido pedido) {
        this(0, nombre, pedido);
    }

    private Repartidor(int id, String nombre, Pedido pedido) {
        this.id = id;
        this.nombre = nombre;
        this.pedido = pedido;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public Pedido getPedido() {
        return pedido;
    }

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public void run() {
        if (pedido == null) {
            return;
        }

        try {
            System.out.println(nombre + " inició la entrega del pedido #" + pedido.getIdPedido());
            Thread.sleep(pedido.getDuracionSimulacionMs());
            pedido.setEstado(EstadoPedido.ENTREGADO);
            System.out.println(nombre + " entregó el pedido #" + pedido.getIdPedido());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            pedido.setEstado(EstadoPedido.PENDIENTE);
            pedido.asignarRepartidor("Sin asignar");
        }
    }
}
