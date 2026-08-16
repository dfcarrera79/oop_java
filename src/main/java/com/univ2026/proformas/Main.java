/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

import com.univ2026.proformas.modelo.Cliente;
import com.univ2026.proformas.modelo.Producto;

/** Punto de entrada del sistema de gestion de proformas. */
public final class Main {
    private Main() {
    }

    /** Inicia la aplicacion de demostracion. */
    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println(" SISTEMA DE GESTION DE PROFORMAS - Demo Fase 1");
        System.out.println("============================================================");
        System.out.println();

        demoCasosValidos();
        demoCasosInvalidos();

        System.out.println("Demo finalizada: los objetos conservaron un estado valido.");
    }

    private static void demoCasosValidos() {
        System.out.println("== Casos validos ==");
        Producto laptop = new Producto("P-0001", "Laptop Lenovo", "Equipo portatil, 16 GB RAM", 850.0, 12.0, true);
        Producto monitor = new Producto("P-0002", "Monitor 24 pulgadas", "", 180.0, 0.0, false);
        Cliente cliente = new Cliente(
                "1100001234", "Ana Torres", "Av. Loja y Sucre", "0991234567", "ana.torres@correo.com", true);

        System.out.println(laptop.resumen());
        System.out.println(monitor.resumen());
        System.out.println(cliente.resumen());
        System.out.println();
    }

    private static void demoCasosInvalidos() {
        System.out.println("== Casos invalidos (validacion en accion) ==");
        ejecutarIntento("Precio negativo", () -> new Producto("P-0003", "Teclado", "", -10.0, 0.0, true));
        ejecutarIntento("Impuesto mayor a 100", () -> new Producto("P-0004", "Teclado", "", 10.0, 150.0, true));
        ejecutarIntento("Codigo vacio", () -> new Producto("", "Teclado"));
        ejecutarIntento(
                "Email mal formado", () -> new Cliente("1100000001", "Luis", "", "", "correo-sin-arroba", true));
        ejecutarIntento("Identificacion vacia", () -> new Cliente("", "Luis"));
        System.out.println();
    }

    private static void ejecutarIntento(String nombre, Runnable intento) {
        try {
            intento.run();
            System.out.println("  ERROR: " + nombre + " no fue rechazado");
        } catch (IllegalArgumentException error) {
            System.out.println("  [ok] " + nombre + ": rechazado - " + error.getMessage());
        }
    }
}