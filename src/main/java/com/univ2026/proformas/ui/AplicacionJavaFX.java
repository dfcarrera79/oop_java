package com.univ2026.proformas.ui;

import com.univ2026.proformas.aplicacion.AplicacionProformas;
import com.univ2026.proformas.exportacion.ExportadorProformaPDF;
import com.univ2026.proformas.persistencia.sqlite.BaseDatosSQLite;
import com.univ2026.proformas.persistencia.sqlite.CatalogoProductosSQLite;
import com.univ2026.proformas.persistencia.sqlite.RegistroClientesSQLite;
import com.univ2026.proformas.persistencia.sqlite.RepositorioCuentasPagoSQLite;
import com.univ2026.proformas.persistencia.sqlite.RepositorioProformasSQLite;
import com.univ2026.proformas.persistencia.sqlite.RutasDatos;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Aplicacion de escritorio JavaFX */
public final class AplicacionJavaFX extends Application {
    public static final String PROPIEDAD_BASE_DATOS = "proformas.db";
    public static final String PROPIEDAD_MODO = "proformas.modo";

    public static void lanzar(String[] argumentos) {
        launch(argumentos);
    }

    @Override
    public void start(Stage escenario) {
        Path ruta = resolverRutaDatos();
        AplicacionProformas aplicacion = crearAplicacion(ruta);
        Path directorioPdf = ruta.toAbsolutePath().normalize().getParent().resolve("pdf");
        ExportacionPDF exportacion = new ExportadorProformaPDF(directorioPdf)::exportar;
        VistaProformas vistaProformas = new VistaProformas(aplicacion, exportacion);
        VistaProductos vistaProductos = new VistaProductos(aplicacion, vistaProformas::refrescarCatalogos);
        VistaClientes vistaClientes = new VistaClientes(aplicacion, () -> {});

        BorderPane raiz = new BorderPane();
        raiz.getStyleClass().add("app-shell");
        Label titulo = new Label("Gestion de proformas");
        titulo.getStyleClass().add("app-title");
        Label ubicacion = new Label("Datos: " + ruta.toAbsolutePath().normalize());
        ubicacion.getStyleClass().add("app-subtitle");
        VBox encabezado = new VBox(3, titulo, ubicacion);
        encabezado.getStyleClass().add("app-header");
        raiz.setTop(encabezado);

        Button proformas = botonNavegacion("Proformas");
        Button productos = botonNavegacion("Productos");
        Button clientes = botonNavegacion("Clientes");
        VBox navegacion = new VBox(8, new Label("NAVEGACION"), proformas, productos, clientes, new Region());
        navegacion.getStyleClass().add("side-nav");
        VBox.setVgrow(navegacion.getChildren().get(navegacion.getChildren().size() - 1), Priority.ALWAYS);
        raiz.setLeft(navegacion);

        proformas.setOnAction(evento -> mostrar(raiz, vistaProformas));
        productos.setOnAction(evento -> {
            vistaProductos.refrescar();
            mostrar(raiz, vistaProductos);
        });
        clientes.setOnAction(evento -> {
            vistaClientes.refrescar();
            mostrar(raiz, vistaClientes);
        });
        mostrar(raiz, vistaProformas);

        Scene escena = new Scene(raiz, 1240, 820);
        String estilos = AplicacionJavaFX.class.getResource("/ui/proformas.css").toExternalForm();
        escena.getStylesheets().add(estilos);
        escenario.setTitle("Gestion de Proformas");
        escenario.setMinWidth(680);
        escenario.setMinHeight(600);
        escenario.setScene(escena);
        escenario.setMaximized(true);
        escenario.show();
    }

    public static AplicacionProformas crearAplicacion(Path ruta) {
        BaseDatosSQLite baseDatos = new BaseDatosSQLite(ruta);
        return new AplicacionProformas(
                new CatalogoProductosSQLite(baseDatos),
                new RegistroClientesSQLite(baseDatos),
                new RepositorioCuentasPagoSQLite(baseDatos),
                new RepositorioProformasSQLite(baseDatos));
    }

    public static Path resolverRutaDatos() {
        String configurada = System.getProperty(PROPIEDAD_BASE_DATOS);
        if (configurada != null && !configurada.isBlank()) {
            return Path.of(configurada.trim());
        }
        String modo = System.getProperty(PROPIEDAD_MODO, "").trim();
        if (modo.equalsIgnoreCase("produccion")) {
            return RutasDatos.produccion();
        }
        if (modo.equalsIgnoreCase("desarrollo") || ejecutandoDesdeMaven()) {
            return RutasDatos.desarrollo();
        }
        return RutasDatos.produccion();
    }

    private static boolean ejecutandoDesdeMaven() {
        String clases = System.getProperty("java.class.path", "");
        return System.getProperty("maven.multiModuleProjectDirectory") != null
                || clases.contains("target/classes")
                || clases.contains("target\\classes");
    }

    private static Button botonNavegacion(String texto) {
        Button boton = new Button(texto);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.getStyleClass().add("nav-button");
        return boton;
    }

    private static void mostrar(BorderPane raiz, Node vista) {
        ScrollPane desplazamiento = new ScrollPane(vista);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setPannable(true);
        desplazamiento.setPadding(new Insets(0));
        desplazamiento.getStyleClass().add("content-scroll");
        raiz.setCenter(desplazamiento);
    }
}
