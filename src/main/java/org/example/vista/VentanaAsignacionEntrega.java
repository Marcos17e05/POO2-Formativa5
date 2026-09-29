package org.example.vista;

import org.example.controlador.ControladorPedidos;
import org.example.modelo.Pedido;
import org.example.modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VentanaAsignacionEntrega extends JFrame {
    private JPanel VentanaAsignacionEntrega;
    private JLabel lblTitulo;
    private JTable tblPedidos;
    private JLabel lblRepartidor;
    private JComboBox<Repartidor> cmbRepartidor;
    private JButton btnIniciarEntrega;
    private JButton btnRefrescar;

    private final ControladorPedidos controlador;
    private DefaultTableModel model;
    private Timer temporizadorRefresco;

    public VentanaAsignacionEntrega(ControladorPedidos controlador) {
        this.controlador = controlador;

        setTitle("SpeedFast - Asignar Repartidor / Iniciar Entrega");
        setContentPane(VentanaAsignacionEntrega);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 480);
        setLocationRelativeTo(null);

        inicializarTabla();
        cargarRepartidores(true);
        refrescarTabla(true);
        inicializarBotones();

        temporizadorRefresco = new Timer(1000, e -> refrescarTabla(false));
        temporizadorRefresco.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                temporizadorRefresco.stop();
            }
        });
    }

    private void inicializarTabla() {
        String[] columnas = {"ID", "Dirección", "Tipo", "Repartidor", "Estado"};

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

    private void cargarRepartidores(boolean mostrarError) {
        try {
            Object seleccionado = cmbRepartidor.getSelectedItem();
            cmbRepartidor.removeAllItems();

            for (Repartidor repartidor : controlador.getRepartidores()) {
                cmbRepartidor.addItem(repartidor);
            }

            if (seleccionado instanceof Repartidor) {
                Repartidor anterior = (Repartidor) seleccionado;
                for (int i = 0; i < cmbRepartidor.getItemCount(); i++) {
                    if (cmbRepartidor.getItemAt(i).getId() == anterior.getId()) {
                        cmbRepartidor.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (IllegalStateException ex) {
            if (mostrarError) {
                mostrarErrorBD(ex);
            }
        }
    }

    private void inicializarBotones() {
        btnIniciarEntrega.addActionListener(e -> iniciarEntrega());
        btnRefrescar.addActionListener(e -> {
            cargarRepartidores(true);
            refrescarTabla(true);
        });
    }

    private void iniciarEntrega() {
        int fila = tblPedidos.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido de la tabla.", "Pedido no seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Repartidor repartidor = (Repartidor) cmbRepartidor.getSelectedItem();
        if (repartidor == null) {
            JOptionPane.showMessageDialog(this, "No hay repartidores registrados. Registre uno desde la ventana principal.", "Sin repartidores", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPedido = (int) model.getValueAt(fila, 0);

        try {
            controlador.iniciarEntrega(idPedido, repartidor);
            refrescarTabla(true);

            JOptionPane.showMessageDialog(this, "Pedido #" + idPedido + " asignado a " + repartidor.getNombre() + ".\nLa entrega quedó registrada en MySQL.", "Entrega iniciada", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo iniciar la entrega", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refrescarTabla(boolean mostrarError) {
        if (model == null) {
            return;
        }

        Integer idSeleccionado = null;
        int filaSeleccionada = tblPedidos.getSelectedRow();

        if (filaSeleccionada >= 0 && filaSeleccionada < model.getRowCount()) {
            idSeleccionado = (Integer) model.getValueAt(filaSeleccionada, 0);
        }

        try {
            model.setRowCount(0);
            int filaObjetivo = -1;
            int filaActual = 0;

            for (Pedido pedido : controlador.getPedidos()) {
                model.addRow(new Object[]{pedido.getIdPedido(), pedido.getDireccionEntrega(), pedido.getTipoPedido(), pedido.getRepartidorAsignado(), pedido.getEstado()});

                if (idSeleccionado != null && pedido.getIdPedido() == idSeleccionado) {
                    filaObjetivo = filaActual;
                }

                filaActual++;
            }

            if (filaObjetivo >= 0) {
                tblPedidos.setRowSelectionInterval(filaObjetivo, filaObjetivo);
            }
        } catch (IllegalStateException ex) {
            if (mostrarError) {
                mostrarErrorBD(ex);
            }
        }
    }

    private void mostrarErrorBD(IllegalStateException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }
}
