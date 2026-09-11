package com.univ2026.proformas.presentacion;

import com.univ2026.proformas.modelo.Cliente;

/** Convierte clientes a texto sin producir salida. */
public final class FormateadorCliente {
    private FormateadorCliente() {}

    public static String formatear(Cliente cliente) {
        String estado = cliente.isActivo() ? "activo" : "inactivo";
        return "[" + cliente.getIdentificacion() + "] " + cliente.getNombre() + " - " + cliente.getDireccion() + " - "
                + cliente.getTelefono() + " - " + cliente.getEmail() + " - " + estado;
    }
}
