package org.example.vista;

import org.example.controlador.ControladorPedidos;
import org.example.modelo.Pedido;

import javax.swing.*;

public class VentanaRegistroPedido extends JFrame {
    private JPanel VentanaRegistroPedido;
    private JLabel lblTitulo;
    private JLabel lblId;
    private JLabel lblDireccion;
    private JLabel lblTipo;
    private JLabel lblDistancia;
    private JTextField txtDistancia;
    private JTextField txtId;
    private JTextField txtDireccion;
    private JComboBox<String> cmbTipo;
    private JButton btnGuardar;
    private JButton btnLimpiar;

    private final ControladorPedidos controlador;

    public VentanaRegistroPedido(ControladorPedidos controlador) {
        this.controlador = controlador;

        setTitle("SpeedFast - Registrar Pedido");
        setContentPane(VentanaRegistroPedido);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 320);
        setLocationRelativeTo(null);
        setResizable(false);

        // La tabla pedido usa AUTO_INCREMENT, por eso el usuario ya no ingresa el ID.
        txtId.setText("Automático");
        txtId.setEnabled(false);

        cmbTipo.removeAllItems();
        cmbTipo.addItem("Comida");
        cmbTipo.addItem("Encomienda");
        cmbTipo.addItem("Express");

        btnGuardar.addActionListener(e -> guardarPedido());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private void guardarPedido() {
        String direccion = txtDireccion.getText().trim();
        String tipo = (String) cmbTipo.getSelectedItem();
        String textoDistancia = txtDistancia.getText().trim();

        if (direccion.isEmpty() || textoDistancia.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar la dirección y la distancia.", "Campos obligatorios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double distancia = Double.parseDouble(textoDistancia.replace(',', '.'));
            Pedido pedido = controlador.registrarPedido(direccion, tipo, distancia);

            JOptionPane.showMessageDialog(this, "Pedido #" + pedido.getIdPedido() + " registrado correctamente en la base de datos.", "SpeedFast", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La distancia debe ser un número válido.", "Distancia inválida", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo registrar", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtId.setText("Automático");
        txtDireccion.setText("");
        txtDistancia.setText("");
        cmbTipo.setSelectedIndex(0);
        txtDireccion.requestFocus();
    }
}
