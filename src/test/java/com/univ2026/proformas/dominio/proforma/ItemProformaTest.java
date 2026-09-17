package com.univ2026.proformas.dominio.proforma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import com.univ2026.proformas.dominio.producto.AtributosFisicos;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.producto.Talla;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ItemProformaTest {
    @Test
    void calculaConBigDecimalDescuentoAntesDelIva() {
        ItemProforma item = new ItemProforma(producto("P-001", "100.00", 15), 2, TipoCliente.MEDICO);

        assertEquals(new BigDecimal("80.00"), item.calcularDescuento());
        assertEquals(new BigDecimal("120.00"), item.calcularSubtotal());
        assertEquals(new BigDecimal("18.00"), item.calcularImpuesto());
        assertEquals(new BigDecimal("138.00"), item.calcularTotal());
    }

    @Test
    void aplicaLaTasaDeCadaTipoDeCliente() {
        Producto producto = producto("P-000", "100.00", 0);

        assertEquals(new BigDecimal("85.00"), new ItemProforma(producto, 1, TipoCliente.PUBLICO).calcularSubtotal());
        assertEquals(new BigDecimal("65.00"), new ItemProforma(producto, 1, TipoCliente.MAYORISTA).calcularSubtotal());
        assertEquals(new BigDecimal("60.00"), new ItemProforma(producto, 1, TipoCliente.MEDICO).calcularSubtotal());
    }

    @Test
    void conservaSnapshotAunqueCambieElProductoActual() {
        Producto producto = producto("P-002", "10.00", 15);
        ItemProforma item = new ItemProforma(producto, 1);

        producto.setPrecio(99);
        producto.setEstado(Estado.INACTIVO);

        assertEquals(new BigDecimal("11.50"), item.calcularTotal());
        assertEquals("10.00", item.getProducto().getPrecio().toString());
    }

    @Test
    void validaCantidadProductoYDescuento() {
        Producto producto = producto("P-003", "10.00", 0);
        assertThrows(IllegalArgumentException.class, () -> new ItemProforma(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new ItemProforma(producto, 0));
        assertThrows(IllegalArgumentException.class, () -> new ItemProforma(producto, 1, new BigDecimal("101")));
    }

    @Test
    void usaTallaDelProductoComoInicialYPermiteSobrescribirla() {
        Producto producto = new Producto("P-004", "Faja", "", 10, 15, Estado.ACTIVO, new AtributosFisicos(1, Talla.M));

        assertEquals(Talla.M, new ItemProforma(producto, 1).getTalla());
        assertEquals(Talla.XL, new ItemProforma(producto, 1, TipoCliente.PUBLICO, Talla.XL).getTalla());
    }

    private static Producto producto(String codigo, String precio, int iva) {
        return new Producto(codigo, "Producto", "", Double.parseDouble(precio), iva, Estado.ACTIVO, null);
    }
}
