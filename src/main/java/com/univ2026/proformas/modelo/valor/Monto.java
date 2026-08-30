package com.univ2026.proformas.modelo.valor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Importe monetario inmutable, no negativo y normalizado a dos decimales. */
public record Monto(BigDecimal valor) {
    /** Valida y normaliza un importe decimal. */
    public Monto {
        if (valor == null) {
            throw new IllegalArgumentException("El monto debe ser un numero valido");
        }
        if (valor.signum() < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    /** Convierte una representacion textual, igual que el modelo Python. */
    public Monto(String valor) {
        this(convertir(valor));
    }

    /** Convierte un double finito. */
    public Monto(double valor) {
        this(convertir(valor));
    }

    public double doubleValue() {
        return valor.doubleValue();
    }

    @Override
    public String toString() {
        return valor.toPlainString();
    }

    private static BigDecimal convertir(String valor) {
        try {
            return new BigDecimal(valor == null ? "" : valor.trim());
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("El monto debe ser un numero valido", error);
        }
    }

    private static BigDecimal convertir(double valor) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException("El monto debe ser finito");
        }
        return BigDecimal.valueOf(valor);
    }
}
