package com.univ2026.proformas.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.persistencia.sqlite.CatalogoProductosSQLite;
import com.univ2026.proformas.persistencia.sqlite.RegistroClientesSQLite;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AplicacionProformasTest {
    @TempDir
    Path temporal;

    private AplicacionProformas aplicacion;

    @BeforeEach
    void preparar() {
        Path base = temporal.resolve("aplicacion.db");
        aplicacion = new AplicacionProformas(new CatalogoProductosSQLite(base), new RegistroClientesSQLite(base));
        aplicacion.registrarProducto(new Producto("P-001", "Teclado", "", 25.0, 15.0, Estado.ACTIVO, null));
        aplicacion.registrarCliente(
                new Cliente("1100001234", "Ana", "", "", "ana@correo.com", TipoCliente.PUBLICO, Estado.ACTIVO));
    }

    @Test
    void noExponeLasColeccionesDeLaSesion() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> aplicacion.listarProductos().clear());
        assertThrows(
                UnsupportedOperationException.class,
                () -> aplicacion.listarClientes().clear());
        assertEquals(1, aplicacion.listarProductos().size());
        assertEquals(1, aplicacion.listarClientes().size());
    }

    @Test
    void bajaLogicaConservaEInactivaLosObjetos() {
        Producto producto = aplicacion.darBajaProducto(" P-001 ");
        Cliente cliente = aplicacion.darBajaCliente("1100001234");

        assertEquals(producto, aplicacion.listarProductos().get(0));
        assertEquals(cliente, aplicacion.listarClientes().get(0));
        assertEquals(Estado.INACTIVO, producto.getEstado());
        assertEquals(Estado.INACTIVO, cliente.getEstado());
    }

    @Test
    void bajaLogicaRechazaElementosInexistentes() {
        assertThrows(IllegalArgumentException.class, () -> aplicacion.darBajaProducto("NO-EXISTE"));
        assertThrows(IllegalArgumentException.class, () -> aplicacion.darBajaCliente("1100009999"));
    }

    @Test
    void creaProformaDemoConDatosActivos() {
        assertEquals("DEMO-001", aplicacion.crearProformaDemo().getNumero());
        assertEquals(24.4375, aplicacion.crearProformaDemo().calcularTotal(), 0.001);
    }

    @Test
    void proformaDemoRequiereDatosActivos() {
        aplicacion.darBajaProducto("P-001");

        IllegalStateException error = assertThrows(IllegalStateException.class, aplicacion::crearProformaDemo);
        assertTrue(error.getMessage().contains("cliente y un producto activos"));
    }
}
