package org.example.dao;

import org.example.modelo.Repartidor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RepartidorDAO {

    public void guardar(Repartidor repartidor) throws SQLException {
        String sql = "INSERT INTO repartidor (nombre) VALUES (?)";

        try (Connection conexion = ConexionBD.obtenerConexion(); PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, repartidor.getNombre());

            int filas = stmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo guardar el repartidor.");
            }

            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    repartidor.setId(claves.getInt(1));
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al guardar el repartidor en MySQL.", ex);
        }
    }

    public List<Repartidor> listarTodos() throws SQLException {
        String sql = "SELECT id, nombre FROM repartidor ORDER BY nombre";
        List<Repartidor> repartidores = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion(); PreparedStatement stmt = conexion.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                repartidores.add(new Repartidor(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException ex) {
            throw new SQLException("Error al consultar los repartidores en MySQL.", ex);
        }

        return repartidores;
    }
}
