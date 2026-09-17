package com.univ2026.proformas.ui;

import com.univ2026.proformas.aplicacion.AplicacionProformas;
import com.univ2026.proformas.dominio.producto.Producto;
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

/** Catalogo visual de productos. */
public final class VistaProductos extends VBox {
    private final AplicacionProformas aplicacion;
    private final Runnable alCambiar;
    private final TextField busqueda = new TextField();
    private final TableView<Producto> tabla = new TableView<>();

    public VistaProductos(AplicacionProformas aplicacion, Runnable alCambiar) {
        this.aplicacion = aplicacion;
        this.alCambiar = alCambiar;
        setSpacing(16);
        Label titulo = new Label("Productos");
        titulo.getStyleClass().add("page-title");
        busqueda.setPromptText("Buscar por codigo, nombre o descripcion");
        busqueda.setOnAction(evento -> refrescar());
        Button buscar = new Button("Buscar");
        buscar.setOnAction(evento -> refrescar());
        Button nuevo = new Button("Nuevo producto");
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
        getChildren().addAll(titulo, barra, ControlesUI.seccion("Catalogo", tabla, new HBox(10, editar, eliminar)));
        refrescar();
    }

    public void refrescar() {
        try {
            tabla.setItems(FXCollections.observableArrayList(
                    busqueda.getText().isBlank()
                            ? aplicacion.listarProductos()
                            : aplicacion.buscarProductos(busqueda.getText())));
        } catch (RuntimeException error) {
            ControlesUI.error(
                    getScene() == null ? null : getScene().getWindow(), "No se pudieron cargar los productos", error);
        }
    }

    private void configurarTabla() {
        TableColumn<Producto, String> codigo = columna("Codigo", 110, Producto::getCodigo);
        TableColumn<Producto, String> nombre = columna("Nombre", 180, Producto::getNombre);
        TableColumn<Producto, String> descripcion = columna("Descripcion", 260, Producto::getDescripcion);
        TableColumn<Producto, String> precio = columna(
                "Precio sin IVA",
                130,
                producto -> ControlesUI.dinero(producto.getPrecio().valor()));
        TableColumn<Producto, String> iva = columna("IVA", 70, producto -> producto.getIvaPct() + "%");
        TableColumn<Producto, String> estado =
                columna("Estado", 90, producto -> producto.getEstado().getEtiqueta());
        tabla.getColumns().setAll(List.of(codigo, nombre, descripcion, precio, iva, estado));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPrefHeight(520);
    }

    private void crear() {
        try {
            FormularioProducto.mostrar(getScene().getWindow(), null).ifPresent(producto -> {
                try {
                    aplicacion.registrarProducto(producto);
                    despuesDeCambio();
                } catch (RuntimeException error) {
                    ControlesUI.error(getScene().getWindow(), "No se pudo crear el producto", error);
                }
            });
        } catch (RuntimeException error) {
            ControlesUI.error(getScene().getWindow(), "No se pudo crear el producto", error);
        }
    }

    private void editar() {
        Producto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            ControlesUI.informacion(getScene().getWindow(), "Seleccione un producto", "Elija una fila para editarla.");
            return;
        }
        FormularioProducto.mostrar(getScene().getWindow(), seleccionado).ifPresent(producto -> {
            try {
                aplicacion.actualizarProducto(producto);
                despuesDeCambio();
            } catch (RuntimeException error) {
                ControlesUI.error(getScene().getWindow(), "No se pudo actualizar el producto", error);
            }
        });
    }

    private void eliminar() {
        Producto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            ControlesUI.informacion(
                    getScene().getWindow(), "Seleccione un producto", "Elija una fila para eliminarla.");
            return;
        }
        if (!ControlesUI.confirmar(
                getScene().getWindow(),
                "Eliminar producto",
                "Se eliminara " + seleccionado.getCodigo() + " - " + seleccionado.getNombre() + ".")) {
            return;
        }
        try {
            aplicacion.eliminarProducto(seleccionado.getCodigo());
            despuesDeCambio();
        } catch (RuntimeException error) {
            ControlesUI.error(getScene().getWindow(), "No se pudo eliminar el producto", error);
        }
    }

    private void despuesDeCambio() {
        refrescar();
        alCambiar.run();
    }

    private static <T> TableColumn<Producto, T> columna(
            String titulo, double ancho, java.util.function.Function<Producto, T> valor) {
        TableColumn<Producto, T> columna = new TableColumn<>(titulo);
        columna.setPrefWidth(ancho);
        columna.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(valor.apply(celda.getValue())));
        return columna;
    }
}
