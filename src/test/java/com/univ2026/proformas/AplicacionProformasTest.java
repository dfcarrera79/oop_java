package com.univ2026.proformas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.modelo.Cliente;
import com.univ2026.proformas.modelo.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AplicacionProformasTest {
    private AplicacionProformas aplicacion;

    @BeforeEach
    void preparar() {
        aplicacion = new AplicacionProformas();
        aplicacion.registrarProducto(new Producto("P-001", "Teclado", "", 25.0, 12.0, true));
        aplicacion.registrarCliente(new Cliente("1100001234", "Ana", "", "", "ana@correo.com", true));
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

        assertSame(producto, aplicacion.listarProductos().get(0));
        assertSame(cliente, aplicacion.listarClientes().get(0));
        assertFalse(producto.isActivo());
        assertFalse(cliente.isActivo());
    }

    @Test
    void bajaLogicaRechazaElementosInexistentes() {
        assertThrows(IllegalArgumentException.class, () -> aplicacion.darBajaProducto("NO-EXISTE"));
        assertThrows(IllegalArgumentException.class, () -> aplicacion.darBajaCliente("1100009999"));
    }

    @Test
    void creaProformaDemoConDatosActivos() {
        assertEquals("DEMO-001", aplicacion.crearProformaDemo().getNumero());
        assertEquals(28.0, aplicacion.crearProformaDemo().calcularTotal(), 0.001);
    }

    @Test
    void proformaDemoRequiereDatosActivos() {
        aplicacion.darBajaProducto("P-001");

        IllegalStateException error = assertThrows(IllegalStateException.class, aplicacion::crearProformaDemo);
        assertTrue(error.getMessage().contains("cliente y un producto activos"));
    }
}
