package com.univ2026.proformas.ui;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.producto.AtributosProducto;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.producto.TarifasIva;
import com.univ2026.proformas.dominio.valor.Monto;
import java.math.BigDecimal;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/** Formulario unico para crear y editar productos desde cualquier vista. */
public final class FormularioProducto extends GridPane {
    private final TextField codigo = new TextField();
    private final TextField nombre = new TextField();
    private final TextArea descripcion = new TextArea();
    private final TextField precio = new TextField();
    private final ComboBox<Integer> iva = new ComboBox<>();
    private final CheckBox incluyeIva = new CheckBox("El precio ingresado incluye IVA");
    private final Label vistaPrevia = new Label();
    private Estado estado = Estado.ACTIVO;
    private AtributosProducto extras;

    public FormularioProducto(Producto producto) {
        setHgap(12);
        setVgap(10);
        setPadding(new Insets(8));
        descripcion.setPrefRowCount(3);
        iva.getItems().setAll(TarifasIva.disponibles());
        iva.getSelectionModel().select(Integer.valueOf(15));
        vistaPrevia.getStyleClass().add("price-preview");
        agregarFila(0, "Codigo", codigo);
        agregarFila(1, "Nombre", nombre);
        agregarFila(2, "Descripcion", descripcion);
        agregarFila(3, "Precio", precio);
        agregarFila(4, "IVA", iva);
        add(incluyeIva, 1, 5);
        add(vistaPrevia, 1, 6);
        precio.textProperty().addListener(this::actualizarVistaPrevia);
        iva.valueProperty().addListener(this::actualizarVistaPrevia);
        incluyeIva.selectedProperty().addListener(this::actualizarVistaPrevia);
        if (producto != null) {
            cargar(producto);
        }
        actualizarVistaPrevia(null, null, null);
    }

    public Producto crearProducto() {
        int tasa = iva.getValue() == null ? -1 : iva.getValue();
        Monto capturado = new Monto(precio.getText());
        BigDecimal base = Monto.convertirAPrecioBase(capturado.valor(), tasa, incluyeIva.isSelected());
        return new Producto(
                codigo.getText(), nombre.getText(), descripcion.getText(), new Monto(base), tasa, estado, extras);
    }

    public static Optional<Producto> mostrar(Window owner, Producto producto) {
        FormularioProducto formulario = new FormularioProducto(producto);
        Dialog<Producto> dialogo = new Dialog<>();
        dialogo.initOwner(owner);
        dialogo.setTitle(producto == null ? "Nuevo producto" : "Editar producto");
        dialogo.getDialogPane().setContent(formulario);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialogo.setResultConverter(boton -> boton == ButtonType.OK ? formulario.crearProducto() : null);
        dialogo.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, evento -> {
            try {
                formulario.crearProducto();
            } catch (RuntimeException error) {
                evento.consume();
                ControlesUI.error(dialogo.getDialogPane().getScene().getWindow(), "Revise el producto", error);
            }
        });
        return dialogo.showAndWait();
    }

    private void cargar(Producto producto) {
        codigo.setText(producto.getCodigo());
        codigo.setDisable(true);
        nombre.setText(producto.getNombre());
        descripcion.setText(producto.getDescripcion());
        precio.setText(producto.getPrecio().toString());
        iva.setValue(producto.getIvaPct());
        estado = producto.getEstado();
        extras = producto.getExtras();
    }

    private void agregarFila(int fila, String etiqueta, javafx.scene.Node campo) {
        add(new Label(etiqueta), 0, fila);
        add(campo, 1, fila);
    }

    private void actualizarVistaPrevia(ObservableValue<?> observable, Object anterior, Object nuevo) {
        try {
            int tasa = iva.getValue() == null ? -1 : iva.getValue();
            BigDecimal base =
                    Monto.convertirAPrecioBase(new Monto(precio.getText()).valor(), tasa, incluyeIva.isSelected());
            vistaPrevia.setText("Precio sin IVA que se guardara: " + ControlesUI.dinero(base));
        } catch (RuntimeException error) {
            vistaPrevia.setText("Precio sin IVA que se guardara: --");
        }
    }
}
