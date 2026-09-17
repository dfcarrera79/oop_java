package com.univ2026.proformas;

import com.univ2026.proformas.ui.AplicacionJavaFX;

/** Punto de entrada del sistema de gestion de proformas. */
public final class Main {
    private Main() {}

    /** Mantiene un lanzador separado de Application para ejecucion portable desde Maven. */
    public static void main(String[] args) {
        AplicacionJavaFX.lanzar(args);
    }
}
