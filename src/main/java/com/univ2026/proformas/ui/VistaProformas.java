package com.univ2026.proformas.ui;

import com.univ2026.proformas.aplicacion.AplicacionProformas;
import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.pago.CuentaPago;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.producto.Talla;
import com.univ2026.proformas.dominio.proforma.ItemProforma;
import com.univ2026.proformas.dominio.proforma.Proforma;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** Creacion, consulta y exportacion de proformas. */
public final class VistaProformas extends VBox {
    private final AplicacionProformas aplicacion;
    private final ExportacionPDF exportacion;
    private final TextField buscarCliente = new TextField();
    private final Label fichaCliente = new Label("Ningun cliente seleccionado");
    private final ComboBox<Producto> productos = new ComboBox<>();
    private final TextField cantidad = new TextField("1");
    private final ComboBox<Talla> talla = new ComboBox<>();
    private final ObservableList<ItemProforma> items = FXCollections.observableArrayList();
    private final TableView<ItemProforma> tablaItems = new TableView<>(items);
    private final Label resumen = new Label();
    private final DatePicker fecha = new DatePicker(LocalDate.now());
    private final TextArea observaciones = new TextArea();
    private final ComboBox<CuentaPago> cuentas = new ComboBox<>();
    private final TextArea instrucciones = new TextArea();
    private final TextField buscarGuardadas = new TextField();
    private final TableView<Proforma> tablaGuardadas = new TableView<>();
    private Cliente clienteSeleccionado;

    public VistaProformas(AplicacionProformas aplicacion, ExportacionPDF exportacion) {
        this.aplicacion = aplicacion;
        this.exportacion = exportacion;
        setSpacing(18);
        Label titulo = new Label("Proformas");
        titulo.getStyleClass().add("page-title");
        getChildren().addAll(titulo, construirCliente(), construirItems(), construirDetalles(), construirGuardadas());
        configurarConvertidores();
        refrescarCatalogos();
        refrescarGuardadas();
        actualizarResumen();
    }

    public void refrescarCatalogos() {
        Producto productoActual = productos.getValue();
        CuentaPago cuentaActual = cuentas.getValue();
        try {
            productos.setItems(FXCollections.observableArrayList(aplicacion.listarProductos().stream()
                    .filter(producto -> producto.getEstado() == Estado.ACTIVO)
                    .toList()));
            cuentas.setItems(FXCollections.observableArrayList(aplicacion.listarCuentas()));
            seleccionarProducto(productoActual == null ? null : productoActual.getCodigo());
            seleccionarCuenta(cuentaActual == null ? null : cuentaActual.getId());
        } catch (RuntimeException error) {
            ControlesUI.error(
                    getScene() == null ? null : getScene().getWindow(), "No se cargaron los catalogos", error);
        }
    }

    private VBox construirCliente() {
        buscarCliente.setPromptText("Identificacion o nombre del cliente");
        buscarCliente.setOnAction(evento -> buscarYSeleccionarCliente());
        Button buscar = new Button("Buscar y seleccionar");
        buscar.setOnAction(evento -> buscarYSeleccionarCliente());
        Button crear = new Button("Crear cliente");
        crear.setOnAction(evento -> crearCliente());
        HBox barra = new HBox(10, buscarCliente, buscar, crear);
        barra.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(buscarCliente, Priority.ALWAYS);
        fichaCliente.setWrapText(true);
        fichaCliente.getStyleClass().add("client-card");
        return ControlesUI.seccion("1. Cliente", barra, fichaCliente);
    }

    private VBox construirItems() {
        productos.setMaxWidth(Double.MAX_VALUE);
        cantidad.setPrefWidth(80);
        talla.getItems().setAll(Talla.values());
        talla.setPromptText("Sin talla");
        Button crearProducto = new Button("Crear producto");
        crearProducto.setOnAction(evento -> crearProducto());
        Button agregar = new Button("Agregar item");
        agregar.getStyleClass().add("primary-button");
        agregar.setOnAction(evento -> agregarItem());
        HBox seleccion = new HBox(
                10,
                new Label("Producto"),
                productos,
                crearProducto,
                new Label("Cantidad"),
                cantidad,
                new Label("Talla"),
                talla,
                agregar);
        seleccion.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(productos, Priority.ALWAYS);
        configurarTablaItems();
        Button quitar = new Button("Quitar seleccionado");
        quitar.getStyleClass().add("danger-button");
        quitar.setOnAction(evento -> quitarItem());
        resumen.getStyleClass().add("summary");
        HBox pie = new HBox(14, quitar, resumen);
        pie.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(quitar, Priority.ALWAYS);
        return ControlesUI.seccion("2. Items", seleccion, tablaItems, pie);
    }

