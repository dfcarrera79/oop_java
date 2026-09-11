package com.univ2026.proformas.dominio.cliente;

/** Categoria comercial del cliente y descuento que le corresponde. */
public enum TipoCliente {
    PUBLICO("publico", 15.0),
    MAYORISTA("mayorista", 35.0),
    MEDICO("medico", 40.0);

    private final String etiqueta;
    private final double descuentoPct;

    TipoCliente(String etiqueta, double descuentoPct) {
        this.etiqueta = etiqueta;
        this.descuentoPct = descuentoPct;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public double getDescuentoPct() {
        return descuentoPct;
    }
}
