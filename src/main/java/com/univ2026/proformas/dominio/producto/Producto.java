package com.univ2026.proformas.dominio.producto;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.valor.Monto;
import java.util.Objects;

/** Representa un producto generico del catalogo. */
public class Producto {
    private final String codigo;
    private String nombre;
    private String descripcion;
    private Monto precio;
    private double ivaPct;
    private Estado estado;
    private AtributosProducto extras;

    /** Crea un producto con valores por defecto. */
    public Producto(String codigo, String nombre) {
        this(codigo, nombre, "", 0.0, 15.0, Estado.ACTIVO, null);
    }

    /** Crea un producto y aplica las mismas validaciones que los setters. */
    public Producto(
            String codigo,
            String nombre,
            String descripcion,
            double precio,
            double ivaPct,
            Estado estado,
            AtributosProducto extras) {
        this(codigo, nombre, descripcion, new Monto(precio), ivaPct, estado, extras);
    }

    /** Crea un producto con un precio representado como modelo de valor. */
    public Producto(
            String codigo,
            String nombre,
            String descripcion,
            Monto precio,
            double ivaPct,
            Estado estado,
            AtributosProducto extras) {
        this.codigo = textoObligatorio(codigo, "El codigo no puede estar vacio");
        setNombre(nombre);
        setDescripcion(descripcion);
        setPrecio(precio);
        setIvaPct(ivaPct);
        setEstado(estado);
        setExtras(extras);
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

    public double getIvaPct() {
        return ivaPct;
    }

    public void setIvaPct(double ivaPct) {
        if (!Double.isFinite(ivaPct)) {
            throw new IllegalArgumentException("El IVA debe ser un numero finito");
        }
        if (ivaPct < 0 || ivaPct > 100) {
            throw new IllegalArgumentException("El IVA debe estar entre 0 y 100");
        }
        this.ivaPct = ivaPct;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser null");
        }
        this.estado = estado;
    }

    public AtributosProducto getExtras() {
        return extras;
    }

    public void setExtras(AtributosProducto extras) {
        this.extras = extras;
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
