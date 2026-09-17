package com.univ2026.proformas.dominio.pago;

/** Cuenta o medio de pago configurable. */
public class CuentaPago {
    private Long id;
    private String nombre;
    private String instrucciones;

    public CuentaPago(Long id, String nombre, String instrucciones) {
        setId(id);
        setNombre(nombre);
        setInstrucciones(instrucciones);
    }

    public CuentaPago(String nombre, String instrucciones) {
        this(null, nombre, instrucciones);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("El id debe ser positivo");
        }
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        this.nombre = nombre.trim();
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public void setInstrucciones(String instrucciones) {
        if (instrucciones == null) {
            throw new IllegalArgumentException("Las instrucciones no pueden ser null");
        }
        this.instrucciones = instrucciones.trim();
    }
}
