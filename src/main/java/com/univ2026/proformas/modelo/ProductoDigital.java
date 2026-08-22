package com.univ2026.proformas.modelo;

import java.util.Locale;

/** Representa un producto descargable cuyo tamanio se expresa en megabytes. */
public class ProductoDigital extends Producto {
    private double tamanioMb;

    /** Crea un producto digital con los valores heredados por defecto. */
    public ProductoDigital(String codigo, String nombre, double tamanioMb) {
        super(codigo, nombre);
        setTamanioMb(tamanioMb);
    }

    /** Crea un producto digital con todos sus datos. */
    public ProductoDigital(
            String codigo,
            String nombre,
            String descripcion,
            double precio,
            double impuestoPct,
            boolean activo,
            double tamanioMb) {
        super(codigo, nombre, descripcion, precio, impuestoPct, activo);
        setTamanioMb(tamanioMb);
    }

    public double getTamanioMb() {
        return tamanioMb;
    }

    public void setTamanioMb(double tamanioMb) {
        if (!Double.isFinite(tamanioMb) || tamanioMb <= 0) {
            throw new IllegalArgumentException("El tamanio debe ser un numero positivo y finito");
        }
        this.tamanioMb = tamanioMb;
    }

    /** Agrega el tamanio a la representacion heredada. */
    @Override
    public String resumen() {
        return super.resumen() + String.format(Locale.US, " - digital: %.2f MB", tamanioMb);
    }
}

