package com.univ2026.proformas.presentacion;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.AplicacionProformas;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

class MenuConsolaTest {
    @Test
    void ejecutaAltasListadosBajasYDemo() {
        String entradas = String.join(
                System.lineSeparator(),
                "1",
                "P-002",
                "Mouse",
                "10",
                "0",
                "4",
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
                "0");

        String texto = ejecutarMenu(entradas);

        assertTrue(texto.contains("Producto registrado."));
        assertTrue(texto.contains("Cliente registrado."));
        assertTrue(texto.contains("[P-002] Mouse"));
        assertTrue(texto.contains("[1100005678] Luis"));
        assertTrue(texto.contains("Proforma DEMO-001 - Luis - 1 item - $10.00"));
        assertTrue(texto.contains("Producto dado de baja: Mouse."));
        assertTrue(texto.contains("Cliente dado de baja: Luis."));
        assertTrue(texto.trim().endsWith("Hasta pronto."));
    }

    @Test
    void informaOpcionesYDatosInvalidos() {
        String entradas = String.join(System.lineSeparator(), "99", "1", "", "Mouse", "10", "0", "0");

        String texto = ejecutarMenu(entradas);

        assertTrue(texto.contains("Opcion no valida."));
        assertTrue(texto.contains("Error: El codigo no puede estar vacio"));
    }

    @Test
    void finalizaCuandoSeAgotaLaEntrada() {
        assertTrue(ejecutarMenu("").trim().endsWith("Sesion finalizada."));
    }

    private static String ejecutarMenu(String entradas) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Scanner scanner = new Scanner(new ByteArrayInputStream(entradas.getBytes(StandardCharsets.UTF_8)));
        PrintStream salida = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        new MenuConsola(new AplicacionProformas(), scanner, salida).ejecutar();
        return bytes.toString(StandardCharsets.UTF_8);
    }
}
