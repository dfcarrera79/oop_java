package com.univ2026.proformas.exportacion;

/** Datos del emisor que se muestran en los documentos exportados. */
public record DatosEmpresa(String nombre, String ruc, String telefono) {
    public static final DatosEmpresa CM_INSUMOS_MEDICOS =
            new DatosEmpresa("CM INSUMOS MEDICOS", "1150755997-001", "0997594324");

    public DatosEmpresa {
        nombre = obligatorio(nombre, "El nombre de la empresa no puede estar vacio");
        ruc = obligatorio(ruc, "El RUC de la empresa no puede estar vacio");
        telefono = obligatorio(telefono, "El telefono de la empresa no puede estar vacio");
    }

    private static String obligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }
}
