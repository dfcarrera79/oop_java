package com.univ2026.proformas.dominio.proforma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import com.univ2026.proformas.dominio.producto.Producto;
import org.junit.jupiter.api.Test;

class ItemProformaTest {
    @Test
    void aplicaDescuentoSegunTipoDeCliente() {
        Producto producto = producto("P-001", 100.0, 0.0);

        assertEquals(85.0, new ItemProforma(producto, 1, TipoCliente.PUBLICO).calcularSubtotal());
        assertEquals(65.0, new ItemProforma(producto, 1, TipoCliente.MAYORISTA).calcularSubtotal());
        assertEquals(60.0, new ItemProforma(producto, 1, TipoCliente.MEDICO).calcularSubtotal());
    }

    @Test
    void sinTipoNoAplicaDescuento() {
        ItemProforma item = new ItemProforma(producto("P-002", 100.0, 0.0), 1);
        assertNull(item.getTipoCliente());
        assertEquals(0.0, item.getDescuentoPct());
        assertEquals(100.0, item.calcularSubtotal());
    }

    @Test
    void calculaDescuentoAntesDelIva() {
        ItemProforma item = new ItemProforma(producto("P-003", 100.0, 15.0), 2, TipoCliente.MEDICO);
        assertEquals(120.0, item.calcularSubtotal(), 0.001);
        assertEquals(18.0, item.calcularImpuesto(), 0.001);
        assertEquals(138.0, item.calcularTotal(), 0.001);
    }

    @Test
    void validaCantidadProductoYEstadoDuranteElCalculo() {
        Producto producto = producto("P-004", 10.0, 15.0);
        assertThrows(IllegalArgumentException.class, () -> new ItemProforma(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new ItemProforma(producto, 0));

        ItemProforma item = new ItemProforma(producto, 1);
        producto.setEstado(Estado.INACTIVO);
        assertThrows(IllegalStateException.class, item::calcularTotal);
    }

    private static Producto producto(String codigo, double precio, double iva) {
        return new Producto(codigo, "Producto", "", precio, iva, Estado.ACTIVO, null);
    }
}
