package com.univ2026.proformas.dominio.producto;

/** Tallas disponibles para productos fisicos. */
public enum Talla {
    XS("XS"),
    S("S"),
    M("M"),
    L("L"),
    XL("XL"),
    UNICA("unica");

    private final String etiqueta;

    Talla(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Busca una talla por su etiqueta; una cadena vacia representa talla omitida. */
    public static Talla desdeEtiqueta(String etiqueta) {
        if (etiqueta == null || etiqueta.isBlank()) {
            return null;
        }
        for (Talla talla : values()) {
            if (talla.etiqueta.equalsIgnoreCase(etiqueta.trim())) {
                return talla;
            }
        }
        throw new IllegalArgumentException("La talla no es valida");
    }
}
