package com.univ2026.proformas.dominio.producto;

/** Caracteristicas inmutables de un producto tangible. */
public record AtributosFisicos(double pesoKg, Talla talla) implements AtributosProducto {
    public AtributosFisicos {
        if (!Double.isFinite(pesoKg) || pesoKg <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor que cero y finito");
        }
    }

    public AtributosFisicos(double pesoKg) {
        this(pesoKg, null);
    }
}
