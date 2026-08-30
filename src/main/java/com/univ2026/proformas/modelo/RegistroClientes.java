package com.univ2026.proformas.modelo;

import com.univ2026.proformas.modelo.valor.RUC;

/** Contrato para registrar y buscar clientes por su identidad. */
public interface RegistroClientes {
    /** Registra un cliente o lanza una excepcion si su identificacion ya existe. */
    void registrar(Cliente cliente);

    /** Retorna el cliente encontrado o null cuando la identificacion no esta registrada. */
    Cliente buscar(RUC identificacion);

    /** Valida una identificacion textual y retorna el cliente encontrado o null. */
    default Cliente buscar(String identificacion) {
        return buscar(new RUC(identificacion));
    }
}
