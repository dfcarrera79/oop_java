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
}
