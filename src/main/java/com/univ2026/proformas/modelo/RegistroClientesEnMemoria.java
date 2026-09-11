package com.univ2026.proformas.modelo;

import com.univ2026.proformas.modelo.valor.RUC;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Registro de clientes con unicidad por identificacion. */
public class RegistroClientesEnMemoria implements RegistroClientes {
    private final Map<RUC, Cliente> clientes = new HashMap<>();

    @Override
    public void registrar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser null");
        }
        if (clientes.containsKey(cliente.getIdentificacion())) {
            throw new IllegalArgumentException(
                    "ya existe un cliente con identificacion " + cliente.getIdentificacion());
        }
        clientes.put(cliente.getIdentificacion(), cliente);
    }

    @Override
    public Cliente buscar(RUC identificacion) {
        if (identificacion == null) {
            throw new IllegalArgumentException("La identificacion no puede ser null");
        }
        return clientes.get(identificacion);
    }

    @Override
    public List<Cliente> listar() {
        return List.copyOf(clientes.values());
    }
}
