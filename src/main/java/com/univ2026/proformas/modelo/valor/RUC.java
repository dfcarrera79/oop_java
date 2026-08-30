package com.univ2026.proformas.modelo.valor;

/** Identificacion tributaria inmutable de 10 o 13 digitos. */
public record RUC(String valor) {
    /** Normaliza y valida una cedula o un RUC. */
    public RUC {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("La identificacion no puede estar vacia");
        }
        valor = valor.trim();
        if (!valor.matches("\\d{10}|\\d{13}")) {
            throw new IllegalArgumentException("La identificacion debe contener 10 o 13 digitos");
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}
