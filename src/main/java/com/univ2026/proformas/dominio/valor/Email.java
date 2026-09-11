package com.univ2026.proformas.dominio.valor;

/** Correo electronico inmutable y normalizado. */
public record Email(String valor) {
    /** Normaliza y valida el correo; el valor vacio representa correo no informado. */
    public Email {
        if (valor == null) {
            throw new IllegalArgumentException("El email no puede ser null");
        }
        valor = valor.trim();
        int arroba = valor.lastIndexOf('@');
        boolean dominioValido = arroba >= 1 && valor.substring(arroba + 1).contains(".");
        if (!valor.isEmpty() && !dominioValido) {
            throw new IllegalArgumentException("El email no tiene un formato valido");
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}
