package com.univ2026.proformas.persistencia.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Operaciones JDBC compartidas para colecciones tipadas respaldadas por SQLite. */
public abstract class ColeccionSQLite<T> {
    private final String urlConexion;

    protected ColeccionSQLite(Path archivoBaseDatos) {
        if (archivoBaseDatos == null) {
            throw new IllegalArgumentException("El archivo de base de datos no puede ser null");
        }
        Path archivoAbsoluto = archivoBaseDatos.toAbsolutePath();
        crearDirectorio(archivoAbsoluto.getParent());
        urlConexion = "jdbc:sqlite:" + archivoAbsoluto;
        InicializadorEsquema.inicializar(urlConexion);
    }

    protected final Connection abrirConexion() throws SQLException {
        return DriverManager.getConnection(urlConexion);
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

    private static void crearDirectorio(Path directorio) {
        if (directorio == null) {
            return;
        }
        try {
            Files.createDirectories(directorio);
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo crear el directorio de la base de datos", error);
        }
    }
}
