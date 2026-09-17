package com.univ2026.proformas.dominio.proforma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.producto.Producto;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ProformaTest {
    @Test
    void administraItemsDatosYTotalesDecimales() {
        Cliente cliente = new Cliente("1100000001", "Ana");
        Proforma proforma = new Proforma(cliente, LocalDate.of(2026, 9, 13), " Nota ", " Banco ");
        proforma.agregarItem(new ItemProforma(producto("P-001", 10, 15), 2));
        proforma.agregarItem(new ItemProforma(producto("P-002", 5, 0), 1));

        assertEquals(new BigDecimal("25.00"), proforma.calcularSubtotal());
        assertEquals(new BigDecimal("3.00"), proforma.calcularImpuesto());
        assertEquals(new BigDecimal("28.00"), proforma.calcularTotal());
        assertEquals("Nota", proforma.getObservaciones());
        assertEquals("Banco", proforma.getInstruccionesPago());
        assertEquals("P-001", proforma.quitarItem(0).getProducto().getCodigo());
        assertEquals(1, proforma.getItems().size());
    }

    @Test
    void exigeClienteFechaYProtegeLista() {
        assertThrows(IllegalArgumentException.class, () -> new Proforma(null, LocalDate.now(), "", ""));
        assertThrows(
                IllegalArgumentException.class, () -> new Proforma(new Cliente("1100000002", "Luis"), null, "", ""));
        Proforma proforma = new Proforma(new Cliente("1100000002", "Luis"), LocalDate.now(), "", "");
        assertTrue(proforma.getItems().isEmpty());
        assertThrows(
                UnsupportedOperationException.class, () -> proforma.getItems().add(null));
        assertThrows(IllegalArgumentException.class, () -> proforma.agregarItem(null));
    }

    @Test
    void normalizaNumeroYNoPermiteCambiarlo() {
        Proforma proforma = new Proforma("  PRO-000001  ", new Cliente("1100000003", "Maria"));

        assertEquals("PRO-000001", proforma.getNumero());
        assertThrows(IllegalStateException.class, () -> proforma.asignarNumero("PRO-000002"));
        assertThrows(IllegalArgumentException.class, () -> proforma.asignarNumero(" "));
    }

    @Test
    void cadaDocumentoTieneSuListaYSnapshotDeCliente() {
        Cliente cliente = new Cliente("1100000004", "Original");
        Proforma primera = new Proforma(cliente, LocalDate.now(), "", "");
        Proforma segunda = new Proforma(cliente, LocalDate.now(), "", "");
        primera.agregarItem(new ItemProforma(producto("P-003", 10, 0), 1));
        cliente.setNombre("Modificado");

        assertEquals(1, primera.getItems().size());
        assertTrue(segunda.getItems().isEmpty());
        assertEquals("Original", primera.getCliente().getNombre());
    }

    private static Producto producto(String codigo, double precio, int iva) {
        return new Producto(codigo, "Producto", "", precio, iva, Estado.ACTIVO, null);
    }
}
