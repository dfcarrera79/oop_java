package com.univ2026.proformas.persistencia.sqlite;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Operaciones JDBC compartidas para colecciones tipadas respaldadas por SQLite. */
public abstract class ColeccionSQLite<T> {
    private final BaseDatosSQLite baseDatos;

    protected ColeccionSQLite(Path archivoBaseDatos) {
        this(new BaseDatosSQLite(archivoBaseDatos));
    }

    protected ColeccionSQLite(BaseDatosSQLite baseDatos) {
        if (baseDatos == null) {
            throw new IllegalArgumentException("La base de datos no puede ser null");
        }
        this.baseDatos = baseDatos;
    }

    protected final Connection abrirConexion() throws SQLException {
        return baseDatos.abrirConexion();
    }

    protected final List<T> consultarLista(String sql, PreparadorSQL preparador) {
        List<T> resultados = new ArrayList<>();
        try (Connection conexion = abrirConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            preparador.preparar(sentencia);
            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    resultados.add(convertirFila(filas));
                }
            }
            return List.copyOf(resultados);
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar SQLite", error);
        }
    }

    protected abstract T convertirFila(ResultSet fila) throws SQLException;

    protected static String textoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }

    protected static String patronLike(String texto, boolean prefijo) {
        String valor = texto == null ? "" : texto.trim();
        valor = valor.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return prefijo ? valor + "%" : "%" + valor + "%";
    }
}
