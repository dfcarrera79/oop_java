package com.univ2026.proformas.persistencia.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Crea de manera idempotente el esquema inicial de productos y clientes. */
public final class InicializadorEsquema {
    private static final String TABLA_PRODUCTOS = """
            CREATE TABLE IF NOT EXISTS productos (
                codigo TEXT PRIMARY KEY NOT NULL,
                nombre TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                precio TEXT NOT NULL CHECK (CAST(precio AS REAL) >= 0),
                iva_pct REAL NOT NULL CHECK (iva_pct >= 0 AND iva_pct <= 100),
                estado TEXT NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO')),
                tipo_atributos TEXT CHECK (tipo_atributos IN ('FISICO', 'DIGITAL') OR tipo_atributos IS NULL),
                peso_kg REAL CHECK (peso_kg > 0 OR peso_kg IS NULL),
                talla TEXT,
                tamanio_mb REAL CHECK (tamanio_mb > 0 OR tamanio_mb IS NULL)
            )
            """;
    private static final String TABLA_CLIENTES = """
            CREATE TABLE IF NOT EXISTS clientes (
                identificacion TEXT PRIMARY KEY NOT NULL,
                nombre TEXT NOT NULL,
                direccion TEXT NOT NULL,
                telefono TEXT NOT NULL,
                email TEXT NOT NULL,
                tipo TEXT NOT NULL CHECK (tipo IN ('PUBLICO', 'MAYORISTA', 'MEDICO')),
                estado TEXT NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO'))
            )
            """;

    private InicializadorEsquema() {}

    static void inicializar(String urlConexion) {
        try (Connection conexion = DriverManager.getConnection(urlConexion);
                Statement sentencia = conexion.createStatement()) {
            sentencia.execute(TABLA_PRODUCTOS);
            sentencia.execute(TABLA_CLIENTES);
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo inicializar el esquema SQLite", error);
        }
    }
}
