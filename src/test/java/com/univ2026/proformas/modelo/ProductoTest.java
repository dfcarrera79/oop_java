package com.univ2026.proformas.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.modelo.valor.Monto;
import org.junit.jupiter.api.Test;

class ProductoTest {
    @Test
    void creaProductoConValoresValidos() {
        Producto producto = new Producto("P-001", "Mouse", "Inalambrico", 15.50, 12.0, true);

        assertEquals("P-001", producto.getCodigo());
        assertEquals("Mouse", producto.getNombre());
        assertEquals("Inalambrico", producto.getDescripcion());
        assertEquals(new Monto("15.50"), producto.getPrecio());
        assertEquals(12.0, producto.getImpuestoPct());
        assertTrue(producto.isActivo());
    }

    @Test
    void usaValoresPorDefectoYNormalizaTextos() {
        Producto producto = new Producto("  P-002  ", "  Teclado  ");

        assertEquals("P-002", producto.getCodigo());
        assertEquals("Teclado", producto.getNombre());
        assertEquals("", producto.getDescripcion());
        assertEquals(new Monto("0.00"), producto.getPrecio());
        assertEquals(0.0, producto.getImpuestoPct());
        assertTrue(producto.isActivo());
    }

    @Test
    void permiteCambiosValidos() {
        Producto producto = new Producto("P-003", "Monitor");

        producto.setPrecio(175.0);
        producto.setImpuestoPct(15.0);
        producto.setActivo(false);

        assertEquals(new Monto("175.00"), producto.getPrecio());
        assertEquals(15.0, producto.getImpuestoPct());
        assertFalse(producto.isActivo());
    }

    @Test
    void rechazaCodigoYNombreVacios() {
        IllegalArgumentException codigoError =
                assertThrows(IllegalArgumentException.class, () -> new Producto(" ", "Mouse"));
        IllegalArgumentException nombreError =
                assertThrows(IllegalArgumentException.class, () -> new Producto("P-004", " "));

        assertEquals("El codigo no puede estar vacio", codigoError.getMessage());
        assertEquals("El nombre no puede estar vacio", nombreError.getMessage());
    }

    @Test
    void rechazaPrecioNegativoEnConstructorYSetter() {
        assertThrows(IllegalArgumentException.class, () -> new Producto("P-005", "Mouse", "", -1.0, 0.0, true));

        Producto producto = new Producto("P-005", "Mouse");
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> producto.setPrecio(-20.0));

        assertEquals("El monto no puede ser negativo", error.getMessage());
        assertEquals(new Monto("0.00"), producto.getPrecio());
    }

    @Test
    void rechazaNumerosNoFinitos() {
        Producto producto = new Producto("P-006", "Mouse");

        assertThrows(IllegalArgumentException.class, () -> producto.setPrecio(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> producto.setImpuestoPct(Double.POSITIVE_INFINITY));
    }

    @Test
    void rechazaImpuestoFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> new Producto("P-007", "Mouse", "", 10.0, 101.0, true));
    }
}
