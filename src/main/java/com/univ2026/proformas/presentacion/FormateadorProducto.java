package com.univ2026.proformas.presentacion;

import com.univ2026.proformas.dominio.producto.AtributosFisicos;
import com.univ2026.proformas.dominio.producto.Producto;
import java.util.Locale;

/** Convierte productos a texto sin producir salida. */
public final class FormateadorProducto {
    private FormateadorProducto() {}

    public static String formatear(Producto producto) {
        StringBuilder texto = new StringBuilder(String.format(
                Locale.US,
                "[%s] %s - $%.2f",
                producto.getCodigo(),
                producto.getNombre(),
                producto.getPrecio().doubleValue()));
        if (producto.getExtras() instanceof AtributosFisicos atributos) {
            if (atributos.talla() != null) {
                texto.append(" - Talla ").append(atributos.talla().getEtiqueta());
            }
            texto.append(
                    String.format(Locale.US, " - (IVA %.1f%%) - %.1f kg", producto.getIvaPct(), atributos.pesoKg()));
        } else {
            texto.append(String.format(Locale.US, " - (IVA %.1f%%)", producto.getIvaPct()));
        }
        return texto.append(" - ").append(producto.getEstado().getEtiqueta()).toString();
    }
}