    private VBox construirDetalles() {
        observaciones.setPromptText("Observaciones para el cliente");
        observaciones.setPrefRowCount(3);
        instrucciones.setPromptText("Instrucciones de pago que quedaran en la proforma");
        instrucciones.setPrefRowCount(4);
        Button cargar = new Button("Cargar cuenta");
        cargar.setOnAction(evento -> cargarCuenta());
        Button guardarCuenta = new Button("Guardar como nueva");
        guardarCuenta.setOnAction(evento -> guardarCuenta());
        Button editarCuenta = new Button("Editar cuenta");
        editarCuenta.setOnAction(evento -> editarCuenta());
        Button eliminarCuenta = new Button("Eliminar cuenta");
        eliminarCuenta.getStyleClass().add("danger-button");
        eliminarCuenta.setOnAction(evento -> eliminarCuenta());
        HBox barraCuentas = new HBox(10, cuentas, cargar, guardarCuenta, editarCuenta, eliminarCuenta);
        HBox.setHgrow(cuentas, Priority.ALWAYS);
        Button guardar = new Button("Guardar proforma");
        guardar.getStyleClass().add("primary-button");
        guardar.setOnAction(evento -> guardarProforma());
        GridPane campos = new GridPane();
        campos.setHgap(12);
        campos.setVgap(10);
        campos.addRow(0, new Label("Fecha"), fecha);
        campos.addRow(1, new Label("Observaciones"), observaciones);
        campos.addRow(2, new Label("Cuenta de pago"), barraCuentas);
        campos.addRow(3, new Label("Instrucciones"), instrucciones);
        GridPane.setHgrow(observaciones, Priority.ALWAYS);
        GridPane.setHgrow(barraCuentas, Priority.ALWAYS);
        GridPane.setHgrow(instrucciones, Priority.ALWAYS);
        return ControlesUI.seccion("3. Detalles y pago", campos, guardar);
    }

    private VBox construirGuardadas() {
        buscarGuardadas.setPromptText("Numero, identificacion o cliente");
        buscarGuardadas.setOnAction(evento -> refrescarGuardadas());
        Button buscar = new Button("Buscar");
        buscar.setOnAction(evento -> refrescarGuardadas());
        HBox barra = new HBox(10, buscarGuardadas, buscar);
        HBox.setHgrow(buscarGuardadas, Priority.ALWAYS);
        configurarTablaGuardadas();
        Button exportar = new Button("Exportar PDF");
        exportar.setOnAction(evento -> exportarSeleccionada());
        Button eliminar = new Button("Eliminar guardada");
        eliminar.getStyleClass().add("danger-button");
        eliminar.setOnAction(evento -> eliminarGuardada());
        return ControlesUI.seccion("Proformas guardadas", barra, tablaGuardadas, new HBox(10, exportar, eliminar));
    }

    private void configurarTablaItems() {
        tablaItems
                .getColumns()
                .setAll(List.of(
                        columnaItem("Cantidad", 75, item -> item.getCantidad()),
                        columnaItem("Codigo", 100, item -> item.getProducto().getCodigo()),
                        columnaItem(
                                "Descripcion",
                                210,
                                item -> item.getProducto().getNombre()
                                        + (item.getProducto().getDescripcion().isBlank()
                                                ? ""
                                                : " - " + item.getProducto().getDescripcion())),
                        columnaItem(
                                "Talla",
                                70,
                                item -> item.getTalla() == null
                                        ? "-"
                                        : item.getTalla().getEtiqueta()),
                        columnaItem(
                                "Precio",
                                90,
                                item -> ControlesUI.dinero(
                                        item.getProducto().getPrecio().valor())),
                        columnaItem(
                                "Descuento", 100, item -> item.getDescuentoPct().toPlainString() + "%"),
                        columnaItem("IVA", 70, item -> item.getProducto().getIvaPct() + "%"),
                        columnaItem("Total", 100, item -> ControlesUI.dinero(item.calcularTotal()))));
        tablaItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaItems.setPrefHeight(250);
    }

