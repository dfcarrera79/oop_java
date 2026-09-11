package com.univ2026.proformas.presentacion;

import com.univ2026.proformas.dominio.proforma.ItemProforma;
import com.univ2026.proformas.dominio.proforma.Proforma;
import java.util.Locale;

/** Convierte proformas a texto sin producir salida. */
public final class FormateadorProforma {
    private FormateadorProforma() {}

    public static String formatear(Proforma proforma) {
        int cantidad = proforma.getItems().size();
        String unidad = cantidad == 1 ? "item" : "items";
        return String.format(
                Locale.US,
                "Proforma %s - %s - %d %s - $%.2f",
                proforma.getNumero(),
                proforma.getCliente().getNombre(),
                cantidad,
                unidad,
                proforma.calcularTotal());
    }

    public static String formatearDetalle(Proforma proforma) {
        StringBuilder texto = new StringBuilder(formatear(proforma));
        for (ItemProforma item : proforma.getItems()) {
            texto.append(String.format(
                    Locale.US,
                    "%n  %d x %s: $%.2f",
                    item.getCantidad(),
                    item.getProducto().getNombre(),
                    item.calcularTotal()));
        }
        texto.append(String.format(
                Locale.US,
                "%nSubtotal: $%.2f%nIVA: $%.2f%nTotal: $%.2f",
                proforma.calcularSubtotal(),
                proforma.calcularImpuesto(),
                proforma.calcularTotal()));
        return texto.toString();
    }
}
