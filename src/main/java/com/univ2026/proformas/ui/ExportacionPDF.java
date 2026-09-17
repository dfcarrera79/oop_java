package com.univ2026.proformas.ui;

import com.univ2026.proformas.dominio.proforma.Proforma;
import java.nio.file.Path;

/** Punto de integracion de UI con el exportador opcional desarrollado en paralelo. */
@FunctionalInterface
public interface ExportacionPDF {
    Path exportar(Proforma proforma) throws Exception;
}