    private void configurarTablaGuardadas() {
        tablaGuardadas.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tablaGuardadas
                .getColumns()
                .setAll(List.of(
                        columnaProforma("Numero", 130, Proforma::getNumero),
                        columnaProforma(
                                "Fecha", 105, proforma -> proforma.getFecha().toString()),
                        columnaProforma(
                                "Identificacion",
                                130,
                                proforma -> proforma.getCliente()
                                        .getIdentificacion()
                                        .valor()),
                        columnaProforma(
                                "Cliente",
                                210,
                                proforma -> proforma.getCliente().getNombre()),
                        columnaProforma(
                                "Items",
                                70,
                                proforma -> Integer.toString(proforma.getItems().size())),
                        columnaProforma("Total", 110, proforma -> ControlesUI.dinero(proforma.calcularTotal()))));
        tablaGuardadas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaGuardadas.setPrefHeight(330);
    }

    private void configurarConvertidores() {
        productos.setConverter(new StringConverter<>() {
            @Override
            public String toString(Producto producto) {
                return producto == null ? "" : producto.getCodigo() + " - " + producto.getNombre();
            }

            @Override
            public Producto fromString(String texto) {
                return null;
            }
        });
        cuentas.setConverter(new StringConverter<>() {
            @Override
            public String toString(CuentaPago cuenta) {
                return cuenta == null ? "" : cuenta.getNombre();
            }

            @Override
            public CuentaPago fromString(String texto) {
                return null;
            }
        });
    }

    private void buscarYSeleccionarCliente() {
        try {
            List<Cliente> resultados = aplicacion.buscarClientes(buscarCliente.getText());
            if (resultados.isEmpty()) {
                ControlesUI.informacion(getScene().getWindow(), "Sin resultados", "No se encontro ningun cliente.");
                return;
            }
            Cliente elegido = resultados.size() == 1
                    ? resultados.get(0)
                    : elegirCliente(resultados).orElse(null);
            if (elegido != null) {
                seleccionarCliente(elegido);
            }
        } catch (RuntimeException error) {
            ControlesUI.error(getScene().getWindow(), "No se pudo buscar el cliente", error);
        }
    }

    private Optional<Cliente> elegirCliente(List<Cliente> clientes) {
        Dialog<Cliente> dialogo = new Dialog<>();
        dialogo.initOwner(getScene().getWindow());
        dialogo.setTitle("Elegir cliente");
        dialogo.setHeaderText("La busqueda tiene varios resultados. Elija uno.");
        ListView<Cliente> lista = new ListView<>(FXCollections.observableArrayList(clientes));
        lista.setPrefSize(560, 280);
        lista.setCellFactory(control -> new ListCell<>() {
            @Override
            protected void updateItem(Cliente cliente, boolean vacia) {
                super.updateItem(cliente, vacia);
                setText(
                        vacia || cliente == null
                                ? null
                                : cliente.getIdentificacion() + " | " + cliente.getNombre() + " | "
                                        + cliente.getEmail());
            }
        });
        dialogo.getDialogPane().setContent(lista);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialogo.setResultConverter(
                boton -> boton == ButtonType.OK ? lista.getSelectionModel().getSelectedItem() : null);
        return dialogo.showAndWait();
    }

    private void seleccionarCliente(Cliente cliente) {
        if (cliente.getEstado() != Estado.ACTIVO) {
            ControlesUI.informacion(
                    getScene().getWindow(),
                    "Cliente inactivo",
                    "Active el cliente desde el modulo Clientes antes de usarlo en una proforma.");
            return;
        }
        if (clienteSeleccionado != null
                && clienteSeleccionado.getIdentificacion().equals(cliente.getIdentificacion())) {
            return;
        }
        if (!items.isEmpty()
                && !ControlesUI.confirmar(
                        getScene().getWindow(),
                        "Cambiar cliente",
                        "Cambiar el cliente quitara los items porque su descuento ya fue calculado.")) {
            return;
        }
        items.clear();
        clienteSeleccionado = cliente;
        buscarCliente.setText(cliente.getIdentificacion().valor());
        fichaCliente.setText("%s | %s\n%s\nTelefono: %s | Correo: %s\nTipo: %s | Estado: %s"
                .formatted(
                        cliente.getIdentificacion(),
                        cliente.getNombre(),
                        cliente.getDireccion().isBlank() ? "Sin direccion" : cliente.getDireccion(),
                        cliente.getTelefono().isBlank() ? "-" : cliente.getTelefono(),
                        cliente.getEmail().valor().isBlank() ? "-" : cliente.getEmail(),
                        cliente.getTipo().getEtiqueta(),
                        cliente.getEstado().getEtiqueta()));
        actualizarResumen();
    }

