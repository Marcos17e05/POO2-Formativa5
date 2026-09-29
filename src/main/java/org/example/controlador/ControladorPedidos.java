package org.example.controlador;

import org.example.dao.EntregaDAO;
import org.example.dao.PedidoDAO;
import org.example.dao.RepartidorDAO;
import org.example.modelo.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControladorPedidos {
    private final PedidoDAO pedidoDAO;
    private final RepartidorDAO repartidorDAO;
    private final EntregaDAO entregaDAO;

    private final Map<Integer, Double> distanciasSesion;

    public ControladorPedidos() {
        this.pedidoDAO = new PedidoDAO();
        this.repartidorDAO = new RepartidorDAO();
        this.entregaDAO = new EntregaDAO();
        this.distanciasSesion = new HashMap<>();
    }

    public synchronized Pedido registrarPedido(String direccion, String tipo, double distancia) {
        if (distancia < 0) {
            throw new IllegalArgumentException("La distancia no puede ser negativa.");
        }

        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección es obligatoria.");
        }

        Pedido pedido = crearPedido(0, direccion.trim(), tipo, distancia);

        try {
            pedidoDAO.guardar(pedido);
            distanciasSesion.put(pedido.getIdPedido(), distancia);
            return pedido;
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized Pedido registrarPedido(int id, String direccion, String tipo, double distancia) {
        return registrarPedido(direccion, tipo, distancia);
    }

    public synchronized Repartidor registrarRepartidor(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del repartidor es obligatorio.");
        }

        String nombreLimpio = nombre.trim();

        try {
            for (Repartidor existente : repartidorDAO.listarTodos()) {
                if (existente.getNombre().equalsIgnoreCase(nombreLimpio)) {
                    throw new IllegalArgumentException("Ya existe un repartidor con ese nombre.");
                }
            }

            Repartidor repartidor = new Repartidor(nombreLimpio);
            repartidorDAO.guardar(repartidor);
            return repartidor;
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized List<Pedido> getPedidos() {
        try {
            List<Pedido> pedidos = pedidoDAO.listarTodos();
            aplicarDistanciasDeSesion(pedidos);
            return pedidos;
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized Pedido buscarPorId(int id) {
        try {
            Pedido pedido = pedidoDAO.buscarPorId(id);
            if (pedido != null) {
                Double distancia = distanciasSesion.get(id);
                if (distancia != null) {
                    pedido.setDistanciaKm(distancia);
                }
            }
            return pedido;
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized List<Repartidor> getRepartidores() {
        try {
            return repartidorDAO.listarTodos();
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public String[] getNombresRepartidores() {
        List<Repartidor> repartidores = getRepartidores();
        String[] nombres = new String[repartidores.size()];

        for (int i = 0; i < repartidores.size(); i++) {
            nombres[i] = repartidores.get(i).getNombre();
        }

        return nombres;
    }

    public synchronized void iniciarEntrega(int idPedido, Repartidor repartidor) {
        if (repartidor == null) {
            throw new IllegalArgumentException("Debe seleccionar un repartidor.");
        }

        Pedido pedido = buscarPorId(idPedido);

        if (pedido == null) {
            throw new IllegalArgumentException("No se encontró el pedido seleccionado.");
        }

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("El pedido ya fue iniciado o entregado.");
        }

        Entrega entrega = new Entrega(
                pedido.getIdPedido(),
                repartidor.getId(),
                LocalDate.now(),
                LocalTime.now().withNano(0)
        );

        try {
            // Primero se registra la relación pedido-repartidor y luego se cambia el estado.
            entregaDAO.guardar(entrega);
            pedidoDAO.actualizarEstado(idPedido, EstadoPedido.EN_REPARTO);

            pedido.asignarRepartidor(repartidor.getNombre());
            pedido.setEstado(EstadoPedido.EN_REPARTO);
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }

        Thread hiloEntrega = new Thread(() -> ejecutarEntrega(pedido, repartidor),
                "Entrega-Pedido-" + idPedido);
        hiloEntrega.start();
    }

    /** Compatibilidad con la firma de la semana anterior. */
    public synchronized void iniciarEntrega(int idPedido, String nombreRepartidor) {
        for (Repartidor repartidor : getRepartidores()) {
            if (repartidor.getNombre().equalsIgnoreCase(nombreRepartidor)) {
                iniciarEntrega(idPedido, repartidor);
                return;
            }
        }
        throw new IllegalArgumentException("No se encontró el repartidor seleccionado.");
    }

    private void ejecutarEntrega(Pedido pedido, Repartidor repartidor) {
        Repartidor trabajador = new Repartidor(repartidor.getNombre(), pedido);
        trabajador.run();

        try {
            pedidoDAO.actualizarEstado(pedido.getIdPedido(), pedido.getEstado());
        } catch (SQLException ex) {
            System.err.println("No se pudo actualizar el estado final del pedido #"
                    + pedido.getIdPedido() + ": " + ex.getMessage());
        }
    }

    private Pedido crearPedido(int id, String direccion, String tipo, double distancia) {
        if (tipo == null) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de pedido.");
        }

        switch (tipo.trim().toLowerCase()) {
            case "comida":
                return new PedidoComida(id, direccion, distancia);
            case "encomienda":
                return new PedidoEncomienda(id, direccion, distancia);
            case "express":
                return new PedidoExpress(id, direccion, distancia);
            default:
                throw new IllegalArgumentException("Tipo de pedido no válido.");
        }
    }

    private void aplicarDistanciasDeSesion(List<Pedido> pedidos) {
        for (Pedido pedido : pedidos) {
            Double distancia = distanciasSesion.get(pedido.getIdPedido());
            if (distancia != null) {
                pedido.setDistanciaKm(distancia);
            }
        }
    }

    private String mensajeBD(SQLException ex) {
        String detalle = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        return "No fue posible completar la operación en la base de datos.\n"
                + "Verifique MySQL, la base speedfast_db y las credenciales de ConexionBD.\n"
                + "Detalle: " + detalle;
    }
}
