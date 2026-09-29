package org.example.modelo;

public abstract class Pedido {
    private int idPedido;
    private final String direccionEntrega;
    private double distanciaKm;
    private volatile String repartidorAsignado;
    private volatile EstadoPedido estado;

    public Pedido(int idPedido, String direccionEntrega) {
        this(idPedido, direccionEntrega, 0.0);
    }

    public Pedido(int idPedido, String direccionEntrega, double distanciaKm) {
        this.idPedido = idPedido;
        this.direccionEntrega = direccionEntrega;
        this.distanciaKm = distanciaKm;
        this.repartidorAsignado = "Sin asignar";
        this.estado = EstadoPedido.PENDIENTE;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public String getRepartidorAsignado() {
        return repartidorAsignado;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public void asignarRepartidor() { this.repartidorAsignado = "Repartidor por asignar";}

    public void asignarRepartidor(String nombreRepartidor) {
        this.repartidorAsignado = nombreRepartidor;
    }

    public abstract String getTipoPedido();

    public abstract int calcularTiempoEntrega();

    public long getDuracionSimulacionMs() {
        return Math.max(1200L, calcularTiempoEntrega() * 120L);
    }

}
