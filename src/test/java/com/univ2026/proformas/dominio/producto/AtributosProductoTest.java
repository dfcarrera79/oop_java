package com.univ2026.proformas.dominio.producto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.univ2026.proformas.dominio.Estado;
import org.junit.jupiter.api.Test;

class AtributosProductoTest {
    @Test
    void especializaProductosPorComposicion() {
        Producto fisico =
                new Producto("F-001", "Faja", "", 50.0, 15.0, Estado.ACTIVO, new AtributosFisicos(0.3, Talla.M));
        Producto digital = new Producto("D-001", "Curso", "", 20.0, 0.0, Estado.ACTIVO, new AtributosDigitales(850.0));

        assertEquals(
                Talla.M,
                assertInstanceOf(AtributosFisicos.class, fisico.getExtras()).talla());
        assertEquals(
                850.0,
                assertInstanceOf(AtributosDigitales.class, digital.getExtras()).tamanioMb());
        assertNull(new Producto("G-001", "Generico").getExtras());
    }

    @Test
    void rechazaMedidasNoPositivasONoFinitas() {
        assertThrows(IllegalArgumentException.class, () -> new AtributosFisicos(0.0));
        assertThrows(IllegalArgumentException.class, () -> new AtributosFisicos(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new AtributosDigitales(-1.0));
        assertThrows(IllegalArgumentException.class, () -> new AtributosDigitales(Double.POSITIVE_INFINITY));
    }
}
