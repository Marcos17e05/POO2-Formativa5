package org.example.modelo;

public class PedidoComida extends Pedido {

    public PedidoComida(int idPedido, String direccionEntrega) {
        super(idPedido, direccionEntrega);
    }

    public PedidoComida(int idPedido, String direccionEntrega, double distanciaKm) {
        super(idPedido, direccionEntrega, distanciaKm);
    }

    @Override
    public String getTipoPedido() {
        return "Comida";
    }

    @Override
    public int calcularTiempoEntrega() {
        return (int) Math.round(15 + (2 * getDistanciaKm()));
    }
}
