package com.univ2026.proformas.modelo;

import java.util.Locale;

/** Representa un producto generico del catalogo. */
public class Producto {
    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private double impuestoPct;
    private boolean activo;

    /** Crea un producto con valores por defecto. */
    public Producto(String codigo, String nombre) {
        this(codigo, nombre, "", 0.0, 0.0, true);
    }

    /** Crea un producto y aplica las mismas validaciones que los setters. */
    public Producto(
            String codigo, String nombre, String descripcion, double precio, double impuestoPct, boolean activo) {
        setCodigo(codigo);
        setNombre(nombre);
        setDescripcion(descripcion);
        setPrecio(precio);
        setImpuestoPct(impuestoPct);
        setActivo(activo);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = textoObligatorio(codigo, "El codigo no puede estar vacio");
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = textoObligatorio(nombre, "El nombre no puede estar vacio");
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = textoOpcional(descripcion, "La descripcion no puede ser null");
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (!Double.isFinite(precio)) {
            throw new IllegalArgumentException("El precio debe ser un numero finito");
        }
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        this.precio = precio;
    }

    public double getImpuestoPct() {
        return impuestoPct;
    }

    public void setImpuestoPct(double impuestoPct) {
        if (!Double.isFinite(impuestoPct)) {
            throw new IllegalArgumentException("El impuesto debe ser un numero finito");
        }
        if (impuestoPct < 0 || impuestoPct > 100) {
            throw new IllegalArgumentException("El impuesto debe estar entre 0 y 100");
        }
        this.impuestoPct = impuestoPct;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /** Retorna una representacion legible sin imprimirla. */
    public String resumen() {
        String estado = activo ? "activo" : "inactivo";
        return String.format(
                Locale.US, "[%s] %s - $%.2f (impuesto %.1f%%) - %s", codigo, nombre, precio, impuestoPct, estado);
    }

    private static String textoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }

    private static String textoOpcional(String valor, String mensaje) {
        if (valor == null) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }
}
