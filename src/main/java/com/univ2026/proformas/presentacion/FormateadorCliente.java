package com.univ2026.proformas.presentacion;

import com.univ2026.proformas.dominio.cliente.Cliente;

/** Convierte clientes a texto sin producir salida. */
public final class FormateadorCliente {
    private FormateadorCliente() {}

    public static String formatear(Cliente cliente) {
        return "[" + cliente.getIdentificacion() + "] " + cliente.getNombre() + " - " + cliente.getDireccion() + " - "
                + cliente.getTelefono() + " - " + cliente.getEmail() + " - "
                + cliente.getTipo().getEtiqueta() + " - "
                + cliente.getEstado().getEtiqueta();
    }
}
