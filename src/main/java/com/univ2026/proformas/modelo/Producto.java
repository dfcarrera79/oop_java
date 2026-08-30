package com.univ2026.proformas.modelo;

import com.univ2026.proformas.modelo.valor.Monto;
import java.util.Locale;
import java.util.Objects;

/** Representa un producto generico del catalogo. */
public class Producto {
    private final String codigo;
    private String nombre;
    private String descripcion;
    private Monto precio;
    private double impuestoPct;
    private boolean activo;

    /** Crea un producto con valores por defecto. */
    public Producto(String codigo, String nombre) {
        this(codigo, nombre, "", 0.0, 0.0, true);
    }

    /** Crea un producto y aplica las mismas validaciones que los setters. */
    public Producto(
            String codigo, String nombre, String descripcion, double precio, double impuestoPct, boolean activo) {
        this(codigo, nombre, descripcion, new Monto(precio), impuestoPct, activo);
    }

    /** Crea un producto con un precio representado como modelo de valor. */
    public Producto(
            String codigo, String nombre, String descripcion, Monto precio, double impuestoPct, boolean activo) {
        this.codigo = textoObligatorio(codigo, "El codigo no puede estar vacio");
        setNombre(nombre);
        setDescripcion(descripcion);
        setPrecio(precio);
        setImpuestoPct(impuestoPct);
        setActivo(activo);
    }

    public String getCodigo() {
        return codigo;
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

    public Monto getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        setPrecio(new Monto(precio));
    }

    public void setPrecio(Monto precio) {
        if (precio == null) {
            throw new IllegalArgumentException("El precio no puede ser null");
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
                Locale.US, "[%s] %s - $%s (impuesto %.1f%%) - %s", codigo, nombre, precio, impuestoPct, estado);
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Producto otro)) {
            return false;
        }
        return codigo.equals(otro.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
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
