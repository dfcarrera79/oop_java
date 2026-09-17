package com.univ2026.proformas.ui;

import com.univ2026.proformas.aplicacion.AplicacionProformas;
import com.univ2026.proformas.dominio.cliente.Cliente;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Registro visual de clientes. */
public final class VistaClientes extends VBox {
    private final AplicacionProformas aplicacion;
    private final Runnable alCambiar;
    private final TextField busqueda = new TextField();
    private final TableView<Cliente> tabla = new TableView<>();

    public VistaClientes(AplicacionProformas aplicacion, Runnable alCambiar) {
        this.aplicacion = aplicacion;
        this.alCambiar = alCambiar;
        setSpacing(16);
        Label titulo = new Label("Clientes");
        titulo.getStyleClass().add("page-title");
        busqueda.setPromptText("Buscar por identificacion o nombre");
        busqueda.setOnAction(evento -> refrescar());
        Button buscar = new Button("Buscar");
        buscar.setOnAction(evento -> refrescar());
        Button nuevo = new Button("Nuevo cliente");
        nuevo.getStyleClass().add("primary-button");
        nuevo.setOnAction(evento -> crear());
        HBox barra = new HBox(10, busqueda, buscar, nuevo);
        barra.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(busqueda, Priority.ALWAYS);
        configurarTabla();
        Button editar = new Button("Editar seleccionado");
        editar.setOnAction(evento -> editar());
        Button eliminar = new Button("Eliminar seleccionado");
        eliminar.getStyleClass().add("danger-button");
        eliminar.setOnAction(evento -> eliminar());
        getChildren().addAll(titulo, barra, ControlesUI.seccion("Registro", tabla, new HBox(10, editar, eliminar)));
        refrescar();
    }

    public void refrescar() {
        try {
            tabla.setItems(FXCollections.observableArrayList(
                    busqueda.getText().isBlank()
                            ? aplicacion.listarClientes()
                            : aplicacion.buscarClientes(busqueda.getText())));
        } catch (RuntimeException error) {
            ControlesUI.error(
                    getScene() == null ? null : getScene().getWindow(), "No se pudieron cargar los clientes", error);
        }
    }

    private void configurarTabla() {
        tabla.getColumns()
                .setAll(List.of(
                        columna(
                                "Identificacion",
                                130,
                                cliente -> cliente.getIdentificacion().valor()),
                        columna("Nombre", 180, Cliente::getNombre),
                        columna("Direccion", 220, Cliente::getDireccion),
                        columna("Telefono", 120, Cliente::getTelefono),
                        columna("Correo", 190, cliente -> cliente.getEmail().valor()),
                        columna("Tipo", 100, cliente -> cliente.getTipo().getEtiqueta()),
                        columna("Estado", 90, cliente -> cliente.getEstado().getEtiqueta())));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPrefHeight(520);
    }

    private void crear() {
        FormularioCliente.mostrar(getScene().getWindow(), null).ifPresent(cliente -> {
            try {
                aplicacion.registrarCliente(cliente);
                despuesDeCambio();
            } catch (RuntimeException error) {
                ControlesUI.error(getScene().getWindow(), "No se pudo crear el cliente", error);
            }
        });
    }

    private void editar() {
        Cliente seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            ControlesUI.informacion(getScene().getWindow(), "Seleccione un cliente", "Elija una fila para editarla.");
            return;
        }
        FormularioCliente.mostrar(getScene().getWindow(), seleccionado).ifPresent(cliente -> {
            try {
                aplicacion.actualizarCliente(cliente);
                despuesDeCambio();
            } catch (RuntimeException error) {
                ControlesUI.error(getScene().getWindow(), "No se pudo actualizar el cliente", error);
            }
        });
    }

    private void eliminar() {
        Cliente seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            ControlesUI.informacion(getScene().getWindow(), "Seleccione un cliente", "Elija una fila para eliminarla.");
            return;
        }
        if (!ControlesUI.confirmar(
                getScene().getWindow(),
                "Eliminar cliente",
                "Se eliminara " + seleccionado.getIdentificacion() + " - " + seleccionado.getNombre() + ".")) {
            return;
        }
        try {
            aplicacion.eliminarCliente(seleccionado.getIdentificacion().valor());
            despuesDeCambio();
        } catch (RuntimeException error) {
            ControlesUI.error(getScene().getWindow(), "No se pudo eliminar el cliente", error);
        }
    }

    private void despuesDeCambio() {
        refrescar();
        alCambiar.run();
    }

    private static TableColumn<Cliente, String> columna(
            String titulo, double ancho, java.util.function.Function<Cliente, String> valor) {
        TableColumn<Cliente, String> columna = new TableColumn<>(titulo);
        columna.setPrefWidth(ancho);
        columna.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(valor.apply(celda.getValue())));
        return columna;
    }
}
