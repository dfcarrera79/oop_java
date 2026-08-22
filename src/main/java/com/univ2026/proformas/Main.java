import com.univ2026.proformas.modelo.Cliente;
import com.univ2026.proformas.modelo.ItemProforma;
import com.univ2026.proformas.modelo.Producto;
import com.univ2026.proformas.modelo.ProductoDigital;
import com.univ2026.proformas.modelo.ProductoFisico;
import com.univ2026.proformas.modelo.Proforma;


/** Punto de entrada del sistema de gestion de proformas. */
public final class Main {
    private Main() {}

    /** Inicia la aplicacion de demostracion. */
    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println(" SISTEMA DE GESTION DE PROFORMAS - Demo Fase 2");
        System.out.println("============================================================");
        System.out.println();

        demoCasosValidos();
        demoCasosInvalidos();

        System.out.println("Demo finalizada: los objetos conservaron un estado valido.");
    }

    private static void demoCasosValidos() {
        System.out.println("== Casos validos ==");
        Producto laptop =
                new ProductoFisico("P-0001", "Laptop Lenovo", "Equipo portatil, 16 GB RAM", 850.0, 12.0, true, 1.8);
        Producto licencia =
                new ProductoDigital("D-0001", "Licencia de ofimatica", "Descarga digital", 120.0, 12.0, true, 850.0);
        Cliente cliente = new Cliente(
                "1100001234", "Ana Torres", "Av. Loja y Sucre", "0991234567", "ana.torres@correo.com", true);

        System.out.println(laptop.resumen());
        System.out.println(licencia.resumen());
        System.out.println(cliente.resumen());

        Proforma proforma = new Proforma("PRO-0001", cliente);
        proforma.agregarItem(new ItemProforma(laptop, 1, 5.0));
        proforma.agregarItem(new ItemProforma(licencia, 2));
        System.out.println(proforma.resumen());
        proforma.getItems().forEach(item -> System.out.println("  " + item.resumen()));
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
        ejecutarIntento("Cantidad no positiva", () -> {
            Producto producto = new Producto("P-0005", "Mouse");
            new ItemProforma(producto, 0);
        });
        ejecutarIntento("Producto inactivo", () -> {
            Producto producto = new Producto("P-0006", "Monitor", "", 180.0, 12.0, false);
            new ItemProforma(producto, 1);
        });
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