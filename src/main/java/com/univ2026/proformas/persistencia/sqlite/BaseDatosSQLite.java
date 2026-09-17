package com.univ2026.proformas.persistencia.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Centraliza la ubicacion, apertura y configuracion de cada conexion SQLite. */
public final class BaseDatosSQLite {
    private final Path archivo;
    private final String urlConexion;

    public BaseDatosSQLite(Path archivo) {
        if (archivo == null) {
            throw new IllegalArgumentException("El archivo de base de datos no puede ser null");
        }
        this.archivo = archivo.toAbsolutePath().normalize();
        crearDirectorio(this.archivo.getParent());
        urlConexion = "jdbc:sqlite:" + this.archivo;
        InicializadorEsquema.inicializar(this);
    }

    public Path getArchivo() {
        return archivo;
    }

    Connection abrirConexion() throws SQLException {
        Connection conexion = DriverManager.getConnection(urlConexion);
        try (var sentencia = conexion.prepareStatement("PRAGMA foreign_keys = ON")) {
            sentencia.execute();
        } catch (SQLException error) {
            conexion.close();
            throw error;
        }
        return conexion;
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
