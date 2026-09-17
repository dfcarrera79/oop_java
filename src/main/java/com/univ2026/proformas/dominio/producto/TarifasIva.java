package com.univ2026.proformas.dominio.producto;

import java.util.List;

/** Fuente unica de las tasas de IVA que pueden seleccionarse. */
public final class TarifasIva {
    private static final List<Integer> DISPONIBLES = List.of(0, 15);

    private TarifasIva() {}

    public static List<Integer> disponibles() {
        return DISPONIBLES;
    }

    public static int validar(double tasa) {
        if (!Double.isFinite(tasa) || tasa != Math.rint(tasa) || !DISPONIBLES.contains((int) tasa)) {
            throw new IllegalArgumentException("El IVA debe ser una de las tasas disponibles: " + DISPONIBLES);
        }
        return (int) tasa;
    }
}
