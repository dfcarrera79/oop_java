package com.univ2026.proformas.presentacion;

import com.univ2026.proformas.modelo.Producto;
import java.util.Locale;

/** Convierte productos a texto sin producir salida. */
public final class FormateadorProducto {
    private FormateadorProducto() {}

    public static String formatear(Producto producto) {
        String estado = producto.isActivo() ? "activo" : "inactivo";
        return String.format(
                Locale.US,
                "[%s] %s - $%.2f (impuesto %.1f%%) - %s",
                producto.getCodigo(),
                producto.getNombre(),
                producto.getPrecio().doubleValue(),
                producto.getImpuestoPct(),
                estado);
    }
}
