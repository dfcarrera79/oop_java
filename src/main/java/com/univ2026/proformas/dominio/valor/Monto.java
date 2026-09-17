package com.univ2026.proformas.dominio.valor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Importe monetario inmutable, no negativo y normalizado a dos decimales. */
public record Monto(BigDecimal valor) {
    public static final int ESCALA = 2;
    public static final RoundingMode REDONDEO = RoundingMode.HALF_UP;
    /** Valida y normaliza un importe decimal. */
    public Monto {
        if (valor == null) {
            throw new IllegalArgumentException("El monto debe ser un numero valido");
        }
        if (valor.signum() < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        valor = normalizar(valor);
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

    /** Normaliza cualquier resultado monetario con la politica unica del sistema. */
    public static BigDecimal normalizar(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El monto debe ser un numero valido");
        }
        return valor.setScale(ESCALA, REDONDEO);
    }

    /** Convierte el precio capturado a precio base, independientemente de si incluia IVA. */
    public static BigDecimal convertirAPrecioBase(BigDecimal precio, int ivaPct, boolean incluyeIva) {
        BigDecimal normalizado = new Monto(precio).valor();
        if (!incluyeIva || ivaPct == 0) {
            return normalizado;
        }
        BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(ivaPct).movePointLeft(2));
        return normalizar(normalizado.divide(factor, 8, REDONDEO));
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
