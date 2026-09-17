package com.univ2026.proformas.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.pago.CuentaPago;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.producto.Talla;
import com.univ2026.proformas.dominio.proforma.Proforma;
import com.univ2026.proformas.persistencia.sqlite.BaseDatosSQLite;
import com.univ2026.proformas.persistencia.sqlite.CatalogoProductosSQLite;
import com.univ2026.proformas.persistencia.sqlite.RegistroClientesSQLite;
import com.univ2026.proformas.persistencia.sqlite.RepositorioCuentasPagoSQLite;
import com.univ2026.proformas.persistencia.sqlite.RepositorioProformasSQLite;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AplicacionProformasTest {
    @TempDir
    Path temporal;

    private AplicacionProformas aplicacion;

    @BeforeEach
    void preparar() {
        BaseDatosSQLite base = new BaseDatosSQLite(temporal.resolve("aplicacion.db"));
        aplicacion = new AplicacionProformas(
                new CatalogoProductosSQLite(base),
                new RegistroClientesSQLite(base),
                new RepositorioCuentasPagoSQLite(base),
                new RepositorioProformasSQLite(base));
        aplicacion.registrarProducto(new Producto("p-001", "Teclado", "", 25, 15, Estado.ACTIVO, null));
        aplicacion.registrarCliente(new Cliente("1100001234", "Ana"));
    }

    @Test
    void exponeCrudCompletoDeCatalogosYCuentas() {
        Producto producto = aplicacion.buscarProductoPorCodigo(" p-001 ");
        producto.setNombre("Teclado mecanico");
        assertEquals("Teclado mecanico", aplicacion.actualizarProducto(producto).getNombre());
        aplicacion.eliminarProducto("p-001");
        assertNull(aplicacion.buscarProductoPorCodigo("P-001"));

        Cliente cliente = aplicacion.buscarClientePorIdentificacion("1100001234");
        cliente.setNombre("Ana Torres");
        assertEquals("Ana Torres", aplicacion.actualizarCliente(cliente).getNombre());

        CuentaPago cuenta = aplicacion.registrarCuenta(new CuentaPago("Banco", "Cuenta 123"));
        cuenta.setInstrucciones("Cuenta 456");
        assertEquals("Cuenta 456", aplicacion.actualizarCuenta(cuenta).getInstrucciones());
        aplicacion.eliminarCuenta(cuenta.getId());
        assertEquals(0, aplicacion.listarCuentas().size());
    }

    @Test
    void creaAgregaYGuardaProformaComoCasoDeUso() {
        Proforma proforma = aplicacion.crearProforma("1100001234", LocalDate.of(2026, 9, 13), "", "Transferir");
        aplicacion.agregarItem(proforma, "p-001", 1, Talla.XL);

        aplicacion.guardarProforma(proforma);

        assertEquals("PRO-000001", proforma.getNumero());
        assertEquals(new BigDecimal("24.44"), proforma.calcularTotal());
        assertEquals(
                Talla.XL, aplicacion.listarProformas().get(0).getItems().get(0).getTalla());
        assertEquals(1, aplicacion.listarProformas().size());
        assertThrows(IllegalStateException.class, () -> aplicacion.eliminarCliente("1100001234"));
        aplicacion.eliminarProforma(proforma.getNumero());
        aplicacion.eliminarCliente("1100001234");
    }

    @Test
    void requiereClienteExistenteAntesDeConstruirItems() {
        assertThrows(
                IllegalArgumentException.class, () -> aplicacion.crearProforma("1100009999", LocalDate.now(), "", ""));
        assertThrows(IllegalArgumentException.class, () -> aplicacion.agregarItem(null, "P-001", 1));
    }
}
