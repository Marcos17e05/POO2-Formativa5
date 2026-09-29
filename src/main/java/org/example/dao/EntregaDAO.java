package org.example.dao;

import org.example.modelo.Entrega;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EntregaDAO {

    public void guardar(Entrega entrega) throws SQLException {
        String sql = "INSERT INTO entrega (id_pedido, id_repartidor, fecha, hora) VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion(); PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, entrega.getIdPedido());
            stmt.setInt(2, entrega.getIdRepartidor());
            stmt.setDate(3, java.sql.Date.valueOf(entrega.getFecha()));
            stmt.setTime(4, java.sql.Time.valueOf(entrega.getHora()));

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo guardar la entrega.");
            }

            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    entrega.setId(claves.getInt(1));
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al guardar la entrega en MySQL.", ex);
        }
    }
}
