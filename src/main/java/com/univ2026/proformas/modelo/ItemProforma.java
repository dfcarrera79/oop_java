package com.univ2026.proformas.modelo;

import java.util.Locale;

/** Representa una linea calculable dentro de una proforma. */
public class ItemProforma {
    private Producto producto;
    private int cantidad;
    private double descuentoPct;

    /** Crea un item sin descuento. */
    public ItemProforma(Producto producto, int cantidad) {
        this(producto, cantidad, 0.0);
    }

    /** Crea un item y aplica las mismas validaciones que los setters. */
    public ItemProforma(Producto producto, int cantidad, double descuentoPct) {
        setProducto(producto);
        setCantidad(cantidad);
        setDescuentoPct(descuentoPct);
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser null");
        }
        if (!producto.isActivo()) {
            throw new IllegalArgumentException("No se puede agregar un producto inactivo");
        }
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        this.cantidad = cantidad;
    }

    public double getDescuentoPct() {
        return descuentoPct;
    }

    public void setDescuentoPct(double descuentoPct) {
        if (!Double.isFinite(descuentoPct)) {
            throw new IllegalArgumentException("El descuento debe ser un numero finito");
        }
        if (descuentoPct < 0 || descuentoPct > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
        this.descuentoPct = descuentoPct;
    }

    /** Calcula precio por cantidad menos el descuento. */
    public double calcularSubtotal() {
        if (!producto.isActivo()) {
            throw new IllegalStateException("No se puede calcular un producto inactivo");
        }
        double precioConDescuento = producto.getPrecio() * (1 - descuentoPct / 100);
        double subtotal = precioConDescuento * cantidad;
        if (!Double.isFinite(subtotal)) {
            throw new IllegalStateException("El subtotal excede el rango permitido");
        }
        return subtotal;
    }

    /** Calcula el impuesto sobre el subtotal descontado. */
    public double calcularImpuesto() {
        return calcularSubtotal() * producto.getImpuestoPct() / 100;
    }

    /** Calcula el valor final de la linea. */
    public double calcularTotal() {
        return calcularSubtotal() + calcularImpuesto();
    }

    /** Retorna una representacion legible sin imprimirla. */
    public String resumen() {
        return String.format(
                Locale.US,
                "%d x %s - descuento %.1f%% - subtotal $%.2f - impuesto $%.2f - total $%.2f",
                cantidad,
                producto.getNombre(),
                descuentoPct,
                calcularSubtotal(),
                calcularImpuesto(),
                calcularTotal());
    }
}
