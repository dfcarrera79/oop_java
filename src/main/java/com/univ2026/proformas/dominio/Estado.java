package com.univ2026.proformas.dominio;

/** Estado de vida de un producto o cliente. */
public enum Estado {
    ACTIVO("activo"),
    INACTIVO("inactivo");

    private final String etiqueta;

    Estado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
