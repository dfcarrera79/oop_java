package com.univ2026.proformas.exportacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import com.univ2026.proformas.dominio.producto.AtributosFisicos;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.producto.Talla;
import com.univ2026.proformas.dominio.proforma.ItemProforma;
import com.univ2026.proformas.dominio.proforma.Proforma;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExportadorProformaPDFTest {
    @TempDir
    Path temporal;

    @Test
    void generaPdfLegibleConTodaLaInformacionEsencial() throws Exception {
        Proforma proforma = proformaGuardada();
        Path destino = temporal.resolve("salida").resolve("cotizacion.pdf");

        Path generado = new ExportadorProformaPDF(temporal).exportar(proforma, destino);

        assertEquals(destino.toAbsolutePath(), generado);
        assertTrue(Files.exists(generado));
        assertTrue(Files.size(generado) > 0);
        try (PDDocument documento = Loader.loadPDF(generado.toFile())) {
            assertTrue(documento.getNumberOfPages() >= 1);
            String texto = new PDFTextStripper().getText(documento);
            assertTrue(texto.contains("CM INSUMOS MEDICOS"));
            assertTrue(texto.contains("PRO-000123"));
            assertTrue(texto.contains("Clinica San Jose"));
            assertTrue(texto.contains("IDENTIFICACION") || texto.contains("Identificacion"));
            assertTrue(texto.contains("DESCRIPCION"));
            assertTrue(texto.contains("SUBTOTAL"));
            assertTrue(texto.contains("IVA"));
            assertTrue(texto.contains("TOTAL"));
            assertTrue(texto.contains("$38.12"));
            assertTrue(texto.contains("Entregar en recepcion"));
            assertTrue(texto.contains("Transferencia Banco Loja"));
        }
    }

    @Test
    void creaDirectorioPredeterminadoYNombreDerivado() throws Exception {
        Path directorio = temporal.resolve("nuevo").resolve("pdf");

        Path generado = new ExportadorProformaPDF(directorio).exportar(proformaGuardada());

        assertTrue(Files.isDirectory(directorio));
        assertEquals(directorio.resolve("proforma-PRO-000123.pdf").toAbsolutePath(), generado);
    }

    @Test
    void paginaUnaProformaExtensaSinPerderElResumen() throws Exception {
        Proforma proforma = proformaGuardada();
        for (int indice = 0; indice < 45; indice++) {
            proforma.agregarItem(item());
        }

        Path generado = new ExportadorProformaPDF(temporal).exportar(proforma);

        try (PDDocument documento = Loader.loadPDF(generado.toFile())) {
            assertTrue(documento.getNumberOfPages() > 1);
            String texto = new PDFTextStripper().getText(documento);
            assertTrue(texto.contains("PROFORMA (continuacion)"));
            assertTrue(texto.contains("SUBTOTAL"));
            assertTrue(texto.contains("INSTRUCCIONES DE PAGO"));
        }
    }

    @Test
    void rechazaProformaNoGuardadaYProformaSinItems() {
        Cliente cliente = cliente();
        Proforma noGuardada = new Proforma(cliente, LocalDate.now(), "", "");
        noGuardada.agregarItem(item());
        Proforma sinItems = new Proforma("PRO-9", cliente);
        ExportadorProformaPDF exportador = new ExportadorProformaPDF(temporal);

        assertThrows(IllegalArgumentException.class, () -> exportador.exportar(noGuardada));
        assertThrows(IllegalArgumentException.class, () -> exportador.exportar(sinItems));
        assertFalse(Files.exists(temporal.resolve("proforma-PRO-9.pdf")));
    }

    private static Proforma proformaGuardada() {
        Proforma proforma =
                new Proforma(cliente(), LocalDate.of(2026, 9, 13), "Entregar en recepcion", "Transferencia Banco Loja");
        proforma.asignarNumero("PRO-000123");
        proforma.agregarItem(item());
        return proforma;
    }

    private static Cliente cliente() {
        return new Cliente(
                "1100000001",
                "Clinica San Jose",
                "Av. Universitaria 123",
                "072570000",
                "compras@clinica.test",
                TipoCliente.MAYORISTA,
                Estado.ACTIVO);
    }

    private static ItemProforma item() {
        Producto producto = new Producto(
                "INS-001",
                "Faja medica",
                "Soporte postoperatorio de compresion graduada",
                25.50,
                15,
                Estado.ACTIVO,
                new AtributosFisicos(0.4, Talla.M));
        return new ItemProforma(producto, 2, TipoCliente.MAYORISTA, Talla.L);
    }
}
