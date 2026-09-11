package com.univ2026.proformas.dominio.producto;

/** Caracteristicas inmutables de un producto descargable. */
public record AtributosDigitales(double tamanioMb) implements AtributosProducto {
    public AtributosDigitales {
        if (!Double.isFinite(tamanioMb) || tamanioMb <= 0) {
            throw new IllegalArgumentException("El tamanio debe ser mayor que cero y finito");
        }
    }
}
