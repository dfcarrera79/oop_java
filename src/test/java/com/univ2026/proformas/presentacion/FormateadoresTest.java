package com.univ2026.proformas.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.proforma.ItemProforma;
import com.univ2026.proformas.dominio.proforma.Proforma;
import org.junit.jupiter.api.Test;

class FormateadoresTest {
    @Test
    void formateaProductoYCliente() {
        Producto producto = new Producto("P-001", "Teclado", "", 25.0, 15.0, Estado.ACTIVO, null);
        Cliente cliente = new Cliente(
                "1100001234", "Ana", "Av. Loja", "0991234567", "ana@correo.com", TipoCliente.PUBLICO, Estado.ACTIVO);

        assertEquals("[P-001] Teclado - $25.00 - (IVA 15.0%) - activo", FormateadorProducto.formatear(producto));
        assertTrue(FormateadorCliente.formatear(cliente).endsWith("ana@correo.com - publico - activo"));
    }

    @Test
    void formateaElDetalleCompletoDeUnaProforma() {
        Cliente cliente = new Cliente("1100001234", "Ana");
        Producto producto = new Producto("P-001", "Teclado", "", 25.0, 15.0, Estado.ACTIVO, null);
        Proforma proforma = new Proforma("DEMO-001", cliente);
        proforma.agregarItem(new ItemProforma(producto, 1));

        String detalle = FormateadorProforma.formatearDetalle(proforma);

        assertTrue(detalle.contains("Proforma DEMO-001 - Ana - 1 item - $28.75"));
        assertTrue(detalle.contains("Subtotal: $25.00"));
        assertTrue(detalle.contains("IVA: $3.75"));
        assertTrue(detalle.endsWith("Total: $28.75"));
    }
}
