package com.univ2026.proformas.dominio.producto;

import com.univ2026.proformas.dominio.Estado;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Contrato de acceso al catalogo persistente de productos. */
public interface CatalogoProductos {
    void registrar(Producto producto);

    Producto buscarPorCodigo(String codigo);

    List<Producto> listar();

    /** Busca parcialmente por codigo, nombre o descripcion, sin distinguir mayusculas. */
    List<Producto> buscar(String texto);

    Producto cambiarEstado(String codigo, Estado estado);

    /** Crea una vista indexada derivada de la consulta actual. */
    Map<String, Producto> indexarPorCodigo();

    /** Crea un conjunto de codigos derivado de la consulta actual. */
    Set<String> listarCodigos();
}
