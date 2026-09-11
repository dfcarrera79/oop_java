package com.univ2026.proformas.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ItemProformaTest {
    @Test
    void calculaSubtotalImpuestoYTotalConDescuento() {
        Producto producto = new Producto("P-001", "Teclado", "", 100.0, 12.0, true);
        ItemProforma item = new ItemProforma(producto, 2, 10.0);

        assertEquals(180.0, item.calcularSubtotal(), 0.001);
        assertEquals(21.6, item.calcularImpuesto(), 0.001);
        assertEquals(201.6, item.calcularTotal(), 0.001);
    }

    @Test
    void permiteDescuentoCompleto() {
        Producto producto = new Producto("P-002", "Servicio", "", 50.0, 12.0, true);
        ItemProforma item = new ItemProforma(producto, 1, 100.0);

        assertEquals(0.0, item.calcularSubtotal());
        assertEquals(0.0, item.calcularTotal());
    }

    @Test
    void usaDescuentoCeroPorDefecto() {
        Producto producto = new Producto("P-009", "Servicio", "", 50.0, 0.0, true);
        ItemProforma item = new ItemProforma(producto, 2);

        assertEquals(0.0, item.getDescuentoPct());
        assertEquals(100.0, item.calcularSubtotal());
    }

    @Test
    void rechazaProductoNullOInactivo() {
        Producto inactivo = new Producto("P-003", "Monitor", "", 150.0, 12.0, false);

        IllegalArgumentException nullError =
                assertThrows(IllegalArgumentException.class, () -> new ItemProforma(null, 1, 0.0));
        IllegalArgumentException inactivoError =
                assertThrows(IllegalArgumentException.class, () -> new ItemProforma(inactivo, 1, 0.0));

        assertEquals("El producto no puede ser null", nullError.getMessage());
        assertEquals("No se puede agregar un producto inactivo", inactivoError.getMessage());
    }

    @Test
    void rechazaCantidadNoPositiva() {
        Producto producto = new Producto("P-004", "Mouse");

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> new ItemProforma(producto, 0, 0.0));

        assertEquals("La cantidad debe ser positiva", error.getMessage());
    }

    @Test
    void rechazaDescuentoInvalidoONoFinito() {
        Producto producto = new Producto("P-005", "Mouse");

        assertThrows(IllegalArgumentException.class, () -> new ItemProforma(producto, 1, -1.0));
        assertThrows(IllegalArgumentException.class, () -> new ItemProforma(producto, 1, 101.0));
        assertThrows(IllegalArgumentException.class, () -> new ItemProforma(producto, 1, Double.NaN));
    }

    @Test
    void settersPermitenCambiosValidosYConservanValorAnteErrores() {
        Producto producto = new Producto("P-006", "Mouse");
        ItemProforma item = new ItemProforma(producto, 1, 0.0);

        item.setCantidad(3);
        item.setDescuentoPct(15.0);

        assertThrows(IllegalArgumentException.class, () -> item.setCantidad(0));
        assertThrows(IllegalArgumentException.class, () -> item.setDescuentoPct(101.0));
        assertEquals(3, item.getCantidad());
        assertEquals(15.0, item.getDescuentoPct());
    }

    @Test
    void rechazaCalculoSiElProductoFueDesactivado() {
        Producto producto = new Producto("P-007", "Mouse", "", 10.0, 12.0, true);
        ItemProforma item = new ItemProforma(producto, 1, 0.0);

        producto.setActivo(false);

        IllegalStateException error = assertThrows(IllegalStateException.class, item::calcularTotal);
        assertEquals("No se puede calcular un producto inactivo", error.getMessage());
    }

    @Test
    void descuentoCompletoEvitaDesbordamientoYRechazaOtrosTotalesNoFinitos() {
        Producto productoMaximo = new Producto("P-008", "Servidor", "", Double.MAX_VALUE, 0.0, true);

        assertEquals(0.0, new ItemProforma(productoMaximo, 2, 100.0).calcularTotal());
        assertThrows(IllegalStateException.class, () -> new ItemProforma(productoMaximo, 2, 0.0).calcularTotal());
    }
}
