package com.univ2026.proformas.presentacion;

import com.univ2026.proformas.AplicacionProformas;
import com.univ2026.proformas.modelo.Cliente;
import com.univ2026.proformas.modelo.Producto;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Menu interactivo con entrada y salida inyectables. */
public class MenuConsola {
    static final String OPCIONES =
            """
            Sistema de Gestion de Proformas - Fase 4
            1. Registrar producto
            2. Listar productos
            3. Dar de baja un producto
            4. Registrar cliente
            5. Listar clientes
            6. Dar de baja un cliente
            7. Mostrar proforma demo
            0. Salir""";

    private final AplicacionProformas aplicacion;
    private final Scanner entrada;
    private final PrintStream salida;

    public MenuConsola(AplicacionProformas aplicacion, Scanner entrada, PrintStream salida) {
        if (aplicacion == null || entrada == null || salida == null) {
            throw new IllegalArgumentException("La aplicacion, entrada y salida son obligatorias");
        }
        this.aplicacion = aplicacion;
        this.entrada = entrada;
        this.salida = salida;
    }

    public void ejecutar() {
        while (true) {
            salida.println(OPCIONES);
            String opcion;
            try {
                opcion = leer("Seleccione una opcion: ");
            } catch (NoSuchElementException error) {
                salida.println("Sesion finalizada.");
                return;
            }
            if ("0".equals(opcion)) {
                salida.println("Hasta pronto.");
                return;
            }
            try {
                ejecutarOpcion(opcion);
            } catch (IllegalArgumentException | IllegalStateException error) {
                salida.println("Error: " + error.getMessage());
            }
        }
    }

    private void ejecutarOpcion(String opcion) {
        switch (opcion) {
            case "1" -> registrarProducto();
            case "2" -> listarProductos();
            case "3" -> darBajaProducto();
            case "4" -> registrarCliente();
            case "5" -> listarClientes();
            case "6" -> darBajaCliente();
            case "7" -> salida.println(FormateadorProforma.formatearDetalle(aplicacion.crearProformaDemo()));
            default -> salida.println("Opcion no valida.");
        }
    }

    private void registrarProducto() {
        Producto producto = new Producto(
                leer("Codigo: "),
                leer("Nombre: "),
                "",
                leerDecimal("Precio: "),
                leerDecimalOpcional("Impuesto (%): "),
                true);
        aplicacion.registrarProducto(producto);
        salida.println("Producto registrado.");
    }

    private void listarProductos() {
        if (aplicacion.listarProductos().isEmpty()) {
            salida.println("No hay productos.");
            return;
        }
        aplicacion.listarProductos().stream()
                .map(FormateadorProducto::formatear)
                .forEach(salida::println);
    }

    private void darBajaProducto() {
        Producto producto = aplicacion.darBajaProducto(leer("Codigo: "));
        salida.println("Producto dado de baja: " + producto.getNombre() + ".");
    }

    private void registrarCliente() {
        Cliente cliente =
                new Cliente(leer("Cedula o RUC: "), leer("Nombre: "), "", "", leer("Email (opcional): "), true);
        aplicacion.registrarCliente(cliente);
        salida.println("Cliente registrado.");
    }

    private void listarClientes() {
        if (aplicacion.listarClientes().isEmpty()) {
            salida.println("No hay clientes.");
            return;
        }
        aplicacion.listarClientes().stream().map(FormateadorCliente::formatear).forEach(salida::println);
    }

    private void darBajaCliente() {
        Cliente cliente = aplicacion.darBajaCliente(leer("Cedula o RUC: "));
        salida.println("Cliente dado de baja: " + cliente.getNombre() + ".");
    }

    private String leer(String mensaje) {
        salida.print(mensaje);
        return entrada.nextLine().trim();
    }

    private double leerDecimal(String mensaje) {
        try {
            return Double.parseDouble(leer(mensaje));
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("El valor debe ser numerico");
        }
    }

    private double leerDecimalOpcional(String mensaje) {
        String valor = leer(mensaje);
        if (valor.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("El valor debe ser numerico");
        }
    }
}
