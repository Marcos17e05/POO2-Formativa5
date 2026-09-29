package org.example.dao;

import org.example.modelo.EstadoPedido;
import org.example.modelo.Pedido;
import org.example.modelo.PedidoComida;
import org.example.modelo.PedidoEncomienda;
import org.example.modelo.PedidoExpress;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    public void guardar(Pedido pedido) throws SQLException {
        String sql = "INSERT INTO pedido (direccion, tipo, estado) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion(); PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, pedido.getDireccionEntrega());
            stmt.setString(2, tipoParaBD(pedido));
            stmt.setString(3, pedido.getEstado().name());

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo guardar el pedido.");
            }

            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    pedido.setIdPedido(claves.getInt(1));
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al guardar el pedido en MySQL.", ex);
        }
    }

    public List<Pedido> listarTodos() throws SQLException {
        String sql = "SELECT p.id, p.direccion, p.tipo, p.estado, " + "(SELECT r.nombre " + " FROM entrega e " + " JOIN repartidor r ON r.id = e.id_repartidor " + " WHERE e.id_pedido = p.id " + " ORDER BY e.id DESC LIMIT 1) AS repartidor " + "FROM pedido p ORDER BY p.id";

        List<Pedido> pedidos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion(); PreparedStatement stmt = conexion.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                pedidos.add(mapearPedido(rs));
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al consultar los pedidos en MySQL.", ex);
        }

        return pedidos;
    }

    public Pedido buscarPorId(int id) throws SQLException {
        String sql = "SELECT p.id, p.direccion, p.tipo, p.estado, " + "(SELECT r.nombre " + " FROM entrega e " + " JOIN repartidor r ON r.id = e.id_repartidor " + " WHERE e.id_pedido = p.id " + " ORDER BY e.id DESC LIMIT 1) AS repartidor " + "FROM pedido p WHERE p.id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion(); PreparedStatement stmt = conexion.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPedido(rs);
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al buscar el pedido en MySQL.", ex);
        }

        return null;
    }

    public void actualizarEstado(int idPedido, EstadoPedido estado) throws SQLException {
        String sql = "UPDATE pedido SET estado = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion(); PreparedStatement stmt = conexion.prepareStatement(sql)) {

            stmt.setString(1, estado.name());
            stmt.setInt(2, idPedido);

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No existe el pedido #" + idPedido + ".");
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al actualizar el estado del pedido.", ex);
        }
    }

    private Pedido mapearPedido(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String direccion = rs.getString("direccion");
        String tipo = rs.getString("tipo");
        String estadoTexto = rs.getString("estado");
        String repartidor = rs.getString("repartidor");

        Pedido pedido = crearPedido(id, direccion, tipo);

        try {
            pedido.setEstado(EstadoPedido.valueOf(estadoTexto.toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException ex) {
            pedido.setEstado(EstadoPedido.PENDIENTE);
        }

        if (repartidor != null && !repartidor.trim().isEmpty()) {
            pedido.asignarRepartidor(repartidor);
        }

        return pedido;
    }

    private Pedido crearPedido(int id, String direccion, String tipo) throws SQLException {
        if (tipo == null) {
            throw new SQLException("El pedido #" + id + " no tiene un tipo válido.");
        }

        switch (tipo.trim().toUpperCase()) {
            case "COMIDA":
                return new PedidoComida(id, direccion);
            case "ENCOMIENDA":
                return new PedidoEncomienda(id, direccion);
            case "EXPRESS":
                return new PedidoExpress(id, direccion);
            default:
                throw new SQLException("Tipo de pedido desconocido en la BD: " + tipo);
        }
    }

    private String tipoParaBD(Pedido pedido) {
        return pedido.getTipoPedido().trim().toUpperCase();
    }
}
