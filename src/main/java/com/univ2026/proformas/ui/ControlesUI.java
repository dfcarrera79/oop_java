package com.univ2026.proformas.ui;

import com.univ2026.proformas.dominio.valor.Monto;
import java.math.BigDecimal;
import java.util.Optional;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

final class ControlesUI {
    private ControlesUI() {}

    static VBox seccion(String titulo, Node... contenido) {
        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("section-title");
        VBox caja = new VBox(12, encabezado);
        caja.getChildren().addAll(contenido);
        caja.getStyleClass().add("card");
        return caja;
    }

    static void error(Window owner, String titulo, Throwable error) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.initOwner(owner);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje(error));
        alerta.showAndWait();
    }

    static void informacion(Window owner, String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.initOwner(owner);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    static boolean confirmar(Window owner, String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.CANCEL, ButtonType.OK);
        alerta.initOwner(owner);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        Optional<ButtonType> resultado = alerta.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    static String dinero(BigDecimal valor) {
        return "$" + Monto.normalizar(valor).toPlainString();
    }

    private static String mensaje(Throwable error) {
        Throwable actual = error;
        while ((actual.getMessage() == null || actual.getMessage().isBlank()) && actual.getCause() != null) {
            actual = actual.getCause();
        }
        return actual.getMessage() == null ? "Ocurrio un error inesperado." : actual.getMessage();
    }
}
