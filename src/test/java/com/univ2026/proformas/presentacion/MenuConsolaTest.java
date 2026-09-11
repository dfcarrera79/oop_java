package com.univ2026.proformas.presentacion;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.aplicacion.AplicacionProformas;
import com.univ2026.proformas.persistencia.sqlite.CatalogoProductosSQLite;
import com.univ2026.proformas.persistencia.sqlite.RegistroClientesSQLite;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MenuConsolaTest {
    @TempDir
    Path temporal;

    @Test
    void ejecutaAltasListadosBajasYDemo() {
        String entradas = String.join(
                System.lineSeparator(),
                "1",
                "P-002",
                "Mouse",
                "10",
                "15",
                "",
                "4",
                "1",
                "1100005678",
                "Luis",
                "luis@correo.com",
                "2",
                "5",
                "7",
                "3",
                "P-002",
                "6",
                "1100005678",
                "8",
                "Mouse",
                "9",
                "LUIS@CORREO",
                "0");

        String texto = ejecutarMenu(entradas);

        assertTrue(texto.contains("Producto registrado."));
        assertTrue(texto.contains("Cliente registrado."));
        assertTrue(texto.contains("[P-002] Mouse"));
        assertTrue(texto.contains("[1100005678] Luis"));
        assertTrue(texto.contains("Proforma DEMO-001 - Luis - 1 item - $9.78"));
        assertTrue(texto.contains("Producto dado de baja: Mouse."));
        assertTrue(texto.contains("Cliente dado de baja: Luis."));
        assertTrue(texto.trim().endsWith("Hasta pronto."));
    }

    @Test
    void informaOpcionesYDatosInvalidos() {
        String entradas = String.join(System.lineSeparator(), "99", "1", "", "Mouse", "10", "15", "", "0");

        String texto = ejecutarMenu(entradas);

        assertTrue(texto.contains("Opcion no valida."));
        assertTrue(texto.contains("Error: El codigo no puede estar vacio"));
    }

    @Test
    void finalizaCuandoSeAgotaLaEntrada() {
        assertTrue(ejecutarMenu("").trim().endsWith("Sesion finalizada."));
    }

    private String ejecutarMenu(String entradas) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Scanner scanner = new Scanner(new ByteArrayInputStream(entradas.getBytes(StandardCharsets.UTF_8)));
        PrintStream salida = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        Path base = temporal.resolve("menu.db");
        AplicacionProformas aplicacion =
                new AplicacionProformas(new CatalogoProductosSQLite(base), new RegistroClientesSQLite(base));
        new MenuConsola(aplicacion, scanner, salida).ejecutar();
        return bytes.toString(StandardCharsets.UTF_8);
    }
}
