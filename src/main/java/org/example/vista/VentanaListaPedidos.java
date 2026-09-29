package org.example.vista;

import org.example.controlador.ControladorPedidos;
import org.example.modelo.Pedido;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VentanaListaPedidos extends JFrame {
    private JPanel VentanaListaPedidos;
    private JLabel lblTitulo;
    private JTable tblPedidos;
    private JButton btnRefrescar;

    private final ControladorPedidos controlador;
    private DefaultTableModel model;

    public VentanaListaPedidos(ControladorPedidos controlador) {
        this.controlador = controlador;

        setTitle("SpeedFast - Lista de Pedidos");
        setContentPane(VentanaListaPedidos);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 460);
        setLocationRelativeTo(null);

        inicializarTabla();
        refrescarTabla(true);

        btnRefrescar.addActionListener(e -> refrescarTabla(true));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                refrescarTabla(false);
            }
        });
    }

    private void inicializarTabla() {
        String[] columnas = {"ID", "Dirección", "Tipo", "Repartidor", "Estado", "Estimado (min)"};

        model = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblPedidos.setModel(model);
        tblPedidos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void refrescarTabla(boolean mostrarError) {
        if (model == null) {
            return;
        }

        try {
            model.setRowCount(0);

            for (Pedido pedido : controlador.getPedidos()) {
                model.addRow(new Object[]{pedido.getIdPedido(), pedido.getDireccionEntrega(), pedido.getTipoPedido(), pedido.getRepartidorAsignado(), pedido.getEstado(), pedido.calcularTiempoEntrega()});
            }
        } catch (IllegalStateException ex) {
            if (mostrarError) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
