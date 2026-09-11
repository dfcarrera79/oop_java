package com.univ2026.proformas.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.modelo.Cliente;
import com.univ2026.proformas.modelo.ItemProforma;
import com.univ2026.proformas.modelo.Producto;
import com.univ2026.proformas.modelo.Proforma;
import org.junit.jupiter.api.Test;

class FormateadoresTest {
    @Test
    void formateaProductoYCliente() {
        Producto producto = new Producto("P-001", "Teclado", "", 25.0, 12.0, true);
        Cliente cliente = new Cliente("1100001234", "Ana", "Av. Loja", "0991234567", "ana@correo.com", true);

        assertEquals("[P-001] Teclado - $25.00 (impuesto 12.0%) - activo", FormateadorProducto.formatear(producto));
        assertTrue(FormateadorCliente.formatear(cliente).endsWith("ana@correo.com - activo"));
    }

    @Test
    void formateaElDetalleCompletoDeUnaProforma() {
        Cliente cliente = new Cliente("1100001234", "Ana");
        Producto producto = new Producto("P-001", "Teclado", "", 25.0, 12.0, true);
        Proforma proforma = new Proforma("DEMO-001", cliente);
        proforma.agregarItem(new ItemProforma(producto, 1));

        String detalle = FormateadorProforma.formatearDetalle(proforma);

        assertTrue(detalle.contains("Proforma DEMO-001 - Ana - 1 item - $28.00"));
        assertTrue(detalle.contains("Subtotal: $25.00"));
        assertTrue(detalle.contains("Impuesto: $3.00"));
        assertTrue(detalle.endsWith("Total: $28.00"));
    }
}
