package com.univ2026.proformas.dominio.cliente;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.valor.Email;
import com.univ2026.proformas.dominio.valor.RUC;
import java.util.Objects;

/** Representa una persona o empresa a la que se emiten proformas. */
public class Cliente {
    private final RUC identificacion;
    private String nombre;
    private String direccion;
    private String telefono;
    private Email email;
    private TipoCliente tipo;
    private Estado estado;

    /** Crea un cliente con datos de contacto vacios. */
    public Cliente(String identificacion, String nombre) {
        this(new RUC(identificacion), nombre, "", "", new Email(""), TipoCliente.PUBLICO, Estado.ACTIVO);
    }

    /** Crea un cliente y aplica las mismas validaciones que los setters. */
    public Cliente(
            String identificacion,
            String nombre,
            String direccion,
            String telefono,
            String email,
            TipoCliente tipo,
            Estado estado) {
        this(new RUC(identificacion), nombre, direccion, telefono, new Email(email), tipo, estado);
    }

    /** Crea un cliente con modelos de valor para identificacion y correo. */
    public Cliente(
            RUC identificacion,
            String nombre,
            String direccion,
            String telefono,
            Email email,
            TipoCliente tipo,
            Estado estado) {
        if (identificacion == null) {
            throw new IllegalArgumentException("La identificacion no puede ser null");
        }
        this.identificacion = identificacion;
        setNombre(nombre);
        setDireccion(direccion);
        setTelefono(telefono);
        setEmail(email);
        setTipo(tipo);
        setEstado(estado);
    }

    public RUC getIdentificacion() {
        return identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = textoObligatorio(nombre, "El nombre no puede estar vacio");
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = textoOpcional(direccion, "La direccion no puede ser null");
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = textoOpcional(telefono, "El telefono no puede ser null");
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(String email) {
        setEmail(new Email(email));
    }

    public void setEmail(Email email) {
        if (email == null) {
            throw new IllegalArgumentException("El email no puede ser null");
        }
        this.email = email;
    }

    public TipoCliente getTipo() {
        return tipo;
    }

    public void setTipo(TipoCliente tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de cliente no puede ser null");
        }
        this.tipo = tipo;
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

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Cliente otro)) {
            return false;
        }
        return identificacion.equals(otro.identificacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificacion);
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
