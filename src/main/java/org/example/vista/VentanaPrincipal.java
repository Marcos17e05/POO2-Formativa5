package org.example.vista;

import org.example.controlador.ControladorPedidos;

import javax.swing.*;

public class VentanaPrincipal extends JFrame {
    private JPanel VentanaPrincipal;
    private JLabel lblTitulo;
    private JLabel lblSubtitulo;
    private JButton btnRegistrarPedido;
    private JButton btnRegistrarRepartidor;
    private JButton btnListarPedidos;
    private JButton btnAsignarEntrega;

    private final ControladorPedidos controlador;

    public VentanaPrincipal() {
        this(new ControladorPedidos());
    }

    public VentanaPrincipal(ControladorPedidos controlador) {
        this.controlador = controlador;

        setTitle("SpeedFast - Gestión de Entregas");
        setContentPane(VentanaPrincipal);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(560, 420);
        setLocationRelativeTo(null);
        setResizable(false);

        btnRegistrarPedido.addActionListener(e -> abrirRegistroPedido());
        btnRegistrarRepartidor.addActionListener(e -> abrirRegistroRepartidor());
        btnListarPedidos.addActionListener(e -> abrirListado());
        btnAsignarEntrega.addActionListener(e -> abrirAsignacion());
    }

    private void abrirRegistroPedido() {
        VentanaRegistroPedido ventana = new VentanaRegistroPedido(controlador);
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }

    private void abrirRegistroRepartidor() {
        VentanaRegistroRepartidor ventana = new VentanaRegistroRepartidor(controlador);
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }

    private void abrirListado() {
        VentanaListaPedidos ventana = new VentanaListaPedidos(controlador);
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }

    private void abrirAsignacion() {
        VentanaAsignacionEntrega ventana = new VentanaAsignacionEntrega(controlador);
        ventana.setLocationRelativeTo(this);
        ventana.setVisible(true);
    }
}
