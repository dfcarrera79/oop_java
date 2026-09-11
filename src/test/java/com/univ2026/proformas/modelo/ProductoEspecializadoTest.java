package com.univ2026.proformas.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.modelo.valor.Monto;
import org.junit.jupiter.api.Test;

class ProductoEspecializadoTest {
    @Test
    void creaProductoFisicoYUsaPolimorfismo() {
        Producto producto = new ProductoFisico("F-001", "Laptop", "Portatil", 800.0, 12.0, true, 1.75);

        assertEquals(1.75, ((ProductoFisico) producto).getPesoKg());
    }

    @Test
    void creaProductoDigitalYUsaPolimorfismo() {
        Producto producto = new ProductoDigital("D-001", "Curso", "Videos", 50.0, 0.0, true, 1024.0);

        assertEquals(1024.0, ((ProductoDigital) producto).getTamanioMb());
    }

    @Test
    void constructoresCortosConservanValoresHeredadosPorDefecto() {
        ProductoFisico productoFisico = new ProductoFisico("F-002", "Mouse", 0.2);
        ProductoDigital productoDigital = new ProductoDigital("D-002", "Manual", 5.0);

        assertEquals(new Monto("0.00"), productoFisico.getPrecio());
        assertEquals(0.0, productoDigital.getImpuestoPct());
        assertTrue(productoFisico.isActivo());
        assertTrue(productoDigital.isActivo());
    }

    @Test
    void rechazaPesoYTamanioNoPositivosONoFinitos() {
        assertThrows(IllegalArgumentException.class, () -> new ProductoFisico("F-003", "Mesa", 0.0));
        assertThrows(IllegalArgumentException.class, () -> new ProductoFisico("F-004", "Mesa", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new ProductoDigital("D-003", "Libro", -1.0));
        assertThrows(
                IllegalArgumentException.class, () -> new ProductoDigital("D-004", "Libro", Double.POSITIVE_INFINITY));
    }

    @Test
    void settersPermitenCambiosValidosYConservanValorAnteErrores() {
        ProductoFisico productoFisico = new ProductoFisico("F-005", "Mesa", 10.0);
        ProductoDigital productoDigital = new ProductoDigital("D-005", "Libro", 5.0);

        productoFisico.setPesoKg(12.5);
        productoDigital.setTamanioMb(8.0);

        assertThrows(IllegalArgumentException.class, () -> productoFisico.setPesoKg(-1.0));
        assertThrows(IllegalArgumentException.class, () -> productoDigital.setTamanioMb(Double.NaN));
        assertEquals(12.5, productoFisico.getPesoKg());
        assertEquals(8.0, productoDigital.getTamanioMb());
    }
}
