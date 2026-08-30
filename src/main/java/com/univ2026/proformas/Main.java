import com.univ2026.proformas.modelo.Cliente;
import com.univ2026.proformas.modelo.ItemProforma;
import com.univ2026.proformas.modelo.Producto;
import com.univ2026.proformas.modelo.ProductoDigital;
import com.univ2026.proformas.modelo.ProductoFisico;
import com.univ2026.proformas.modelo.Proforma;
import com.univ2026.proformas.modelo.RegistroClientes;
import com.univ2026.proformas.modelo.RegistroClientesEnMemoria;
import com.univ2026.proformas.modelo.valor.Email;
import com.univ2026.proformas.modelo.valor.Monto;
import com.univ2026.proformas.modelo.valor.RUC;
import java.util.Locale;

/** Punto de entrada del sistema de gestion de proformas. */
public final class Main {
    private Main() {
    }

    /** Inicia la aplicacion de demostracion. */
    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println(" SISTEMA DE GESTION DE PROFORMAS - Demo Fase 3");
        System.out.println("============================================================");
        System.out.println();

        demoCasosValidos();
        demoCasosInvalidos();

        System.out.println("Demo finalizada: los objetos conservaron un estado valido.");
    }

    private static void demoCasosValidos() {
        System.out.println("== Casos validos ==");
        Producto laptop = new ProductoFisico("P-0001", "Laptop Lenovo", "Equipo portatil, 16 GB RAM", 850.0, 12.0, true,
                1.8);
        Producto licencia = new ProductoDigital("P-0002", "Licencia de ofimatica", "Descarga digital", 45.0, 0.0, true,
                750.0);
        Cliente cliente = new Cliente(
                new RUC("1100001234"),
                "Ana Torres",
                "Av. Loja y Sucre",
                "0991234567",
                new Email("ana.torres@correo.com"),
                true);

        System.out.println(laptop.resumen());
        System.out.println(licencia.resumen());
        System.out.println(cliente.resumen());

        Proforma proforma = new Proforma("PRO-0001", cliente);
        proforma.agregarItem(new ItemProforma(laptop, 1, 5.0));
        proforma.agregarItem(new ItemProforma(licencia, 2));
        System.out.println(proforma.resumen());
        System.out.printf(Locale.US, "Subtotal: $%.2f%n", proforma.calcularSubtotal());
        System.out.printf(Locale.US, "Impuesto: $%.2f%n", proforma.calcularImpuesto());
        System.out.printf(Locale.US, "Total: $%.2f%n", proforma.calcularTotal());

        Email mismoEmail = new Email("ana.torres@correo.com");
        Producto mismoCodigo = new Producto("P-0001", "Producto con otro nombre");
        Cliente mismaIdentificacion = new Cliente("1100001234", "Cliente con otro nombre");
        RegistroClientes registro = new RegistroClientesEnMemoria();
        registro.registrar(cliente);

        System.out.println("Email por valor: " + cliente.getEmail().equals(mismoEmail));
        System.out.println("Producto por codigo: " + laptop.equals(mismoCodigo));
        System.out.println("Cliente por identificacion: " + cliente.equals(mismaIdentificacion));
        System.out.println("Representacion del precio: " + laptop.getPrecio());
        System.out.println("Cliente registrado: " + (registro.buscar("1100001234") == cliente));
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
        ejecutarIntento("Identificacion invalida", () -> new RUC("ABC0012345"));
        ejecutarIntento("Monto negativo", () -> new Monto("-1"));
        ejecutarIntento("Cantidad no positiva", () -> {
            Producto producto = new Producto("P-0005", "Mouse");
            new ItemProforma(producto, 0);
        });
        ejecutarIntento("Producto inactivo", () -> {
            Producto producto = new Producto("P-0006", "Monitor", "", 180.0, 12.0, false);
            new ItemProforma(producto, 1);
        });
        ejecutarIntento("Identificacion duplicada", () -> {
            RegistroClientes registro = new RegistroClientesEnMemoria();
            registro.registrar(new Cliente("1100000002", "Luis"));
            registro.registrar(new Cliente("1100000002", "Otra persona"));
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
