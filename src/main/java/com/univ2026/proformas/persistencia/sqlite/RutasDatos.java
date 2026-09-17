package com.univ2026.proformas.persistencia.sqlite;

import java.nio.file.Path;
import java.util.Map;

/** Resuelve de forma centralizada las rutas de desarrollo y produccion Linux. */
public final class RutasDatos {
    private RutasDatos() {}

    public static Path desarrollo() {
        return Path.of("data", "proformas.db");
    }

    public static Path produccion() {
        return produccion(System.getenv(), System.getProperty("user.home"));
    }

    public static Path produccion(Map<String, String> entorno, String directorioPersonal) {
        if (entorno == null || directorioPersonal == null || directorioPersonal.isBlank()) {
            throw new IllegalArgumentException("El entorno y el directorio personal son obligatorios");
        }
        String xdg = entorno.get("XDG_DATA_HOME");
        Path raiz = xdg == null || xdg.isBlank() ? Path.of(directorioPersonal, ".local", "share") : Path.of(xdg);
        return raiz.resolve("ec.edu.univ.proformas").resolve("proformas.db");
    }
}