    private void crearCliente() {
        FormularioCliente.mostrar(getScene().getWindow(), null).ifPresent(cliente -> {
            try {
                aplicacion.registrarCliente(cliente);
                seleccionarCliente(cliente);
            } catch (RuntimeException error) {
                ControlesUI.error(getScene().getWindow(), "No se pudo crear el cliente", error);
            }
        });
    }

    private void crearProducto() {
        FormularioProducto.mostrar(getScene().getWindow(), null).ifPresent(producto -> {
            try {
                aplicacion.registrarProducto(producto);
                refrescarCatalogos();
                seleccionarProducto(producto.getCodigo());
            } catch (RuntimeException error) {
                ControlesUI.error(getScene().getWindow(), "No se pudo crear el producto", error);
            }
        });
    }

    private void agregarItem() {
        if (clienteSeleccionado == null) {
            ControlesUI.informacion(
                    getScene().getWindow(),
                    "Seleccione un cliente",
                    "Debe seleccionar un cliente antes de agregar items.");
            return;
        }
        Producto producto = productos.getValue();
        if (producto == null) {
            ControlesUI.informacion(
                    getScene().getWindow(), "Seleccione un producto", "Elija un producto del catalogo.");
            return;
        }
        try {
            int unidades = Integer.parseInt(cantidad.getText().trim());
            Proforma temporal = aplicacion.crearProforma(
                    clienteSeleccionado.getIdentificacion().valor(), LocalDate.now(), "", "");
            ItemProforma item = aplicacion.agregarItem(temporal, producto.getCodigo(), unidades, talla.getValue());
            items.add(item);
            cantidad.setText("1");
            actualizarResumen();
        } catch (NumberFormatException error) {
            ControlesUI.error(
                    getScene().getWindow(),
                    "Cantidad invalida",
                    new IllegalArgumentException("La cantidad debe ser un entero positivo"));
        } catch (RuntimeException error) {
            ControlesUI.error(getScene().getWindow(), "No se pudo agregar el item", error);
        }
    }

    private void quitarItem() {
        ItemProforma item = tablaItems.getSelectionModel().getSelectedItem();
        if (item == null) {
            ControlesUI.informacion(getScene().getWindow(), "Seleccione un item", "Elija una fila para quitarla.");
            return;
        }
        items.remove(item);
        actualizarResumen();
    }

    private void actualizarResumen() {
        if (clienteSeleccionado == null || items.isEmpty()) {
            resumen.setText("Subtotal: $0.00   IVA: $0.00   Total: $0.00");
            return;
        }
        Proforma temporal = new Proforma(clienteSeleccionado, LocalDate.now(), "", "");
        items.forEach(temporal::agregarItem);
        resumen.setText("Subtotal: %s   IVA: %s   Total: %s"
                .formatted(
                        ControlesUI.dinero(temporal.calcularSubtotal()),
                        ControlesUI.dinero(temporal.calcularImpuesto()),
                        ControlesUI.dinero(temporal.calcularTotal())));
    }

    private void cargarCuenta() {
        CuentaPago cuenta = cuentas.getValue();
        if (cuenta == null) {
            ControlesUI.informacion(getScene().getWindow(), "Seleccione una cuenta", "Elija una cuenta para cargarla.");
            return;
        }
        instrucciones.setText(cuenta.getInstrucciones());
    }

    private void guardarCuenta() {
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.initOwner(getScene().getWindow());
        dialogo.setTitle("Nueva cuenta de pago");
        dialogo.setHeaderText("Guardar las instrucciones actuales como nueva cuenta");
        dialogo.setContentText("Nombre:");
        dialogo.showAndWait().ifPresent(nombre -> {
            try {
                CuentaPago guardada = aplicacion.registrarCuenta(new CuentaPago(nombre, instrucciones.getText()));
                refrescarCatalogos();
                seleccionarCuenta(guardada.getId());
            } catch (RuntimeException error) {
                ControlesUI.error(getScene().getWindow(), "No se pudo guardar la cuenta", error);
            }
        });
    }

    private void editarCuenta() {
        CuentaPago cuenta = cuentas.getValue();
        if (cuenta == null) {
            ControlesUI.informacion(getScene().getWindow(), "Seleccione una cuenta", "Elija una cuenta para editarla.");
            return;
        }
        Dialog<CuentaPago> dialogo = dialogoCuenta(cuenta);
        dialogo.showAndWait().ifPresent(editada -> {
            try {
                CuentaPago actualizada = aplicacion.actualizarCuenta(editada);
                refrescarCatalogos();
                seleccionarCuenta(editada.getId());
                instrucciones.setText(actualizada.getInstrucciones());
            } catch (RuntimeException error) {
                ControlesUI.error(getScene().getWindow(), "No se pudo actualizar la cuenta", error);
            }
        });
    }

