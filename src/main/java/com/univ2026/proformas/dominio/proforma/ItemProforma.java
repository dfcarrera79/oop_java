package com.univ2026.proformas.dominio.proforma;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import com.univ2026.proformas.dominio.producto.Producto;

/** Representa una linea calculable dentro de una proforma. */
public class ItemProforma {
    private Producto producto;
    private int cantidad;
    private TipoCliente tipoCliente;

    /** Crea un item sin descuento. */
    public ItemProforma(Producto producto, int cantidad) {
        this(producto, cantidad, null);
    }

    /** Crea un item y aplica las mismas validaciones que los setters. */
    public ItemProforma(Producto producto, int cantidad, TipoCliente tipoCliente) {
        setProducto(producto);
        setCantidad(cantidad);
        setTipoCliente(tipoCliente);
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser null");
        }
        if (producto.getEstado() != Estado.ACTIVO) {
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
        return tipoCliente == null ? 0.0 : tipoCliente.getDescuentoPct();
    }

    public TipoCliente getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(TipoCliente tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    /** Calcula precio por cantidad menos el descuento. */
    public double calcularSubtotal() {
        if (producto.getEstado() != Estado.ACTIVO) {
            throw new IllegalStateException("No se puede calcular un producto inactivo");
        }
        double precioConDescuento = producto.getPrecio().doubleValue() * (1 - getDescuentoPct() / 100);
        double subtotal = precioConDescuento * cantidad;
        if (!Double.isFinite(subtotal)) {
            throw new IllegalStateException("El subtotal excede el rango permitido");
        }
        return subtotal;
    }

    /** Calcula el impuesto sobre el subtotal descontado. */
    public double calcularImpuesto() {
        return calcularSubtotal() * producto.getIvaPct() / 100;
    }

    /** Calcula el valor final de la linea. */
    public double calcularTotal() {
        return calcularSubtotal() + calcularImpuesto();
    }
}
