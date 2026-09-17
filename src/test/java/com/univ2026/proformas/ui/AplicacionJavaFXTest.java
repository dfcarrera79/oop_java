package com.univ2026.proformas.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AplicacionJavaFXTest {
    @TempDir
    Path temporal;

    @Test
    void respetaRutaInyectadaYComponePersistenciaSinLanzarVentanas() {
        String anterior = System.getProperty(AplicacionJavaFX.PROPIEDAD_BASE_DATOS);
        Path base = temporal.resolve("inyectada.db");
        try {
            System.setProperty(AplicacionJavaFX.PROPIEDAD_BASE_DATOS, base.toString());

            assertEquals(base, AplicacionJavaFX.resolverRutaDatos());
            assertTrue(AplicacionJavaFX.crearAplicacion(base).listarProductos().isEmpty());
            assertTrue(base.toFile().isFile());
        } finally {
            if (anterior == null) {
                System.clearProperty(AplicacionJavaFX.PROPIEDAD_BASE_DATOS);
            } else {
                System.setProperty(AplicacionJavaFX.PROPIEDAD_BASE_DATOS, anterior);
            }
        }
    }
}