    private Dialog<CuentaPago> dialogoCuenta(CuentaPago cuenta) {
        TextField nombre = new TextField(cuenta.getNombre());
        TextArea detalle = new TextArea(cuenta.getInstrucciones());
        detalle.setPrefRowCount(5);
        GridPane contenido = new GridPane();
        contenido.setHgap(10);
        contenido.setVgap(10);
        contenido.setPadding(new Insets(8));
        contenido.addRow(0, new Label("Nombre"), nombre);
        contenido.addRow(1, new Label("Instrucciones"), detalle);
        Dialog<CuentaPago> dialogo = new Dialog<>();
        dialogo.initOwner(getScene().getWindow());
        dialogo.setTitle("Editar cuenta de pago");
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialogo.setResultConverter(boton ->
                boton == ButtonType.OK ? new CuentaPago(cuenta.getId(), nombre.getText(), detalle.getText()) : null);
        dialogo.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, evento -> {
            try {
                new CuentaPago(cuenta.getId(), nombre.getText(), detalle.getText());
            } catch (RuntimeException error) {
                evento.consume();
                ControlesUI.error(dialogo.getDialogPane().getScene().getWindow(), "Revise la cuenta", error);
            }
        });
        return dialogo;
    }

    private void eliminarCuenta() {
        CuentaPago cuenta = cuentas.getValue();
        if (cuenta == null) {
            ControlesUI.informacion(
                    getScene().getWindow(), "Seleccione una cuenta", "Elija una cuenta para eliminarla.");
            return;
        }
        if (!ControlesUI.confirmar(
                getScene().getWindow(),
                "Eliminar cuenta",
                "Se eliminara la cuenta de pago " + cuenta.getNombre() + ".")) {
            return;
        }
        try {
            aplicacion.eliminarCuenta(cuenta.getId());
            refrescarCatalogos();
        } catch (RuntimeException error) {
            ControlesUI.error(getScene().getWindow(), "No se pudo eliminar la cuenta", error);
        }
    }

    private void guardarProforma() {
        if (clienteSeleccionado == null) {
            ControlesUI.informacion(
                    getScene().getWindow(), "Falta el cliente", "Seleccione el cliente de la proforma.");
            return;
        }
        try {
            Proforma proforma = aplicacion.crearProforma(
                    clienteSeleccionado.getIdentificacion().valor(),
                    fecha.getValue(),
                    observaciones.getText(),
                    instrucciones.getText());
            items.forEach(proforma::agregarItem);
            aplicacion.guardarProforma(proforma);
            refrescarGuardadas();
            exportar(proforma);
            limpiarBorrador();
        } catch (RuntimeException error) {
            ControlesUI.error(getScene().getWindow(), "No se pudo guardar la proforma", error);
        }
    }

    private void limpiarBorrador() {
        clienteSeleccionado = null;
        buscarCliente.clear();
        fichaCliente.setText("Ningun cliente seleccionado");
        items.clear();
        fecha.setValue(LocalDate.now());
        observaciones.clear();
        instrucciones.clear();
        cuentas.getSelectionModel().clearSelection();
        actualizarResumen();
    }

    private void refrescarGuardadas() {
        try {
            tablaGuardadas.setItems(FXCollections.observableArrayList(
                    buscarGuardadas.getText().isBlank()
                            ? aplicacion.listarProformas()
                            : aplicacion.buscarProformas(buscarGuardadas.getText())));
        } catch (RuntimeException error) {
            ControlesUI.error(
                    getScene() == null ? null : getScene().getWindow(), "No se cargaron las proformas", error);
        }
    }

    private void eliminarGuardada() {
        Proforma proforma = tablaGuardadas.getSelectionModel().getSelectedItem();
        if (proforma == null) {
            ControlesUI.informacion(getScene().getWindow(), "Seleccione una proforma", "Elija una proforma guardada.");
            return;
        }
        if (!ControlesUI.confirmar(
                getScene().getWindow(),
                "Eliminar proforma",
                "Se eliminara definitivamente " + proforma.getNumero() + ".")) {
            return;
        }
        try {
            aplicacion.eliminarProforma(proforma.getNumero());
            refrescarGuardadas();
        } catch (RuntimeException error) {
            ControlesUI.error(getScene().getWindow(), "No se pudo eliminar la proforma", error);
        }
    }

    private void exportarSeleccionada() {
        Proforma proforma = tablaGuardadas.getSelectionModel().getSelectedItem();
        if (proforma == null) {
            ControlesUI.informacion(getScene().getWindow(), "Seleccione una proforma", "Elija una proforma guardada.");
            return;
        }
        exportar(proforma);
    }

    private void exportar(Proforma proforma) {
        Path ruta = null;
        Throwable error = null;
        try {
            ruta = exportacion.exportar(proforma);
            abrirPdf(ruta);
        } catch (Exception excepcion) {
            error = excepcion;
        }
        mostrarResultadoExportacion(ruta, error);
    }

    private void mostrarResultadoExportacion(Path ruta, Throwable error) {
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.initOwner(getScene().getWindow());
        dialogo.setTitle("Resultado de exportacion");
        dialogo.setHeaderText(
                error == null
                        ? "PDF exportado y abierto correctamente"
                        : ruta == null
                                ? "No se pudo exportar el PDF"
                                : "PDF exportado; no se pudo abrir automaticamente");
        TextField campoRuta = new TextField(
                ruta == null ? "" : ruta.toAbsolutePath().normalize().toString());
        campoRuta.setEditable(false);
        campoRuta.setPromptText("No se genero ningun archivo");
        Label detalle = new Label(error == null ? "Archivo generado:" : mensajeError(error));
        detalle.setWrapText(true);
        dialogo.getDialogPane().setContent(new VBox(10, detalle, campoRuta));
        ButtonType abrir = new ButtonType("Abrir PDF", ButtonBar.ButtonData.LEFT);
        dialogo.getDialogPane().getButtonTypes().addAll(abrir, ButtonType.CLOSE);
        Button botonAbrir = (Button) dialogo.getDialogPane().lookupButton(abrir);
        botonAbrir.setDisable(ruta == null || !escritorioDisponible());
        Path rutaFinal = ruta;
        botonAbrir.addEventFilter(javafx.event.ActionEvent.ACTION, evento -> {
            evento.consume();
            try {
                abrirPdf(rutaFinal);
            } catch (IOException | RuntimeException excepcion) {
                ControlesUI.error(dialogo.getDialogPane().getScene().getWindow(), "No se pudo abrir el PDF", excepcion);
            }
        });
        dialogo.showAndWait();
    }

    private void seleccionarProducto(String codigo) {
        if (codigo == null) {
            productos.getSelectionModel().clearSelection();
            return;
        }
        productos.getItems().stream()
                .filter(producto -> producto.getCodigo().equals(codigo))
                .findFirst()
                .ifPresent(productos::setValue);
    }

    private void seleccionarCuenta(Long id) {
        if (id == null) {
            cuentas.getSelectionModel().clearSelection();
            return;
        }
        cuentas.getItems().stream()
                .filter(cuenta -> id.equals(cuenta.getId()))
                .findFirst()
                .ifPresent(cuentas::setValue);
    }

    private static String mensajeError(Throwable error) {
        Throwable actual = error;
        while ((actual.getMessage() == null || actual.getMessage().isBlank()) && actual.getCause() != null) {
            actual = actual.getCause();
        }
        return actual.getMessage() == null ? "Ocurrio un error inesperado." : actual.getMessage();
    }

    private static boolean escritorioDisponible() {
        try {
            return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN);
        } catch (RuntimeException error) {
            return false;
        }
    }

    private static void abrirPdf(Path ruta) throws IOException {
        if (!escritorioDisponible()) {
            throw new UnsupportedOperationException("El sistema no permite abrir archivos automaticamente");
        }
        Desktop.getDesktop().open(ruta.toFile());
    }

    private static <T> TableColumn<ItemProforma, T> columnaItem(
            String titulo, double ancho, java.util.function.Function<ItemProforma, T> valor) {
        TableColumn<ItemProforma, T> columna = new TableColumn<>(titulo);
        columna.setPrefWidth(ancho);
        columna.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(valor.apply(celda.getValue())));
        return columna;
    }

    private static TableColumn<Proforma, String> columnaProforma(
            String titulo, double ancho, java.util.function.Function<Proforma, String> valor) {
        TableColumn<Proforma, String> columna = new TableColumn<>(titulo);
        columna.setPrefWidth(ancho);
        columna.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(valor.apply(celda.getValue())));
        return columna;
    }
}
