package com.univ2026.proformas.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ProformaTest {
    @Test
    void agregaItemsYAcumulaTotales() {
        Cliente cliente = new Cliente("1100000001", "Ana");
        Producto productoUno = new Producto("P-001", "Teclado", "", 100.0, 12.0, true);
        Producto productoDos = new Producto("P-002", "Mouse", "", 25.0, 0.0, true);
        Proforma proforma = new Proforma("PRO-001", cliente);
        ItemProforma item = new ItemProforma(productoUno, 2, 10.0);

        proforma.agregarItem(item);
        proforma.agregarItem(new ItemProforma(productoDos, 1));

        assertEquals(item, proforma.getItems().get(0));
        assertEquals(205.0, proforma.calcularSubtotal(), 0.001);
        assertEquals(21.6, proforma.calcularImpuesto(), 0.001);
        assertEquals(226.6, proforma.calcularTotal(), 0.001);
        assertEquals(2, proforma.getItems().size());
    }

    @Test
    void iniciaSinItemsYConTotalesEnCero() {
        Proforma proforma = new Proforma("PRO-002", new Cliente("1100000002", "Luis"));

        assertTrue(proforma.getItems().isEmpty());
        assertEquals(0.0, proforma.calcularTotal());
    }

    @Test
    void noPermiteModificarLaListaDesdeElExterior() {
        Proforma proforma = new Proforma("PRO-003", new Cliente("1100000003", "Maria"));
        List<ItemProforma> items = proforma.getItems();

        assertThrows(UnsupportedOperationException.class, () -> items.add(null));
    }

    @Test
    void rechazaClienteNullYConservaElClienteActual() {
        Cliente cliente = new Cliente("1100000004", "Carlos");
        Proforma proforma = new Proforma("PRO-004", cliente);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> proforma.setCliente(null));

        assertEquals("El cliente no puede ser null", error.getMessage());
        assertEquals(cliente, proforma.getCliente());
    }

    @Test
    void validaYNormalizaNumeroAlCrearYAsignar() {
        Proforma proforma = new Proforma("  PRO-005  ", new Cliente("1100000005", "Sofia"));

        assertEquals("PRO-005", proforma.getNumero());
        assertThrows(IllegalArgumentException.class, () -> proforma.setNumero(" "));
        assertEquals("PRO-005", proforma.getNumero());
        assertThrows(IllegalArgumentException.class, () -> new Proforma(null, new Cliente("1100000006", "Elena")));
    }

    @Test
    void agregarItemRechazaNullYProductoDesactivado() {
        Proforma proforma = new Proforma("PRO-006", new Cliente("1100000007", "Sofia"));
        Producto activo = new Producto("P-003", "Mouse");
        ItemProforma item = new ItemProforma(activo, 1);
        activo.setActivo(false);

        assertThrows(IllegalArgumentException.class, () -> proforma.agregarItem(null));
        assertThrows(IllegalArgumentException.class, () -> proforma.agregarItem(item));
        assertTrue(proforma.getItems().isEmpty());
    }

    @Test
    void cadaProformaPoseeSuPropiaListaDeItems() {
        Cliente cliente = new Cliente("1100000008", "Eva");
        Proforma primera = new Proforma("PRO-007", cliente);
        Proforma segunda = new Proforma("PRO-008", cliente);

        primera.agregarItem(new ItemProforma(new Producto("P-004", "Manual"), 1));

        assertEquals(1, primera.getItems().size());
        assertTrue(segunda.getItems().isEmpty());
    }
}
