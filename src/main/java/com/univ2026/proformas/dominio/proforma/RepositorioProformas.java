package com.univ2026.proformas.dominio.proforma;

import java.util.List;

/** Contrato de almacenamiento transaccional de proformas emitidas. */
public interface RepositorioProformas {
    Proforma guardar(Proforma proforma);

    Proforma buscarPorNumero(String numero);

    List<Proforma> listar();

    List<Proforma> buscar(String texto);

    void eliminar(String numero);
}
