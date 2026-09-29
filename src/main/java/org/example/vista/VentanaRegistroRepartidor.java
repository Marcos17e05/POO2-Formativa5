package org.example.vista;

import org.example.controlador.ControladorPedidos;
import org.example.modelo.Repartidor;

import javax.swing.*;

public class VentanaRegistroRepartidor extends JFrame {
    private JPanel VentanaRegistroRepartidor;
    private JLabel lblTitulo;
    private JLabel lblNombre;
    private JTextField txtNombre;
    private JButton btnGuardar;
    private JButton btnLimpiar;

    private final ControladorPedidos controlador;

    public VentanaRegistroRepartidor(ControladorPedidos controlador) {
        this.controlador = controlador;

        setTitle("SpeedFast - Registrar Repartidor");
        setContentPane(VentanaRegistroRepartidor);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(460, 230);
        setLocationRelativeTo(null);
        setResizable(false);

        btnGuardar.addActionListener(e -> guardarRepartidor());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private void guardarRepartidor() {
        String nombre = txtNombre.getText().trim();

        try {
            Repartidor repartidor = controlador.registrarRepartidor(nombre);

            JOptionPane.showMessageDialog(this, "Repartidor #" + repartidor.getId() + " - " + repartidor.getNombre() + " registrado correctamente en la base de datos.", "SpeedFast", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo registrar", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtNombre.requestFocus();
    }
}
