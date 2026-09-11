package com.univ2026.proformas.modelo;

/** Representa un producto tangible cuyo peso se expresa en kilogramos. */
public class ProductoFisico extends Producto {
    private double pesoKg;

    /** Crea un producto fisico con los valores heredados por defecto. */
    public ProductoFisico(String codigo, String nombre, double pesoKg) {
        super(codigo, nombre);
        setPesoKg(pesoKg);
    }

    /** Crea un producto fisico con todos sus datos. */
    public ProductoFisico(
            String codigo,
            String nombre,
            String descripcion,
            double precio,
            double impuestoPct,
            boolean activo,
            double pesoKg) {
        super(codigo, nombre, descripcion, precio, impuestoPct, activo);
        setPesoKg(pesoKg);
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(double pesoKg) {
        if (!Double.isFinite(pesoKg) || pesoKg <= 0) {
            throw new IllegalArgumentException("El peso debe ser un numero positivo y finito");
        }
        this.pesoKg = pesoKg;
    }
}
