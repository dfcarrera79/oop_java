package com.univ2026.proformas.dominio.pago;

import java.util.List;

/** Contrato persistente para medios de pago. */
public interface RepositorioCuentasPago {
    CuentaPago registrar(CuentaPago cuenta);

    CuentaPago actualizar(CuentaPago cuenta);

    CuentaPago buscarPorId(long id);

    List<CuentaPago> listar();

    List<CuentaPago> buscar(String texto);

    void eliminar(long id);
}
