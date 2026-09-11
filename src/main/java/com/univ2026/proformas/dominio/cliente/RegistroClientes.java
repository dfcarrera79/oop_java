package com.univ2026.proformas.dominio.cliente;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.valor.RUC;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Contrato para registrar y buscar clientes por su identidad. */
public interface RegistroClientes {
    /** Registra un cliente o lanza una excepcion si su identificacion ya existe. */
    void registrar(Cliente cliente);

    /** Retorna el cliente encontrado o null cuando la identificacion no esta registrada. */
    Cliente buscarPorIdentificacion(RUC identificacion);

    /** Valida una identificacion textual y retorna el cliente encontrado o null. */
    default Cliente buscarPorIdentificacion(String identificacion) {
        return buscarPorIdentificacion(new RUC(identificacion));
    }

    /** Retorna los clientes registrados sin exponer la coleccion interna. */
    List<Cliente> listar();

    /** Busca parcialmente por identificacion, nombre o correo, sin distinguir mayusculas. */
    List<Cliente> buscar(String texto);

    /** Persiste un nuevo estado y retorna el cliente reconstruido. */
    Cliente cambiarEstado(String identificacion, Estado estado);

    /** Crea una vista indexada derivada de la consulta actual. */
    Map<String, Cliente> indexarPorIdentificacion();

    /** Crea un conjunto de identificaciones derivado de la consulta actual. */
    Set<String> listarIdentificaciones();
}
