package com.univ2026.proformas.ui;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/** Formulario unico para crear y editar clientes desde cualquier vista. */
public final class FormularioCliente extends GridPane {
    private final TextField identificacion = new TextField();
    private final TextField nombre = new TextField();
    private final TextArea direccion = new TextArea();
    private final TextField telefono = new TextField();
    private final TextField email = new TextField();
    private final ComboBox<TipoCliente> tipo = new ComboBox<>();
    private final ComboBox<Estado> estado = new ComboBox<>();

    public FormularioCliente(Cliente cliente) {
        setHgap(12);
        setVgap(10);
        setPadding(new Insets(8));
        direccion.setPrefRowCount(2);
        tipo.getItems().setAll(TipoCliente.values());
        estado.getItems().setAll(Estado.values());
        tipo.setValue(TipoCliente.PUBLICO);
        estado.setValue(Estado.ACTIVO);
        agregarFila(0, "Identificacion", identificacion);
        agregarFila(1, "Nombre", nombre);
        agregarFila(2, "Direccion", direccion);
        agregarFila(3, "Telefono", telefono);
        agregarFila(4, "Correo", email);
        agregarFila(5, "Tipo", tipo);
        agregarFila(6, "Estado", estado);
        if (cliente != null) {
            cargar(cliente);
        }
    }

    public Cliente crearCliente() {
        return new Cliente(
                identificacion.getText(),
                nombre.getText(),
                direccion.getText(),
                telefono.getText(),
                email.getText(),
                tipo.getValue(),
                estado.getValue());
    }

    public static Optional<Cliente> mostrar(Window owner, Cliente cliente) {
        FormularioCliente formulario = new FormularioCliente(cliente);
        Dialog<Cliente> dialogo = new Dialog<>();
        dialogo.initOwner(owner);
        dialogo.setTitle(cliente == null ? "Nuevo cliente" : "Editar cliente");
        dialogo.getDialogPane().setContent(formulario);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialogo.setResultConverter(boton -> boton == ButtonType.OK ? formulario.crearCliente() : null);
        dialogo.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, evento -> {
            try {
                formulario.crearCliente();
            } catch (RuntimeException error) {
                evento.consume();
                ControlesUI.error(dialogo.getDialogPane().getScene().getWindow(), "Revise el cliente", error);
            }
        });
        return dialogo.showAndWait();
    }

    private void cargar(Cliente cliente) {
        identificacion.setText(cliente.getIdentificacion().valor());
        identificacion.setDisable(true);
        nombre.setText(cliente.getNombre());
        direccion.setText(cliente.getDireccion());
        telefono.setText(cliente.getTelefono());
        email.setText(cliente.getEmail().valor());
        tipo.setValue(cliente.getTipo());
        estado.setValue(cliente.getEstado());
    }

    private void agregarFila(int fila, String etiqueta, Node campo) {
        add(new Label(etiqueta), 0, fila);
        add(campo, 1, fila);
    }
}
