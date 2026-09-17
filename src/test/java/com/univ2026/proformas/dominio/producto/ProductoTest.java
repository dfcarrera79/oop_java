package com.univ2026.proformas.dominio.producto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.valor.Monto;
import org.junit.jupiter.api.Test;

class ProductoTest {
    @Test
    void creaProductoConNuevosValoresPorDefecto() {
        Producto producto = new Producto("  P-001  ", "  Faja  ");
        assertEquals("P-001", producto.getCodigo());
        assertEquals("Faja", producto.getNombre());
        assertEquals(new Monto("0.00"), producto.getPrecio());
        assertEquals(15, producto.getIvaPct());
        assertEquals(Estado.ACTIVO, producto.getEstado());
        assertNull(producto.getExtras());
    }

    @Test
    void validaPropiedadesMedianteSetters() {
        Producto producto = new Producto("P-002", "Faja");
        producto.setPrecio(85.0);
        producto.setIvaPct(0.0);
        producto.setEstado(Estado.INACTIVO);
        assertEquals(new Monto("85.00"), producto.getPrecio());
        assertEquals(0, producto.getIvaPct());
        assertEquals(Estado.INACTIVO, producto.getEstado());
        assertThrows(IllegalArgumentException.class, () -> producto.setIvaPct(12.0));
        assertThrows(IllegalArgumentException.class, () -> producto.setEstado(null));
    }

    @Test
    void normalizaCodigoConLocaleRaiz() {
        assertEquals("PRO-I", new Producto("  pro-i  ", "Faja").getCodigo());
    }

    @Test
    void rechazaDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new Producto(" ", "Faja"));
        assertThrows(IllegalArgumentException.class, () -> new Producto("P-003", " "));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Producto("P-004", "Faja", "", -1.0, 15.0, Estado.ACTIVO, null));
    }
}
